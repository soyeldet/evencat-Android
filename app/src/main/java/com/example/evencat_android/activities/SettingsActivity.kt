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
import com.example.evencat_android.activities.LocaleHelper.setLocale
import okhttp3.ResponseBody
import org.bouncycastle.crypto.engines.BlowfishEngine
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher
import org.bouncycastle.crypto.params.KeyParameter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Base64

class SettingsActivity : AppCompatActivity() {
    private val secretKey = "999a999ale469993"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

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

        val languageCodes = arrayOf("es", "cat", "en")


        var passVisible1 = false
        var passVisible2 = false
        var passVisible3 = false

        backButton.setOnClickListener {
            finish()
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languageCodes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        idiomeSpinner.adapter = adapter

        val currentLang = getSavedLanguage(this)
        val selectedIndex = languageCodes.indexOf(currentLang)
        idiomeSpinner.setSelection(selectedIndex)

        idiomeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedLanguage = languageCodes[position]
                val currentLanguage = getSavedLanguage(this@SettingsActivity)
                if (selectedLanguage != currentLanguage) {
                    setLocale(this@SettingsActivity, selectedLanguage)

                    val intent = Intent(this@SettingsActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        nameEdit.setText(MainActivity.UserSession.username)
        emailEdit.setText(MainActivity.UserSession.email)

        saveButton.setOnClickListener {
            val name = nameEdit.text.toString().trim()
            val email = emailEdit.text.toString().trim()
            val pass1 = pass1Edit.text.toString()
            val pass2 = pass2Edit.text.toString()
            val pass3 = pass3Edit.text.toString()

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
            if (encryptPassword(pass3) != MainActivity.UserSession.password.toString()){
                toast("Escribe tu contraseña para modificarla")
                return@setOnClickListener
            }


            val encryptedPassword = if (pass1.isNotEmpty()) encryptPassword(pass1) else MainActivity.UserSession.password

            updateUser(
                id = MainActivity.UserSession.id ?: 0,
                username = name,
                email = email,
                encryptedPassword = encryptedPassword!!
            )
        }

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

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

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

    private fun updateUser(id: Int, username: String, email: String, encryptedPassword: String) {
        val user = User(
            id = id,
            nombre = username,
            correo = email,
            contrasena = encryptedPassword,
            rol = MainActivity.UserSession.rol ?: "UsuariNormal",
            image_url = "",
            descripcion = MainActivity.UserSession.description ?: ""
        )

        RetrofitClient.instance.updateUser(id, user).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    toast("Cambios guardados correctamente")
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

    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return prefs.getString("lang", "es") ?: "es"
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, getSavedLanguage(newBase)))
    }

}
