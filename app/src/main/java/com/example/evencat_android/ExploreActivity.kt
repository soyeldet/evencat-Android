package com.example.evencat_android

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ExploreActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore)

        val imageButtonMenu: ImageButton = findViewById(R.id.menu_image_utton)

        imageButtonMenu.setOnClickListener{
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
        }

        imageButtonMenu.setOnClickListener{
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
        }
    }
}