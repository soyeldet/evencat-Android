package com.example.evencat_android

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.set
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prueba_beat_on_jeans.api.RetrofitClient
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        var editing = false

        val nameText: EditText = findViewById(R.id.nameText)
        val descriptionText: EditText = findViewById(R.id.descriptionText)
        val editTextProfile: TextView = findViewById(R.id.editProfileText)
        val iconEditProfile: ImageView = findViewById(R.id.iconEditProfile)
        val FriendsRV: RecyclerView = findViewById(R.id.friendsRView)
        val editProfile: Button = findViewById(R.id.editProfile)

        nameText.setText(MainActivity.UserSession.username)
        descriptionText.setText(MainActivity.UserSession.descripcion)
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
                            MainActivity.UserSession.descripcion = descriptionText.text.toString()
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

                Toast.makeText(this@ProfileActivity, "Saved", Toast.LENGTH_SHORT).show()

            }
        }
    }
}