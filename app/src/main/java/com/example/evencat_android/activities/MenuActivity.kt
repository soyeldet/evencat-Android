package com.example.evencat_android.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.evencat_android.R
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Actividad principal del menú de la aplicación.
 * Desde aquí el usuario puede navegar a otras secciones como explorar, eventos, perfil y configuración.
 */
class MenuActivity : AppCompatActivity() {

    /**
     * Método que se ejecuta al crear la actividad.
     * Inicializa la interfaz de usuario y configura los listeners de los botones.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        /**
         * Habilita el soporte de pantalla completa con ajuste de los bordes.
         */
        enableEdgeToEdge()

        /**
         * Ajusta el padding de la vista principal para evitar solapamiento con las barras del sistema.
         */
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        // Referencias a elementos de la interfaz
        val buttonExplore: Button = findViewById(R.id.explore_button_menu)
        val buttonSettings: Button = findViewById(R.id.settings)
        val buttonEvents: Button = findViewById(R.id.events_button_menu)
        val buttonProfile: Button = findViewById(R.id.profile_button_menu)
        val buttonProfile2: CircleImageView = findViewById(R.id.profile_image_button)
        val buttonExit: Button = findViewById(R.id.exit)
        val username: TextView = findViewById(R.id.username)

        /**
         * Muestra el nombre de usuario obtenido de la sesión activa.
         */
        username.setText(MainActivity.UserSession.username.toString())

        /**
         * Carga la imagen de perfil del usuario desde la URL guardada en la sesión.
         * Si falla, se muestra una imagen por defecto.
         */
        val imageUrl = MainActivity.UserSession.imageUrl
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.profile_p)
                .error(R.drawable.profile_p)
                .into(buttonProfile2)
        }

        /**
         * Cierra la sesión del usuario y lo redirige a la pantalla principal.
         */
        buttonExit.setOnClickListener {
            MainActivity.UserSession.clearSession(this)

            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        /**
         * Abre la actividad de exploración de eventos.
         */
        buttonExplore.setOnClickListener {
            val intent = Intent(this, ExploreActivity::class.java)
            startActivity(intent)
        }

        /**
         * Abre la actividad de eventos del usuario.
         */
        buttonEvents.setOnClickListener {
            val intent = Intent(this, UserEventsActivity::class.java)
            startActivity(intent)
        }

        /**
         * Abre la actividad del perfil del usuario (botón de texto).
         */
        buttonProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        /**
         * Abre la actividad del perfil del usuario (botón de imagen).
         */
        buttonProfile2.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        /**
         * Abre la actividad de configuración.
         */
        buttonSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}
