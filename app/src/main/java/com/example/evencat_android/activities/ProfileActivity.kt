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

class ProfileActivity : AppCompatActivity() {
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private var imageUri: Uri? = null
    private lateinit var profileImageView: CircleImageView
    private var imageUrlNew: String = ""
    private lateinit var buttonProfile2: CircleImageView
    private lateinit var FriendsRV: RecyclerView

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

        val imageButtonMenu: ImageButton = findViewById(R.id.menu_image_utton)
        val nameText: EditText = findViewById(R.id.nameText)
        val descriptionText: EditText = findViewById(R.id.descriptionText)
        val editTextProfile: TextView = findViewById(R.id.editProfileText)
        val iconEditProfile: ImageView = findViewById(R.id.iconEditProfile)
        FriendsRV = findViewById(R.id.friendsRView)
        val editProfile: Button = findViewById(R.id.editProfile)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
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

        val imageUrl = MainActivity.UserSession.imageUrl
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.profile_p)
                .error(R.drawable.profile_p)
                .into(profileImageView)
        }

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.profile_p)
                .error(R.drawable.profile_p)
                .into(buttonProfile2)
        }

        profileImageView.setOnClickListener {
            showImagePickerDialog()
        }

        buttonExit.setOnClickListener {
            MainActivity.UserSession.clearSession(this)

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            finish()
        }

        buttonSettings.setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        buttonExplore.setOnClickListener{
            val intent = Intent(this, ExploreActivity::class.java)
            startActivity(intent)
        }

        buttonEvents.setOnClickListener{
            val intent = Intent(this, UserEventsActivity::class.java)
            startActivity(intent)
        }

        buttonProfile.setOnClickListener{
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        buttonProfile2.setOnClickListener{
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        imageButtonMenu.setOnClickListener{
            drawerLayout.openDrawer(GravityCompat.START)
        }

        nameText.setText(MainActivity.UserSession.username)
        descriptionText.setText(MainActivity.UserSession.description)
        FriendsRV.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        FriendsRV.layoutManager = GridLayoutManager(this, 4)

        loadFriends()

        nameText.isFocusable = false
        nameText.isClickable = false
        nameText.isFocusableInTouchMode = false
        nameText.setCursorVisible(false)

        descriptionText.isFocusable = false
        descriptionText.isClickable = false
        descriptionText.isFocusableInTouchMode = false
        descriptionText.setCursorVisible(false)

        profileImageView.isClickable = false
        profileImageView.isFocusable = false
        profileImageView.isEnabled = false

        add_friends.isClickable = false
        add_friends.isFocusable = false
        add_friends.isEnabled = false

        FriendsRV.setOnTouchListener { _, _ -> false }

        editProfile.setOnClickListener {
            if (!editing){
                editing = true
                iconEditProfile.isVisible = false
                editTextProfile.setText("Save")

                nameText.isFocusable = true
                nameText.isClickable = true
                nameText.isFocusableInTouchMode = true
                nameText.setCursorVisible(true)

                descriptionText.isFocusable = true
                descriptionText.isClickable = true
                descriptionText.isFocusableInTouchMode = true
                descriptionText.setCursorVisible(true)

                profileImageView.isClickable = true
                profileImageView.isFocusable = true
                profileImageView.isEnabled = true

                FriendsRV.setOnTouchListener { _, _ -> true }

                add_friends.isClickable = true
                add_friends.isFocusable = true
                add_friends.isEnabled = true

            } else {
                editing = false
                editTextProfile.setText("Edit Profile")

                iconEditProfile.isVisible = true
                nameText.isFocusable = false
                nameText.isClickable = false
                nameText.isFocusableInTouchMode = false
                nameText.setCursorVisible(false)

                descriptionText.isFocusable = false
                descriptionText.isClickable = false
                descriptionText.isFocusableInTouchMode = false
                descriptionText.setCursorVisible(false)

                profileImageView.isClickable = false
                profileImageView.isFocusable = false
                profileImageView.isEnabled = false

                FriendsRV.setOnTouchListener { _, _ -> false }

                add_friends.isClickable = false
                add_friends.isFocusable = false
                add_friends.isEnabled = false

                lifecycleScope.launch {
                    try {
                        val userId = MainActivity.UserSession.id ?: return@launch
                        val response = RetrofitClient.instance.updateDescription(userId, descriptionText.text.toString())

                        if (response.isSuccessful) {
                            MainActivity.UserSession.description = descriptionText.text.toString()
                        } else {
                            Log.e("API", "Error al actualizar descripción: ${response.code()}")
                        }
                    } catch (e: Exception) {
                        Log.e("API", "Excepción al actualizar descripción", e)
                    }
                }

                lifecycleScope.launch {
                    try {
                        val userId = MainActivity.UserSession.id ?: return@launch
                        val response = RetrofitClient.instance.updateName(userId, nameText.text.toString())

                        if (response.isSuccessful) {
                            MainActivity.UserSession.username = nameText.text.toString()
                        } else {
                            Log.e("API", "Error al actualizar descripción: ${response.code()}")
                        }
                    } catch (e: Exception) {
                        Log.e("API", "Excepción al actualizar descripción", e)
                    }
                }

                MainActivity.UserSession.description = descriptionText.text.toString()
                MainActivity.UserSession.username = nameText.text.toString()
                MainActivity.UserSession.saveSession(this@ProfileActivity)
                Toast.makeText(this@ProfileActivity, "Saved", Toast.LENGTH_SHORT).show()

            }
        }

        add_friends.setOnClickListener {
            showAddFriendDialog()
        }

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
                profileImageView.setImageURI(it) // Mostrar en el ImageView
                buttonProfile2.setImageURI(it)
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
                            this@ProfileActivity,
                            "Network error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }

                    when {
                        response.isSuccessful -> {
                            imageUrlNew = response.body()?.url ?: ""
                            Toast.makeText(
                                this@ProfileActivity,
                                "Imagen subida: $imageUrlNew",
                                Toast.LENGTH_LONG
                            ).show()
                            lifecycleScope.launch {
                                try {
                                    val userId = MainActivity.UserSession.id ?: return@launch
                                    val response = RetrofitClient.instance.updateImageUrl(userId, imageUrlNew)

                                    if (response.isSuccessful) {
                                        MainActivity.UserSession.imageUrl = imageUrlNew
                                        MainActivity.UserSession.saveSession(this@ProfileActivity)
                                        Toast.makeText(this@ProfileActivity, "Imagen actualizada", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Log.e("API", "Error al actualizar imagen: ${response.code()}")
                                    }
                                } catch (e: Exception) {
                                    Log.e("API", "Excepción al actualizar imagen", e)
                                }
                            }
                            MainActivity.UserSession.imageUrl = imageUrlNew
                            MainActivity.UserSession.saveSession(this@ProfileActivity)

                        }

                        response.code() == 400 -> {
                            val errorBody = response.errorBody()?.string()
                            Log.e("Upload Error", "400 Bad Request: $errorBody")
                            Toast.makeText(
                                this@ProfileActivity,
                                "Error del servidor: ${errorBody ?: "Formato inválido"}",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        else -> {
                            Toast.makeText(
                                this@ProfileActivity,
                                "Error ${response.code()}: ${response.message()}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ProfileActivity,
                    "Error: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showAddFriendDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Añadir amigo")

        val input = EditText(this)
        input.hint = "ID del amigo"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        builder.setPositiveButton("Añadir") { dialog, _ ->
            val friendIdStr = input.text.toString()
            val friendId = friendIdStr.toIntOrNull()

            if (friendId == null) {
                Toast.makeText(this, "ID inválido", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            addFriendApi(friendId)
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun addFriendApi(friendId: Int) {
        lifecycleScope.launch {
            try {
                val userId = MainActivity.UserSession.id ?: return@launch
                val request = AmicsRequest(usuari1_id = userId, usuari2_id = friendId)
                val response = RetrofitClient.instance.addFriend(request)

                if (response.isSuccessful) {
                    Toast.makeText(this@ProfileActivity, "Amigo añadido", Toast.LENGTH_SHORT).show()
                    // Opcional: recarga la lista de amigos aquí
                    loadFriends()
                } else {
                    Toast.makeText(this@ProfileActivity, "Error al añadir amigo: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

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
                findViewById<RecyclerView>(R.id.friendsRView).adapter = adapter
            }
        }
    }


}