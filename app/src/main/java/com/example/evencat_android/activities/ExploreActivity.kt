package com.example.evencat_android.activities

import android.content.Intent
import java.text.SimpleDateFormat
import java.util.Locale
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.evencat_android.adapters.Event2
import com.example.evencat_android.adapters.Event2Adapter
import com.example.evencat_android.adapters.EventAdapter
import com.example.evencat_android.adapters.EventAdapter.Event
import com.example.evencat_android.R
import com.example.evencat_android.RetrofitClient
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch

class ExploreActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }


        val imageButtonMenu: ImageButton = findViewById(R.id.menu_image_utton)
        val upcomingEventsRV: RecyclerView = findViewById(R.id.upcomingEvents)
        val nearbyYouRV: RecyclerView = findViewById(R.id.nearbyYouRV)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val buttonSearch: Button = findViewById(R.id.button_search)
        val buttonAdd: Button = findViewById(R.id.button_add)
        val icAdd: TextView = findViewById(R.id.ic_add)
        val icSearch: ImageView = findViewById(R.id.ic_search)
        val seeAll: TextView = findViewById(R.id.see_all)


        val buttonExplore: Button = findViewById(R.id.explore_button_menu)
        val buttonEvents: Button = findViewById(R.id.events_button_menu)
        val buttonProfile: Button = findViewById(R.id.profile_button_menu)
        val buttonProfile2: CircleImageView = findViewById(R.id.profile_image_button)
        val buttonExit: Button = findViewById(R.id.exit)
        val username: TextView = findViewById(R.id.username)
        username.setText(MainActivity.UserSession.username.toString())
        val buttonSettings: Button = findViewById(R.id.settings)

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

        buttonExplore.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        buttonEvents.setOnClickListener {
            val intent = Intent(this, UserEventsActivity::class.java)
            startActivity(intent)
        }

        buttonProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        buttonProfile2.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        buttonSearch.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        seeAll.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        upcomingEventsRV.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        nearbyYouRV.layoutManager = LinearLayoutManager(this)

        getEventsFromApi(upcomingEventsRV, nearbyYouRV)

        imageButtonMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        if (MainActivity.UserSession.rol == "UsuariNormal") {
            icAdd.visibility = View.GONE
            icAdd.isEnabled = false
            buttonAdd.visibility = View.GONE
            buttonAdd.isEnabled = false

            val layoutParams = icSearch.layoutParams as FrameLayout.LayoutParams
            layoutParams.gravity = Gravity.RIGHT
            layoutParams.marginEnd = 45
            icSearch.layoutParams = layoutParams

            val layoutParams2 = buttonSearch.layoutParams as FrameLayout.LayoutParams
            layoutParams2.gravity = Gravity.RIGHT
            layoutParams2.marginEnd = 45
            buttonSearch.layoutParams = layoutParams2
        } else {
            buttonAdd.setOnClickListener {
                val intent = Intent(this, EventDetailsActivity::class.java)
                intent.putExtra("creatingEvent", 1)
                startActivity(intent)
            }
        }
    }

    private fun getEventsFromApi(upcomingEventsRV: RecyclerView, nearbyYouRV: RecyclerView) {
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.instance
                val eventsResponse = apiService.getEvents()

                // Listas separadas para cada tipo de evento
                val upcomingEvents = mutableListOf<Event>()
                val nearbyEvents = mutableListOf<Event2>()

                eventsResponse.forEach { event ->
                    // Datos comunes para ambos eventos
                    val espai = try {
                        apiService.getEspais(event.espai_id)
                    } catch (e: Exception) {
                        null
                    }

                    val organizerName = try {
                        apiService.getOrganizer(event.organitzador_id).nombre
                    } catch (e: Exception) {
                        "Desconocido"
                    }

                    // Procesar fecha para Event (Upcoming)
                    val formattedDateUpcoming = if (!event.date.isNullOrBlank()) {
                        try {
                            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                            val outputFormat = SimpleDateFormat("dd MMM", Locale.US)
                            val parsedDate = inputFormat.parse(event.date)
                            outputFormat.format(parsedDate)
                        } catch (e: Exception) {
                            "01 JAN"
                        }
                    } else {
                        "01 JAN"
                    }

                    val dayMonthParts = formattedDateUpcoming.split(" ")
                    val day = dayMonthParts.getOrNull(0) ?: "01"
                    val month = dayMonthParts.getOrNull(1) ?: "JAN"

                    // Crear objeto Event para Upcoming
                    upcomingEvents.add(
                        Event(
                            id = event.id,
                            imageUrl = event.image_url ?: "",
                            day = day,
                            month = month,
                            title = event.name ?: "Sin título",
                            location = espai?.ubicacio ?: "Sin ubicación",
                            description = event.description ?: "",
                            rawDate = event.date ?: "",
                            espaiId = event.espai_id ?: 0,
                            organizer = event.organitzador_id ?: 0
                        )
                    )

                    // Procesar fecha para Event2 (Nearby)
                    val formattedDateNearby = formatDateTime(event.date ?: "")

                    // Extraer ciudad y dirección
                    val ubicacion = espai?.ubicacio ?: ""
                    val parts = ubicacion.split(",", limit = 2)
                    val city = parts.getOrNull(0)?.trim() ?: "Desconocido"

                    // Crear objeto Event2 para Nearby
                    nearbyEvents.add(
                        Event2(
                            id = event.id,  // ¡Añadir ID!
                            title = event.name ?: "Sin título",
                            description = event.description ?: "Sin descripción",
                            date = formattedDateNearby,
                            imageResId = event.image_url ?: "",
                            organizerName = organizerName,
                            organizerId = event.organitzador_id ?: 0,
                            city = city,
                            location = espai?.ubicacio ?: "Sin ubicación",
                            espaiId = event.espai_id ?: 0,
                            rawDate = event.date ?: ""
                        )
                    )
                }

                // Configurar adaptadores
                upcomingEventsRV.adapter = EventAdapter(upcomingEvents).apply {
                    setOnItemClickListener { event -> openEventDetails(event) }
                }

                nearbyYouRV.adapter = Event2Adapter(nearbyEvents).apply {
                    setOnItemClickListener { event -> openEventDetails2(event) }
                }

            } catch (e: Exception) {
                Toast.makeText(this@ExploreActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun formatDateTime(rawDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val date = inputFormat.parse(rawDate)

            val day = SimpleDateFormat("d", Locale.US).format(date)
            val daySuffix = getDayOfMonthSuffix(day.toInt())
            val month = SimpleDateFormat("MMMM", Locale.US).format(date)
            val weekday = SimpleDateFormat("EEE", Locale.US).format(date)
            val time = SimpleDateFormat("h:mm a", Locale.US).format(date)

            "$day$daySuffix $month - $weekday - $time"
        } catch (e: Exception) {
            "1st Jan - Mon - 12:00 AM"
        }
    }

    fun getDayOfMonthSuffix(day: Int): String {
        return when {
            day in 11..13 -> "th"
            day % 10 == 1 -> "st"
            day % 10 == 2 -> "nd"
            day % 10 == 3 -> "rd"
            else -> "th"
        }
    }

    private fun openEventDetails(event: Event) {
        val intent = Intent(this, EventDetailsActivity::class.java).apply {
            putExtra("event_id", event.id)
            putExtra("title", event.title)
            putExtra("description", event.description)
            putExtra("raw_date", event.rawDate)
            putExtra("location", event.location)
            putExtra("organizer", event.organizer)
            putExtra("espai_id", event.espaiId)
        }
        startActivity(intent)
    }

    private fun openEventDetails2(event: Event2) {
        val intent = Intent(this, EventDetailsActivity::class.java).apply {
            putExtra("event_id", event.id)
            putExtra("espai_id", event.espaiId)
            putExtra("title", event.title)
            putExtra("description", event.description)
            putExtra("raw_date", event.rawDate)
            putExtra("formatted_date", event.date)
            putExtra("location", event.location)
            putExtra("city", event.city)
            putExtra("organizer", event.organizerId)
            putExtra("organizer_name", event.organizerName)
            putExtra("image_url", event.imageResId)
        }
        startActivity(intent)


    }

}