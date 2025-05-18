// Declaración del paquete y importaciones necesarias
package com.example.evencat_android.activities

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
import com.example.evencat_android.adapters.Event2
import com.example.evencat_android.adapters.Event2Adapter
import com.example.evencat_android.R
import com.example.evencat_android.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Actividad para buscar eventos en la aplicación.
 * Permite buscar eventos por nombre y muestra los resultados en una lista.
 */
class SearchActivity : AppCompatActivity() {

    // Adaptador para el RecyclerView de eventos
    private lateinit var eventAdapter: Event2Adapter

    // Listas para almacenar todos los eventos y los eventos filtrados
    private var allEvents = mutableListOf<Event2>()
    private var filteredEvents = mutableListOf<Event2>()

    // Campo de texto para buscar eventos
    private lateinit var eventName : EditText

    /**
     * Método llamado cuando se crea la actividad.
     * Configura la interfaz de usuario y los listeners.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Establece el layout de la actividad
        setContentView(R.layout.activity_search)

        // Habilita el diseño edge-to-edge (bordes completos)
        enableEdgeToEdge()

        // Ajusta los márgenes para evitar superposiciones con la barra de sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        // Configuración del RecyclerView para mostrar los eventos
        val nearbyYouRV: RecyclerView = findViewById(R.id.nearbyYouRV)
        nearbyYouRV.layoutManager = LinearLayoutManager(this)

        // Obtener referencias a los elementos de la UI
        val backButton: ImageButton = findViewById(R.id.back_button)
        eventName = findViewById(R.id.event_name_editText)

        // Configurar el listener para el botón de retroceso
        backButton.setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }

        // Configurar el listener para el campo de búsqueda
        eventName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Filtrar eventos cuando cambia el texto
                filterEvents(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Obtener los eventos desde la API
        getEventsFromApi(nearbyYouRV)
    }

    /**
     * Obtiene los eventos desde la API y los muestra en el RecyclerView.
     * @param nearbyYouRV RecyclerView donde se mostrarán los eventos
     */
    private fun getEventsFromApi(nearbyYouRV: RecyclerView) {
        // Usar corrutinas para operaciones asíncronas
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.instance
                // Obtener la lista de eventos desde la API
                val eventsResponse = apiService.getEvents()
                val nearbyEvents = mutableListOf<Event2>()

                // Procesar cada evento obtenido
                eventsResponse.forEach { event ->
                    // Obtener información adicional del espacio y organizador
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

                    // Formatear la fecha del evento
                    val formattedDateNearby = formatDateTime(event.date ?: "")

                    // Procesar la ubicación para obtener la ciudad
                    val ubicacion = espai?.ubicacio ?: ""
                    val parts = ubicacion.split(",", limit = 2)
                    val city = parts.getOrNull(0)?.trim() ?: "Desconocido"

                    // Crear objeto Event2 con los datos procesados
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

                // Actualizar las listas de eventos
                allEvents = nearbyEvents.toMutableList()
                filteredEvents = allEvents.toMutableList()

                // Configurar el adaptador del RecyclerView
                eventAdapter = Event2Adapter(filteredEvents).apply {
                    setOnItemClickListener { event -> openEventDetails2(event) }
                }
                nearbyYouRV.adapter = eventAdapter

                // Re-aplicar la búsqueda actual después de cargar los datos
                val currentQuery = eventName.text.toString()
                filterEvents(currentQuery)

            } catch (e: Exception) {
                Toast.makeText(this@SearchActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Filtra los eventos según el texto de búsqueda.
     * @param query Texto de búsqueda
     */
    private fun filterEvents(query: String) {
        val trimmedQuery = query.trim()
        filteredEvents.clear()

        if (trimmedQuery.isEmpty()) {
            // Si no hay texto de búsqueda, mostrar todos los eventos
            filteredEvents.addAll(allEvents)
        } else {
            // Filtrar eventos cuyo título contenga el texto de búsqueda
            allEvents.forEach {
                if (it.title.trim().contains(trimmedQuery, ignoreCase = true)) {
                    filteredEvents.add(it)
                }
            }
        }

        // Notificar al adaptador que los datos han cambiado
        eventAdapter.notifyDataSetChanged()
    }

    /**
     * Abre la actividad de detalles de un evento.
     * @param event Evento seleccionado
     */
    private fun openEventDetails2(event: Event2) {
        val intent = Intent(this, EventDetailsActivity::class.java).apply {
            // Pasar todos los datos relevantes del evento
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

    /**
     * Formatea una fecha de texto a un formato más legible.
     * @param dateTime Fecha en formato "yyyy-MM-dd HH:mm:ss"
     * @return Fecha formateada como "dd MMM, HH:mm" o "Fecha desconocida" si hay error
     */
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