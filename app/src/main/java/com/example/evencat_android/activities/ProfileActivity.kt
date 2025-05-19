package com.example.evencat_android.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.evencat_android.AmicsRequest
import com.example.evencat_android.R
import com.example.evencat_android.RetrofitClient
import com.example.evencat_android.activities.RegisterActivity
import com.example.evencat_android.adapters.UserBubbleAdapter
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * Actividad de perfil de usuario.
 * Permite al usuario ver y editar su perfil, cambiar su imagen, y gestionar sus amigos.
 */
class ProfileActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private var imageUri: Uri? = null
    private lateinit var profileImageView: CircleImageView
    private var imageUrlNew: String = ""
    private lateinit var buttonProfile2: CircleImageView
    private lateinit var FriendsRV: RecyclerView

    /**
     * Se ejecuta al crear la actividad.
     * Inicializa la vista, establece valores del usuario, listeners, y comportamientos interactivos.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        var editing = false

        // Referencias a elementos de la interfaz
        val imageButtonMenu: ImageButton = findViewById(R.id.menu_image_utton)
        val nameText: EditText = findViewById(R.id.nameText)
        val descriptionText: EditText = findViewById(R.id.descriptionText)
        val editTextProfile: TextView = findViewById(R.id.editProfileText)
        val iconEditProfile: ImageView = findViewById(R.id.iconEditProfile)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val editProfile: Button = findViewById(R.id.editProfile)
        val buttonSettings: Button = findViewById(R.id.settings)
        val add_friends: Button = findViewById(R.id.add_friends)

        val buttonExplore: Button = findViewById(R.id.explore_button_menu)
        val buttonEvents: Button = findViewById(R.id.events_button_menu)
        val buttonProfile: Button = findViewById(R.id.profile_button_menu)
        buttonProfile2 = findViewById(R.id.profile_image_button)
        val buttonExit: Button = findViewById(R.id.exit)
        val username: TextView = findViewById(R.id.username)
        username.setText(MainActivity.UserSession.username.toString())

        profileImageView = findViewById(R.id.porfile_picture)

        /**
         * Carga la imagen del usuario en el perfil.
         */
        val imageUrl = MainActivity.UserSession.imageUrl
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this).load(imageUrl).placeholder(R.drawable.profile_p)
                .error(R.drawable.profile_p).into(profileImageView)
            Glide.with(this).load(imageUrl).into(buttonProfile2)
        }

        /**
         * Permite cambiar la imagen de perfil.
         */
        profileImageView.setOnClickListener {
            showImagePickerDialog()
        }

        /**
         * Cierra sesión del usuario y vuelve a la pantalla principal.
         */
        buttonExit.setOnClickListener {
            MainActivity.UserSession.clearSession(this)
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Navegación del menú lateral
        buttonSettings.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
        buttonExplore.setOnClickListener { startActivity(Intent(this, ExploreActivity::class.java)) }
        buttonEvents.setOnClickListener { startActivity(Intent(this, UserEventsActivity::class.java)) }
        buttonProfile.setOnClickListener { drawerLayout.closeDrawer(GravityCompat.START) }
        buttonProfile2.setOnClickListener { drawerLayout.closeDrawer(GravityCompat.START) }
        imageButtonMenu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }

        // Configuración inicial de campos y RV
        nameText.setText(MainActivity.UserSession.username)
        descriptionText.setText(MainActivity.UserSession.description)
        FriendsRV = findViewById(R.id.friendsRView)
        FriendsRV.layoutManager = GridLayoutManager(this, 4)
        loadFriends()

        // Deshabilitar edición por defecto
        disableEditing(nameText, descriptionText, profileImageView, add_friends)

        /**
         * Alterna entre modo edición y modo visualización del perfil.
         */
        editProfile.setOnClickListener {
            if (!editing) {
                editing = true
                enableEditing(nameText, descriptionText, profileImageView, add_friends)
                iconEditProfile.isVisible = false
                profileImageView.isEnabled = true
                profileImageView.isClickable = true
                profileImageView.isFocusable = true
                editTextProfile.text = getString(R.string.save)
            } else {
                editing = false
                disableEditing(nameText, descriptionText, profileImageView, add_friends)
                iconEditProfile.isVisible = true
                editTextProfile.text = getString(R.string.edit_profile)

                // Guardar cambios
                lifecycleScope.launch {
                    try {
                        val userId = MainActivity.UserSession.id ?: return@launch
                        RetrofitClient.instance.updateDescription(userId, descriptionText.text.toString())
                        RetrofitClient.instance.updateName(userId, nameText.text.toString())
                        MainActivity.UserSession.description = descriptionText.text.toString()
                        MainActivity.UserSession.username = nameText.text.toString()
                        MainActivity.UserSession.saveSession(this@ProfileActivity)
                        Toast.makeText(this@ProfileActivity, "Saved", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("Profile", "Error al guardar cambios", e)
                    }
                }
            }
        }

        /**
         * Muestra un cuadro de diálogo para añadir amigos.
         */
        add_friends.setOnClickListener {
            showAddFriendDialog()
        }
    }

    /**
     * Muestra un diálogo para elegir entre cámara o galería.
     */
    private fun showImagePickerDialog() {
        val options = arrayOf("Hacer una foto", "Seleccionar de galería")
        AlertDialog.Builder(this)
            .setTitle("Selecciona una opción")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }.show()
    }

    /**
     * Abre la cámara del dispositivo.
     */
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imageUri = FileProvider.getUriForFile(this, "${packageName}.provider", createImageFile())
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    /**
     * Abre la galería para seleccionar una imagen.
     */
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    /**
     * Crea un archivo temporal para almacenar la imagen tomada por la cámara.
     */
    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("IMG_", ".jpg", storageDir)
    }

    /**
     * Maneja el resultado de seleccionar o tomar una imagen.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val uri = when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> imageUri
                REQUEST_IMAGE_PICK -> data?.data
                else -> null
            }

            uri?.let {
                profileImageView.setImageURI(it)
                buttonProfile2.setImageURI(it)
                uploadImageWithLifecycle(it)
            }
        }
    }

    /**
     * Sube la imagen seleccionada al servidor y actualiza el perfil.
     */
    private fun uploadImageWithLifecycle(uri: Uri) {
        lifecycleScope.launch {
            try {
                val mimeType = contentResolver.getType(uri) ?: "image/*"
                val ext = when (mimeType) {
                    "image/jpeg" -> ".jpg"
                    "image/png" -> ".png"
                    else -> ".bin"
                }
                contentResolver.openInputStream(uri)?.use { input ->
                    val tempFile = File.createTempFile("upload_", ext, cacheDir).apply { deleteOnExit() }
                    input.copyTo(tempFile.outputStream())

                    val body = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("file", "image_${System.currentTimeMillis()}$ext", body)
                    val response = RetrofitClient.instance.uploadImage(part)

                    if (response.isSuccessful) {
                        imageUrlNew = response.body()?.url ?: ""
                        MainActivity.UserSession.imageUrl = imageUrlNew
                        MainActivity.UserSession.saveSession(this@ProfileActivity)
                        Toast.makeText(this@ProfileActivity, "Imagen actualizada", Toast.LENGTH_SHORT).show()

                        RetrofitClient.instance.updateImageUrl(MainActivity.UserSession.id!!, imageUrlNew)
                    } else {
                        Toast.makeText(this@ProfileActivity, "Error al subir imagen: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Muestra un diálogo para introducir un ID de amigo y enviarlo.
     */
    private fun showAddFriendDialog() {
        val input = EditText(this).apply {
            hint = "ID del amigo"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        AlertDialog.Builder(this)
            .setTitle("Añadir amigo")
            .setView(input)
            .setPositiveButton("Añadir") { dialog, _ ->
                val id = input.text.toString().toIntOrNull()
                if (id == null) {
                    Toast.makeText(this, "ID inválido", Toast.LENGTH_SHORT).show()
                } else {
                    addFriendApi(id)
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
            .show()
    }

    /**
     * Envía una solicitud al servidor para añadir a un amigo.
     */
    private fun addFriendApi(friendId: Int) {
        lifecycleScope.launch {
            try {
                val userId = MainActivity.UserSession.id ?: return@launch
                val request = AmicsRequest(usuari1_id = userId, usuari2_id = friendId)
                val response = RetrofitClient.instance.addFriend(request)

                if (response.isSuccessful) {
                    Toast.makeText(this@ProfileActivity, "Amigo añadido", Toast.LENGTH_SHORT).show()
                    loadFriends()
                } else {
                    Toast.makeText(this@ProfileActivity, "Error: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Carga la lista de amigos del usuario y la muestra en el RecyclerView.
     */
    private fun loadFriends() {
        lifecycleScope.launch {
            val userId = MainActivity.UserSession.id ?: return@launch
            val response = RetrofitClient.instance.getFriends(userId)
            if (response.isSuccessful) {
                val friends = response.body()?.filter { it.id != userId } ?: emptyList()
                val adapter = UserBubbleAdapter(friends).apply {
                    setOnItemClickListener { user ->
                        Toast.makeText(this@ProfileActivity, "Amigo seleccionado: ${user.name}", Toast.LENGTH_SHORT).show()
                    }
                }
                FriendsRV.adapter = adapter
            }
        }
    }

    /**
     * Habilita la edición del perfil.
     */
    private fun enableEditing(name: EditText, desc: EditText, image: ImageView, addBtn: Button) {
        name.isFocusableInTouchMode = true
        desc.isFocusableInTouchMode = true
        image.isClickable = true
        addBtn.isEnabled = true
    }

    /**
     * Deshabilita la edición del perfil.
     */
    private fun disableEditing(name: EditText, desc: EditText, image: ImageView, addBtn: Button) {
        name.isFocusable = false
        name.isClickable = false
        name.setCursorVisible(false)

        desc.isFocusable = false
        desc.isClickable = false
        desc.setCursorVisible(false)

        image.isEnabled = false
        addBtn.isEnabled = false
    }
}