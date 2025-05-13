package com.example.evencat_android

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import de.hdodenhof.circleimageview.CircleImageView

class UserEventsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_events)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        val imageButtonMenu: ImageButton = findViewById(R.id.menu_image_utton)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)

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

        buttonExplore.setOnClickListener{
            val intent = Intent(this, ExploreActivity::class.java)
            startActivity(intent)
        }

        buttonEvents.setOnClickListener{
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        buttonProfile.setOnClickListener{
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        buttonProfile2.setOnClickListener{
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        imageButtonMenu.setOnClickListener{
            drawerLayout.openDrawer(GravityCompat.START)
        }

    }

}
