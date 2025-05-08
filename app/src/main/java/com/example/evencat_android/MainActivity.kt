package com.example.evencat_android

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.example.prueba_beat_on_jeans.api.RetrofitClient
import com.example.prueba_beat_on_jeans.api.User
import com.example.prueba_beat_on_jeans.api.UserLogin
import kotlinx.coroutines.launch
import org.bouncycastle.crypto.engines.BlowfishEngine
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher
import org.bouncycastle.crypto.params.KeyParameter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private val secretKey = "999a999ale469993"
    private var shouldRememberUser = true

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonSingIn: Button = findViewById(R.id.sing_in_button)
        val buttonSingUp: Button = findViewById(R.id.sing_up_button)
        val buttonPassword: ImageButton = findViewById(R.id.showPassword)
        val textEmail: EditText = findViewById(R.id.emailText)
        val textPassword: EditText = findViewById(R.id.passwordText)
        val rememberMe: SwitchCompat = findViewById(R.id.switchRememberMe)

        // Verifica si ya hay sesión guardada
        UserSession.loadUserData(this)
        if (UserSession.isLoggedIn) {
            val intent = Intent(this, ExploreActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Captura el estado del switch
        rememberMe.setOnCheckedChangeListener { _, isChecked ->
            shouldRememberUser = isChecked
        }

        var passwordVisible = false

        buttonSingIn.setOnClickListener {
            verifyUser(textEmail.text.toString(), textPassword.text.toString())
        }

        buttonSingUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        buttonPassword.setOnClickListener {
            passwordVisible = !passwordVisible
            val selection = textPassword.selectionStart

            if (passwordVisible) {
                textPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                buttonPassword.setImageResource(R.drawable.password_invisible)
            } else {
                textPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                buttonPassword.setImageResource(R.drawable.password_visible)
            }

            textPassword.typeface = ResourcesCompat.getFont(this, R.font.poppins_medium)
            textPassword.setSelection(selection)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun verifyUser(username: String, encryptedPassword: String) {
        lifecycleScope.launch {
            val user = UserLogin(username, encryptPassword(encryptedPassword))
            RetrofitClient.instance.loginUser(user).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful) {
                        val userRecieved = response.body()
                        if (userRecieved != null) {
                            if (shouldRememberUser) {
                                UserSession.setUserData(
                                    context = this@MainActivity,
                                    id = userRecieved.id,
                                    username = userRecieved.nombre,
                                    email = userRecieved.correo,
                                    password = userRecieved.contrasena,
                                    rol = userRecieved.rol,
                                    description = userRecieved.descripcion
                                )
                            } else {
                                UserSession.id = userRecieved.id
                                UserSession.username = userRecieved.nombre
                                UserSession.email = userRecieved.correo
                                UserSession.password = userRecieved.contrasena
                                UserSession.rol = userRecieved.rol
                                UserSession.descripcion = userRecieved.descripcion
                                UserSession.isLoggedIn = true
                            }

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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun encryptPassword(pswd: String): String {
        val engine = BlowfishEngine()
        val blockCipher = PaddedBufferedBlockCipher(engine)

        val keyBytes = secretKey.toByteArray(Charsets.UTF_8)
        blockCipher.init(true, KeyParameter(keyBytes))

        val inputBytes = pswd.toByteArray(Charsets.UTF_8)
        val outputBytes = ByteArray(blockCipher.getOutputSize(inputBytes.size))

        var length = blockCipher.processBytes(inputBytes, 0, inputBytes.size, outputBytes, 0)
        length += blockCipher.doFinal(outputBytes, length)

        return java.util.Base64.getEncoder().encodeToString(outputBytes.copyOf(length))
    }

    object UserSession {
        var id: Int? = null
        var username: String? = null
        var email: String? = null
        var password: String? = null
        var rol: String? = null
        var descripcion: String? = null
        var isLoggedIn: Boolean = false

        fun setUserData(context: Context, id: Int, username: String, email: String,
                        password: String, rol: String, description: String) {
            UserSession.id = id
            UserSession.username = username
            UserSession.email = email
            UserSession.password = password
            UserSession.rol = rol
            UserSession.descripcion = description
            UserSession.isLoggedIn = true

            val sharedPref = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putInt("id", id)
                putString("username", username)
                putString("email", email)
                putString("password", password)
                putString("rol", rol)
                putString("description", description)
                putBoolean("isLoggedIn", true)
                apply()
            }
        }

        fun loadUserData(context: Context) {
            val sharedPref = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            id = sharedPref.getInt("id", 0)
            email = sharedPref.getString("email", null)
            password = sharedPref.getString("password", null)
            rol = sharedPref.getString("rol", null)
            username = sharedPref.getString("username", null)
            descripcion = sharedPref.getString("description", null)
            isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
        }

        fun clearSession(context: Context) {
            id = 0
            username = null
            email = null
            password = null
            rol = null
            descripcion = null
            isLoggedIn = false

            val sharedPref = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                clear()
                apply()
            }
        }
    }
}
