package com.example.evencat_android.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.example.evencat_android.R
import com.example.evencat_android.RetrofitClient
import com.example.evencat_android.User
import com.example.evencat_android.UserLogin
import kotlinx.coroutines.launch
import org.bouncycastle.crypto.engines.BlowfishEngine
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher
import org.bouncycastle.crypto.params.KeyParameter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Base64
import java.util.Locale

/**
 * Actividad principal que maneja el inicio de sesión de usuarios.
 * También gestiona la sesión persistente y la internacionalización.
 */
class MainActivity : AppCompatActivity() {
    // Clave secreta para el cifrado de contraseñas
    private val secretKey = "999a999ale469993"
    // Flag para recordar usuario
    private var shouldRememberUser = true

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Habilitar edge-to-edge (aprovechar toda la pantalla)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Hacer la barra de estado transparente
        window.statusBarColor = Color.TRANSPARENT

        // Establecer idioma guardado
        setLocale(this, getSavedLanguage(this))

        // Obtener referencias a los elementos de la UI
        val buttonSingIn: Button = findViewById(R.id.sing_in_button)
        val buttonSingUp: Button = findViewById(R.id.sing_up_button)
        val buttonPassword: ImageButton = findViewById(R.id.showPassword)
        val textEmail: EditText = findViewById(R.id.emailText)
        val textPassword: EditText = findViewById(R.id.passwordText)
        val rememberMe: SwitchCompat = findViewById(R.id.switchRememberMe)

