package com.example.evencat_android.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.evencat_android.R
import com.example.evencat_android.RetrofitClient
import com.example.evencat_android.User
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import org.bouncycastle.crypto.engines.BlowfishEngine
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher
import org.bouncycastle.crypto.params.KeyParameter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.Base64

class RegisterActivity : AppCompatActivity() {
    private val secretKey = "999a999ale469993"
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private var imageUri: Uri? = null
    private lateinit var Image: ImageView
    private var imageUrl: String = ""


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        val imageButtonBack: ImageButton = findViewById(R.id.back_image_button)
        val buttonSingIn: Button = findViewById(R.id.sing_in_button)
        val textName: EditText = findViewById(R.id.nameText)
        val textEmail: EditText = findViewById(R.id.emailText)
        val textPassword1: EditText = findViewById(R.id.password1Text)
        val textPassword2: EditText = findViewById(R.id.password2Text)
        val buttonPassword: ImageButton = findViewById(R.id.showPassword)
        val buttonPassword2: ImageButton = findViewById(R.id.showPassword2)
        Image = findViewById(R.id.profile_picture)
        var passwordVisible = false
        var passwordVisible2 = false

        imageButtonBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        Image.setOnClickListener {
            showImagePickerDialog()
        }

        buttonSingIn.setOnClickListener {
            if (textName.text.toString().isEmpty()) {
                Toast.makeText(this, "Escribe un nombre de usuario", Toast.LENGTH_SHORT).show()
            } else if (textEmail.text.toString().isEmpty()) {
                Toast.makeText(this, "Escribe una dirección de correo válida", Toast.LENGTH_SHORT)
                    .show()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail.text.toString()).matches()) {
                Toast.makeText(this, "El formato del correo no es válido", Toast.LENGTH_SHORT)
                    .show()
            } else if (textPassword1.text.toString().length < 8) {
                Toast.makeText(this, "Escribe mínimo 8 caracteres", Toast.LENGTH_SHORT).show()
            } else if (textPassword1.text.toString() != textPassword2.text.toString()) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            } else if (imageUrl == ""){
                Toast.makeText(this, "Añade una imagen de perfil", Toast.LENGTH_SHORT).show()
            }
            else {
                uploadUser(
                    textName.text.toString(),
                    textEmail.text.toString(),
                    textPassword1.text.toString(),
                    imageUrl
                )
            }
        }

        buttonPassword.setOnClickListener{
            passwordVisible = !passwordVisible
            val selection = textPassword1.selectionStart

            if (passwordVisible) {
                textPassword1.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                buttonPassword.setImageResource(R.drawable.password_invisible)
            } else {
                textPassword1.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                buttonPassword.setImageResource(R.drawable.password_visible)
            }

            textPassword1.typeface = ResourcesCompat.getFont(this, R.font.poppins_medium)
            textPassword1.setSelection(selection)
        }

        buttonPassword2.setOnClickListener{
            passwordVisible2 = !passwordVisible2
            val selection = textPassword2.selectionStart

            if (passwordVisible2) {
                textPassword2.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                buttonPassword2.setImageResource(R.drawable.password_invisible)
            } else {
                textPassword2.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                buttonPassword2.setImageResource(R.drawable.password_visible)
            }

            textPassword2.typeface = ResourcesCompat.getFont(this, R.font.poppins_medium)
            textPassword2.setSelection(selection)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadUser(username: String, email: String, plainPassword: String, imageUrl: String) {
        val encryptedPassword = encryptPassword(plainPassword)

        val user = User(
            0,
            username,
            email,
            encryptedPassword,
            rol = "UsuariNormal",
            descripcion = "",
            imageUrl
        )

        RetrofitClient.instance.registerUser(user).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(applicationContext, "Usuario registrado con éxito", Toast.LENGTH_LONG).show()

                    // Guardar sesión automáticamente
                    MainActivity.UserSession.setUserData(
                        context = this@RegisterActivity,
                        id = 0, // Cambia si el backend devuelve el ID real
                        username = username,
                        email = email,
                        password = encryptedPassword,
                        rol = "UsuariNormal",
                        imageUrl = imageUrl,
                        description = ""
                    )

                    // Ir a ExploreActivity
                    val intent = Intent(this@RegisterActivity, ExploreActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(applicationContext, "Error en el servidor: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("Upload user", "Error: ${t.message}")
                Toast.makeText(applicationContext, "Error de conexión: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
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

        return Base64.getEncoder().encodeToString(outputBytes.copyOf(length))
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Hacer una foto", "Seleccionar de galería")

        AlertDialog.Builder(this)
            .setTitle("Selecciona una opción")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imageUri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider", // asegúrate que coincida con el `provider` de AndroidManifest
            createImageFile()
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("IMG_", ".jpg", storageDir)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            val uri = when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> imageUri
                REQUEST_IMAGE_PICK -> data?.data
                else -> null
            }

            uri?.let {
                Image.setImageURI(it) // Mostrar en el ImageView
                uploadImageWithLifecycle(it)     // Subir al servidor
            }
        }
    }

    private fun uploadImageWithLifecycle(uri: Uri) {
        lifecycleScope.launch {
            try {
                val mimeType = contentResolver.getType(uri) ?: "image/*"
                val fileExtension = when (mimeType) {
                    "image/jpeg" -> ".jpg"
                    "image/png" -> ".png"
                    else -> ".bin"
                }

                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val tempFile = File.createTempFile("upload_", fileExtension, cacheDir).apply {
                        deleteOnExit()
                    }

                    inputStream.copyTo(tempFile.outputStream())

                    val requestBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
                    val multipart = MultipartBody.Part.createFormData(
                        "file",
                        "image_${System.currentTimeMillis()}$fileExtension",
                        requestBody
                    )

                    val response = try {
                        RetrofitClient.instance.uploadImage(multipart)
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Network error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }

                    when {
                        response.isSuccessful -> {
                            imageUrl = response.body()?.url ?: ""
                            Toast.makeText(
                                this@RegisterActivity,
                                "Imagen subida: $imageUrl",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        response.code() == 400 -> {
                            val errorBody = response.errorBody()?.string()
                            Log.e("Upload Error", "400 Bad Request: $errorBody")
                            Toast.makeText(
                                this@RegisterActivity,
                                "Error del servidor: ${errorBody ?: "Formato inválido"}",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        else -> {
                            Toast.makeText(
                                this@RegisterActivity,
                                "Error ${response.code()}: ${response.message()}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Error: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}