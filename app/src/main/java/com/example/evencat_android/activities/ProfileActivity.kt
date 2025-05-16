package com.example.evencat_android.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.evencat_android.R
import com.example.evencat_android.RetrofitClient
import com.example.evencat_android.adapters.UserBubbleAdapter
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {
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
        val FriendsRV: RecyclerView = findViewById(R.id.friendsRView)
        val editProfile: Button = findViewById(R.id.editProfile)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val buttonSettings: Button = findViewById(R.id.settings)

        val buttonExplore: Button = findViewById(R.id.explore_button_menu)
        val buttonEvents: Button = findViewById(R.id.events_button_menu)
        val buttonProfile: Button = findViewById(R.id.profile_button_menu)
        val buttonProfile2: CircleImageView = findViewById(R.id.profile_image_button)
        val buttonExit: Button = findViewById(R.id.exit)
        val username: TextView = findViewById(R.id.username)
        username.setText(MainActivity.UserSession.username.toString())

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

        val userList = listOf("Anna", "Joan", "Marc", "Laia", "Pau")
        val adapter = UserBubbleAdapter(userList)
        FriendsRV.adapter = adapter

        nameText.isFocusable = false
        nameText.isClickable = false
        nameText.isFocusableInTouchMode = false
        nameText.setCursorVisible(false)

        descriptionText.isFocusable = false
        descriptionText.isClickable = false
        descriptionText.isFocusableInTouchMode = false
        descriptionText.setCursorVisible(false)

        FriendsRV.setOnTouchListener { _, _ -> true }

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
    }
}