        // Cargar datos de sesión si existen
        UserSession.loadUserData(this)
        if (UserSession.isLoggedIn) {
            // Si hay sesión activa, redirigir a ExploreActivity
            val intent = Intent(this, ExploreActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Listener para el switch "Recordarme"
        rememberMe.setOnCheckedChangeListener { _, isChecked ->
            shouldRememberUser = isChecked
        }

        // Variable para controlar visibilidad de contraseña
        var passwordVisible = false

        // Configurar botón de inicio de sesión
        buttonSingIn.setOnClickListener {
            verifyUser(textEmail.text.toString(), textPassword.text.toString())
        }

        // Configurar botón de registro
        buttonSingUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Configurar botón para mostrar/ocultar contraseña
        buttonPassword.setOnClickListener {
            passwordVisible = !passwordVisible
            val selection = textPassword.selectionStart

            if (passwordVisible) {
                // Mostrar contraseña
                textPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                buttonPassword.setImageResource(R.drawable.password_invisible)
            } else {
                // Ocultar contraseña
                textPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                buttonPassword.setImageResource(R.drawable.password_visible)
            }

            // Mantener el tipo de letra y posición del cursor
            textPassword.typeface = ResourcesCompat.getFont(this, R.font.poppins_medium)
            textPassword.setSelection(selection)
        }
    }

    /**
     * Verifica las credenciales del usuario con el servidor
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun verifyUser(username: String, encryptedPassword: String) {
        lifecycleScope.launch {
            // Crear objeto UserLogin con credenciales cifradas
            val user = UserLogin(username, encryptPassword(encryptedPassword))

            // Llamada a la API para login
            RetrofitClient.instance.loginUser(user).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful) {
                        val userRecieved = response.body()
                        if (userRecieved != null) {
                            // Guardar datos de usuario según preferencia "Recordarme"
                            if (shouldRememberUser) {
                                UserSession.setUserData(
                                    context = this@MainActivity,
                                    id = userRecieved.id,
                                    username = userRecieved.nombre,
                                    email = userRecieved.correo,
                                    password = userRecieved.contrasena,
                                    rol = userRecieved.rol,
                                    description = userRecieved.descripcion,
                                    imageUrl = userRecieved.image_url
                                )
                            } else {
                                // Solo guardar en memoria (no persistente)
                                UserSession.id = userRecieved.id
                                UserSession.username = userRecieved.nombre
                                UserSession.email = userRecieved.correo
                                UserSession.password = userRecieved.contrasena
                                UserSession.rol = userRecieved.rol
                                UserSession.description = userRecieved.descripcion
                                UserSession.isLoggedIn = true
                            }

                            // Redirigir a la pantalla principal
                            val intent = Intent(this@MainActivity, ExploreActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Toast.makeText(applicationContext, "Correo o contraseña incorrectos.", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(applicationContext, "Error de conexión: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    /**
     * Cifra una contraseña usando el algoritmo Blowfish
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun encryptPassword(pswd: String): String {
        val engine = BlowfishEngine()
        val blockCipher = PaddedBufferedBlockCipher(engine)

        // Configurar clave de cifrado
        val keyBytes = secretKey.toByteArray(Charsets.UTF_8)
        blockCipher.init(true, KeyParameter(keyBytes))

        // Cifrar contraseña
        val inputBytes = pswd.toByteArray(Charsets.UTF_8)
        val outputBytes = ByteArray(blockCipher.getOutputSize(inputBytes.size))

        var length = blockCipher.processBytes(inputBytes, 0, inputBytes.size, outputBytes, 0)
        length += blockCipher.doFinal(outputBytes, length)

        // Devolver contraseña cifrada en Base64
        return Base64.getEncoder().encodeToString(outputBytes.copyOf(length))
    }

    /**
     * Objeto companion para gestionar la sesión del usuario
     */
    object UserSession {
        // Propiedades de la sesión
        var id: Int? = null
        var username: String? = null
        var email: String? = null
        var password: String? = null
        var rol: String? = null
        var description: String? = null
        var imageUrl: String? = null
        var isLoggedIn: Boolean = false

        /**
         * Guarda los datos del usuario en SharedPreferences
         */
        fun setUserData(
            context: Context,
            id: Int,
            username: String,
            email: String,
            password: String,
            rol: String,
            description: String?,
            imageUrl: String?
        ) {
            // Actualizar propiedades en memoria
            UserSession.id = id
            UserSession.username = username
            UserSession.email = email
            UserSession.password = password
            UserSession.rol = rol
            UserSession.description = description
            UserSession.imageUrl = imageUrl
            UserSession.isLoggedIn = true

            // Guardar en SharedPreferences
            val sharedPref = context.getSharedPreferences("UserSession", MODE_PRIVATE)
            with(sharedPref.edit()) {
                putInt("id", id)
                putString("username", username)
                putString("email", email)
                putString("password", password)
                putString("rol", rol)
                putString("description", description)
                putString("image_url", imageUrl)
                putBoolean("isLoggedIn", true)
                apply()
            }
        }

        /**
         * Carga los datos del usuario desde SharedPreferences
         */
        fun loadUserData(context: Context) {
            val sharedPref = context.getSharedPreferences("UserSession", MODE_PRIVATE)
            id = sharedPref.getInt("id", 0)
            email = sharedPref.getString("email", null)
            password = sharedPref.getString("password", null)
            rol = sharedPref.getString("rol", null)
            username = sharedPref.getString("username", null)
            description = sharedPref.getString("description", null)
            imageUrl = sharedPref.getString("image_url", null)
            isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
        }

        /**
         * Limpia los datos de sesión (logout)
         */
        fun clearSession(context: Context) {
            // Limpiar propiedades en memoria
            id = 0
            username = null
            email = null
            password = null
            rol = null
            description = null
            isLoggedIn = false

            // Limpiar SharedPreferences
            val sharedPref = context.getSharedPreferences("UserSession", MODE_PRIVATE)
            with(sharedPref.edit()) {
                clear()
                apply()
            }
        }

        /**
         * Guarda parcialmente la sesión (para actualizaciones)
         */
        fun saveSession(context: Context) {
            val sharedPref = context.getSharedPreferences("UserSession", MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("username", username)
                putString("description", description)
                putString("image_url", imageUrl)
                apply()
            }
        }
    }

    // Métodos para gestión de idioma/localización

    /**
     * Adjunta el contexto con la configuración de idioma correcta
     */
    override fun attachBaseContext(newBase: Context) {
        val lang = newBase.getSharedPreferences("settings", MODE_PRIVATE).getString("lang", "es") ?: "es"
        val context = updateBaseContextLocale(newBase, lang)
        super.attachBaseContext(context)
    }

    /**
     * Actualiza el contexto con una nueva configuración regional
     */
    fun updateBaseContextLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = context.resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }

    /**
     * Establece el idioma de la aplicación
     */
    fun setLocale(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources = context.resources
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        // Guardar preferencia de idioma
        val prefs = context.getSharedPreferences("settings", MODE_PRIVATE)
        prefs.edit().putString("lang", language).apply()
    }

    /**
     * Obtiene el idioma guardado en preferencias
     */
    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("settings", MODE_PRIVATE)
        return prefs.getString("lang", "es") ?: "es"
    }
}