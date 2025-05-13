package com.example.evencat_android

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class SearchActivity : AppCompatActivity() {

    private lateinit var eventAdapter: Event2Adapter
    private var allEvents = mutableListOf<Event2>()
    private var filteredEvents = mutableListOf<Event2>()
    private lateinit var eventName : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        val nearbyYouRV: RecyclerView = findViewById(R.id.nearbyYouRV)
        nearbyYouRV.layoutManager = LinearLayoutManager(this)

        val backButton: ImageButton = findViewById(R.id.back_button)
        eventName = findViewById(R.id.event_name_editText)

        backButton.setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }

        eventName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterEvents(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        getEventsFromApi(nearbyYouRV)
    }

    private fun getEventsFromApi(nearbyYouRV: RecyclerView) {
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.instance
                val eventsResponse = apiService.getEvents()
                val nearbyEvents = mutableListOf<Event2>()

                eventsResponse.forEach { event ->
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

                    val formattedDateNearby = formatDateTime(event.date ?: "")
                    val ubicacion = espai?.ubicacio ?: ""
                    val parts = ubicacion.split(",", limit = 2)
                    val city = parts.getOrNull(0)?.trim() ?: "Desconocido"

                    nearbyEvents.add(
                        Event2(
                            id = event.id,
                            title = event.name ?: "Sin título",
                            description = event.description ?: "Sin descripción",
                            date = formattedDateNearby,
                            imageResId = event.image_url ?: "",
                            organizerName = organizerName,
                            organizerId = event.organitzador_id ?: 0,
                            city = city,
                            location = espai?.ubicacio ?: "Sin ubicación",
                            rawDate = event.date ?: "",
                            espaiId = espai?.espai_id ?: 0
                        )
                    )
                }

                allEvents = nearbyEvents.toMutableList()
                filteredEvents = allEvents.toMutableList()

                eventAdapter = Event2Adapter(filteredEvents).apply {
                    setOnItemClickListener { event -> openEventDetails2(event) }
                }
                nearbyYouRV.adapter = eventAdapter

                // Re-apply current search query after loading data
                val currentQuery = eventName.text.toString()
                filterEvents(currentQuery)

            } catch (e: Exception) {
                Toast.makeText(this@SearchActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun filterEvents(query: String) {
        val trimmedQuery = query.trim()
        filteredEvents.clear()

        if (trimmedQuery.isEmpty()) {
            filteredEvents.addAll(allEvents)
        } else {
            allEvents.forEach {
                if (it.title.trim().contains(trimmedQuery, ignoreCase = true)) {
                    filteredEvents.add(it)
                }
            }
        }

        eventAdapter.notifyDataSetChanged()
    }

    private fun openEventDetails2(event: Event2) {
        val intent = Intent(this, EventDetailsActivity::class.java).apply {
            putExtra("event_id", event.id)
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

    private fun formatDateTime(dateTime: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val outputFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.US)
            val parsedDate = inputFormat.parse(dateTime)
            outputFormat.format(parsedDate)
        } catch (e: Exception) {
            "Fecha desconocida"
        }
    }
}