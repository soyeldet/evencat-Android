// Declaración del paquete y importaciones necesarias
package com.example.evencat_android.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.evencat_android.R
import com.example.evencat_android.RetrofitClient
import com.example.evencat_android.User
import com.example.evencat_android.classes.LocaleHelper
import com.example.evencat_android.classes.LocaleHelper.setLocale
import okhttp3.ResponseBody
import org.bouncycastle.crypto.engines.BlowfishEngine
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher
import org.bouncycastle.crypto.params.KeyParameter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Base64

/**
 * Actividad para la configuración del usuario.
 * Permite cambiar nombre, email, contraseña e idioma de la aplicación.
 */
class SettingsActivity : AppCompatActivity() {
    // Clave secreta para el cifrado de contraseñas
    private val secretKey = "999a999ale469993"

    /**
     * Método llamado cuando se crea la actividad.
     * Configura la interfaz de usuario y los listeners.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Establece el layout de la actividad
        setContentView(R.layout.activity_settings)

        // Habilita el diseño edge-to-edge (bordes completos)
        enableEdgeToEdge()

        // Ajusta los márgenes para evitar superposiciones con la barra de sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        // Obtener referencias a los elementos de la UI
        val backButton: ImageButton = findViewById(R.id.back_image_button)
        val saveButton: Button = findViewById(R.id.sing_in_button)
        val nameEdit: EditText = findViewById(R.id.nameText)
        val emailEdit: EditText = findViewById(R.id.emailText)
        val pass1Edit: EditText = findViewById(R.id.password1Text)
        val pass2Edit: EditText = findViewById(R.id.password2Text)
        val pass3Edit: EditText = findViewById(R.id.passwordOldText)
        val showPass1: ImageButton = findViewById(R.id.showPassword)
        val showPass2: ImageButton = findViewById(R.id.showPassword2)
        val showPass3: ImageButton = findViewById(R.id.showPasswordOld)
        val idiomeSpinner: Spinner = findViewById(R.id.idiome)

        // Códigos de idiomas soportados
        val languageCodes = arrayOf("es", "cat", "en")

        // Variables para controlar la visibilidad de las contraseñas
        var passVisible1 = false
        var passVisible2 = false
        var passVisible3 = false

        // Configurar el listener para el botón de retroceso
        backButton.setOnClickListener {
            finish()
        }

        // Configurar el Spinner de idiomas
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languageCodes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        idiomeSpinner.adapter = adapter

        // Establecer el idioma actual seleccionado
        val currentLang = getSavedLanguage(this)
        val selectedIndex = languageCodes.indexOf(currentLang)
        idiomeSpinner.setSelection(selectedIndex)

        // Configurar el listener para cambios de idioma
        idiomeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedLanguage = languageCodes[position]
                val currentLanguage = getSavedLanguage(this@SettingsActivity)
                if (selectedLanguage != currentLanguage) {
                    // Cambiar el idioma de la aplicación
                    setLocale(this@SettingsActivity, selectedLanguage)

                    // Reiniciar la aplicación para aplicar los cambios de idioma
                    val intent = Intent(this@SettingsActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Mostrar los datos actuales del usuario
        nameEdit.setText(MainActivity.UserSession.username)
        emailEdit.setText(MainActivity.UserSession.email)

        // Configurar el listener para el botón de guardar
        saveButton.setOnClickListener {
            // Obtener los valores de los campos
            val name = nameEdit.text.toString().trim()
            val email = emailEdit.text.toString().trim()
            val pass1 = pass1Edit.text.toString()
            val pass2 = pass2Edit.text.toString()
            val pass3 = pass3Edit.text.toString()

            // Validaciones de los campos
            if (name.isEmpty()) {
                toast("El nombre no puede estar vacío")
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                toast("Correo electrónico no válido")
                return@setOnClickListener
            }

            if (pass1 != pass2) {
                toast("Las contraseñas no coinciden")
                return@setOnClickListener
            }

            if (pass1.length < 8) {
                toast("La contraseña debe tener al menos 8 caracteres")
                return@setOnClickListener
            }

            // Verificar que la contraseña actual sea correcta
            if (encryptPassword(pass3) != MainActivity.UserSession.password.toString()){
                toast("Escribe tu contraseña para modificarla")
                return@setOnClickListener
            }

            // Cifrar la nueva contraseña o mantener la actual si no se cambió
            val encryptedPassword = if (pass1.isNotEmpty()) encryptPassword(pass1) else MainActivity.UserSession.password

            // Actualizar los datos del usuario
            updateUser(
                id = MainActivity.UserSession.id ?: 0,
                username = name,
                email = email,
                encryptedPassword = encryptedPassword!!
            )
        }

        // Configurar listeners para mostrar/ocultar contraseñas
        showPass1.setOnClickListener {
            passVisible1 = !passVisible1
            togglePasswordVisibility(pass1Edit, passVisible1, showPass1)
        }

        showPass2.setOnClickListener {
            passVisible2 = !passVisible2
            togglePasswordVisibility(pass2Edit, passVisible2, showPass2)
        }

        showPass3.setOnClickListener {
            passVisible3 = !passVisible3
            togglePasswordVisibility(pass3Edit, passVisible3, showPass3)
        }
    }

    /**
     * Muestra un Toast con un mensaje.
     * @param msg Mensaje a mostrar
     */
    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    /**
     * Alterna la visibilidad del texto en un EditText de contraseña.
     * @param editText Campo de contraseña
     * @param visible Si debe mostrarse el texto
     * @param button Botón que activó el cambio
     */
    private fun togglePasswordVisibility(editText: EditText, visible: Boolean, button: ImageButton) {
        val selection = editText.selectionStart
        editText.inputType = if (visible) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        button.setImageResource(if (visible) R.drawable.password_invisible else R.drawable.password_visible)
        editText.typeface = ResourcesCompat.getFont(this, R.font.poppins_medium)
        editText.setSelection(selection)
    }

    /**
     * Cifra una contraseña usando el algoritmo Blowfish.
     * @param password Contraseña en texto plano
     * @return Contraseña cifrada en Base64
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun encryptPassword(password: String): String {
        val engine = BlowfishEngine()
        val cipher = PaddedBufferedBlockCipher(engine)
        val keyBytes = secretKey.toByteArray(Charsets.UTF_8)
        cipher.init(true, KeyParameter(keyBytes))

        val inputBytes = password.toByteArray(Charsets.UTF_8)
        val outputBytes = ByteArray(cipher.getOutputSize(inputBytes.size))
        var len = cipher.processBytes(inputBytes, 0, inputBytes.size, outputBytes, 0)
        len += cipher.doFinal(outputBytes, len)

        return Base64.getEncoder().encodeToString(outputBytes.copyOf(len))
    }

    /**
     * Actualiza los datos del usuario en el servidor.
     * @param id ID del usuario
     * @param username Nuevo nombre de usuario
     * @param email Nuevo correo electrónico
     * @param encryptedPassword Nueva contraseña cifrada
     */
    private fun updateUser(id: Int, username: String, email: String, encryptedPassword: String) {
        // Crear objeto User con los datos actualizados
        val user = User(
            id = id,
            nombre = username,
            correo = email,
            contrasena = encryptedPassword,
            rol = MainActivity.UserSession.rol ?: "UsuariNormal",
            image_url = "",
            descripcion = MainActivity.UserSession.description ?: ""
        )

        // Llamada a la API para actualizar el usuario
        RetrofitClient.instance.updateUser(id, user).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    toast("Cambios guardados correctamente")
                    // Actualizar los datos de sesión
                    MainActivity.UserSession.setUserData(
                        context = this@SettingsActivity,
                        id = id,
                        username = username,
                        email = email,
                        password = encryptedPassword,
                        rol = user.rol,
                        imageUrl = MainActivity.UserSession.imageUrl,
                        description = user.descripcion
                    )
                    finish()
                } else {
                    toast("Error al actualizar: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("SettingsActivity", "Fallo al actualizar usuario", t)
                toast("Error de red: ${t.message}")
            }
        })
    }

    /**
     * Obtiene el idioma guardado en las preferencias.
     * @param context Contexto de la aplicación
     * @return Código del idioma guardado ("es" por defecto)
     */
    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return prefs.getString("lang", "es") ?: "es"
    }

    /**
     * Sobrescribe el contexto base para aplicar el idioma seleccionado.
     */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, getSavedLanguage(newBase)))
    }
}