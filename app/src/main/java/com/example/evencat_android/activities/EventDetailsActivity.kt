package com.example.evencat_android.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils.formatDateTime
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.evencat_android.Event
import com.example.evencat_android.EventRequest
import com.example.evencat_android.R
import com.example.evencat_android.ReservationRequest
import com.example.evencat_android.RetrofitClient
import com.example.evencat_android.Seat
import com.example.evencat_android.activities.UserEventsActivity
import com.example.evencat_android.adapters.Event2
import com.example.evencat_android.adapters.Event2Adapter
import com.example.evencat_android.adapters.SeatAdapter
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.Date
import java.util.Locale

/**
 * Actividad que muestra los detalles de un evento y permite:
 * - Visualizar información del evento
 * - Crear nuevos eventos (modo creación)
 * - Reservar asientos para eventos existentes
 * - Acceder al chat del evento
 */
@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class EventDetailsActivity : AppCompatActivity() {
    // Views
    private lateinit var hourText: TextView
    private lateinit var dayText: TextView
    private lateinit var cityText: TextView
    private lateinit var organizerName: TextView
    private lateinit var locationText: TextView
    private lateinit var eventName: TextView
    private lateinit var eventDescription: TextView
    private lateinit var organizer_picture: CircleImageView

    // Variables de estado
    private var activeDialog: AlertDialog? = null
    private var espai: Int? = null
    private var event: Event? = null
    private var espaiId: Int? = null
    private var id: Int? = null
    private lateinit var adapter: SeatAdapter

    /**
     * Método llamado al crear la actividad
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_details)

        // Determinar si estamos en modo creación (1) o visualización (0)
        val creatingEvent = intent.getIntExtra("creatingEvent", 0)

        // Obtener referencias a los elementos de la UI
        val buyBackground: TextView = findViewById(R.id.backround_buy)
        val buyText: TextView = findViewById(R.id.text_buy)
        val buyButton: Button = findViewById(R.id.buy_button)
        val chatBackground: TextView = findViewById(R.id.chat_background)
        val chatIcon: ImageView = findViewById(R.id.chat_icon)
        val chatButton: Button = findViewById(R.id.chat_button)
        val dateButton: Button = findViewById(R.id.select_date_button)
        val cityButton: Button = findViewById(R.id.select_city_button)
        val backButton: Button = findViewById(R.id.back_button)
        val backgroundCreate: TextView = findViewById(R.id.backround_create)
        val textCreate: TextView = findViewById(R.id.text_create)
        val createButton: Button = findViewById(R.id.create_button)

        // Inicializar views de texto
        hourText = findViewById(R.id.hour_text)
        dayText = findViewById(R.id.day_text)
        cityText = findViewById(R.id.city_text)
        organizerName = findViewById(R.id.organizer_name)
        organizer_picture = findViewById(R.id.organizer_picture)
        locationText = findViewById(R.id.location_text)
        eventName = findViewById(R.id.event_name_editText)
        eventDescription = findViewById(R.id.event_description)

        // Configurar botón de retroceso
        backButton.setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }

        // Configurar botón de chat
        chatButton.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val userId = MainActivity.UserSession.id ?: return@launch
                    val eventId = id ?: return@launch

                    // Verificar si el usuario tiene reserva para este evento
                    val response = RetrofitClient.instance.checkReservation(userId, eventId)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body() == true) {
                            // Si tiene reserva, abrir el chat
                            val intent = Intent(this@EventDetailsActivity, ChatActivity::class.java)
                            intent.putExtra("chat_id", eventId)
                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                this@EventDetailsActivity,
                                "Necesitas una reserva para acceder al chat",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@EventDetailsActivity,
                            "Error al verificar la reserva",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        // Modo creación de evento
        if (creatingEvent == 1) {
            configureCreationMode(
                dateButton, cityButton,
                chatButton, chatIcon, chatBackground,
                buyText, buyButton, buyBackground,
                backgroundCreate, createButton, textCreate
            )
        }
        // Modo visualización de evento
        else {
            configureViewMode()

            // Mostrar elementos de compra
            buyBackground.visibility = View.VISIBLE
            buyButton.visibility = View.VISIBLE
            buyButton.isEnabled = true
            buyButton.isFocusable = true

            // Configurar botón de compra/reserva
            buyButton.setOnClickListener {
                handleSeatSelection()
            }
        }
    }

    /**
     * Maneja la selección de asientos para reservar
     */
    private fun handleSeatSelection() {
        lifecycleScope.launch {
            try {
                // Obtener asientos disponibles del servidor
                val response = RetrofitClient.instance.getAvailableSeats(id!!)
                if (response.isSuccessful) {
                    val seatResponses = response.body()

                    withContext(Dispatchers.Main) {
                        // Configurar diálogo de selección de asientos
                        val dialogView = layoutInflater.inflate(R.layout.dialog_seat_selection, null)
                        val recyclerSeats = dialogView.findViewById<RecyclerView>(R.id.recycler_seats)
                        val buttonConfirm = dialogView.findViewById<Button>(R.id.button_confirm_seat)

                        // Preparar lista de asientos
                        val seats = if (seatResponses.isNullOrEmpty() || (seatResponses.size == 1 && seatResponses[0] == 0)) {
                            // Caso especial: evento sin asientos asignados
                            mutableListOf(
                                Seat(
                                    id = 0,
                                    seatNumber = "Asiento aleatorio",
                                    isAvailable = true,
                                    isSelected = false
                                )
                            )
                        } else {
                            // Asientos normales
                            seatResponses.mapIndexed { index, seatId ->
                                Seat(
                                    id = seatId,
                                    seatNumber = "Butaca ${index + 1}",
                                    isAvailable = seatId != 0,
                                    isSelected = false
                                )
                            }.toMutableList()
                        }

                        // Configurar RecyclerView
                        val columnCount = if (seats.size == 1) 1 else 5
                        recyclerSeats.layoutManager = GridLayoutManager(this@EventDetailsActivity, columnCount)

                        adapter = SeatAdapter(seats) { seat ->
                            // Manejar selección de asiento
                            seats.forEach { it.isSelected = false }
                            seat.isSelected = true
                            adapter.notifyDataSetChanged()
                        }
                        recyclerSeats.adapter = adapter

                        // Mostrar diálogo
                        val dialog = AlertDialog.Builder(this@EventDetailsActivity)
                            .setView(dialogView)
                            .setCancelable(true)
                            .create()

                        // Configurar botón de confirmación
                        buttonConfirm.setOnClickListener {
                            val selectedSeat = seats.find { it.isSelected }
                            if (selectedSeat == null) {
                                Toast.makeText(this@EventDetailsActivity, "Selecciona una butaca", Toast.LENGTH_SHORT).show()
                                return@setOnClickListener
                            }

                            // Crear objeto de reserva
                            val currentDate = SimpleDateFormat(
                                "yyyy-MM-dd'T'HH:mm:ss",
                                Locale.getDefault()
                            ).format(Date())

                            val reserva = ReservationRequest(
                                eventoId = id!!,
                                butacaId = if (selectedSeat.id == 0) null else selectedSeat.id,
                                fechaReserva = currentDate,
                                userId = MainActivity.UserSession.id!!
                            )

                            // Enviar reserva al servidor
                            lifecycleScope.launch {
                                try {
                                    val reservaResponse = RetrofitClient.instance.createReservation(reserva)
                                    if (reservaResponse.isSuccessful) {
                                        Toast.makeText(this@EventDetailsActivity, "Reserva creada con éxito", Toast.LENGTH_LONG).show()
                                        Log.d(TAG, "Reserva exitosa: ${reservaResponse.body()}")
                                        dialog.dismiss()
                                    } else {
                                        Toast.makeText(this@EventDetailsActivity, "Este usuario ya tiene una reserva", Toast.LENGTH_SHORT).show()
                                        Log.e(TAG, "Respuesta error: ${reservaResponse.errorBody()?.string()}")
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Excepción al crear reserva: ${e.message}")
                                    Toast.makeText(this@EventDetailsActivity, "Fallo al enviar reserva", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        dialog.show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EventDetailsActivity, "Error al obtener asientos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Configura la actividad en modo creación de evento
     */
    private fun configureCreationMode(
        dateButton: Button, cityButton: Button,
        chatButton: Button, chatIcon: ImageView, chatBackground: TextView,
        buyText: TextView, buyButton: Button, buyBackground: TextView,
        backgroundCreate: TextView, createButton: Button, textCreate: TextView
    ) {
        // Habilitar edición de campos
        eventName.apply {
            isClickable = true
            isFocusableInTouchMode = true
        }

        eventDescription.apply {
            isClickable = true
            isFocusableInTouchMode = true
        }

        // Cargar imagen del organizador (usuario actual)
        val imageUrl = MainActivity.UserSession.imageUrl
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.profile_p)
                .error(R.drawable.profile_p)
                .into(organizer_picture)
        }

        // Habilitar botones de selección
        dateButton.isEnabled = true
        cityButton.isEnabled = true

        // Ocultar elementos no necesarios en modo creación
        chatButton.visibility = View.GONE
        chatIcon.visibility = View.GONE
        chatBackground.visibility = View.GONE
        buyText.visibility = View.GONE
        buyButton.visibility = View.GONE
        buyBackground.visibility = View.GONE

        // Mostrar elementos de creación
        backgroundCreate.visibility = View.VISIBLE
        createButton.visibility = View.VISIBLE
        textCreate.visibility = View.VISIBLE

        // Establecer nombre del organizador
        organizerName.text = MainActivity.UserSession.username

        // Configurar listeners
        dateButton.setOnClickListener {
            showDateTimePicker()
        }

        cityButton.setOnClickListener {
            showEspaiSelectionDialog()
        }

        // Centrar el fondo del chat (si es visible)
        (chatBackground.layoutParams as FrameLayout.LayoutParams).apply {
            gravity = Gravity.CENTER
            chatBackground.layoutParams = this
        }

        // Configurar edición de descripción
        eventDescription.setOnClickListener {
            showDescriptionEditDialog()
        }

        // Configurar edición de título
        eventName.setOnClickListener {
            showTitleEditDialog()
        }

        // Configurar botón de creación
        createButton.setOnClickListener {
            createEvent()
        }
    }

    /**
     * Muestra el selector de fecha y hora
     */
    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val dateText = "%02d/%02d/%04d".format(dayOfMonth, month + 1, year)
                dayText.text = dateText
                showTimePicker(calendar)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    /**
     * Muestra el selector de hora
     */
    @SuppressLint("SetTextI18n")
    private fun showTimePicker(calendar: Calendar) {
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                hourText.text = "%02d:%02d".format(hourOfDay, minute)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )

        timePickerDialog.show()
    }

    /**
     * Muestra diálogo para seleccionar un espacio (espai)
     */
    private fun showEspaiSelectionDialog() {
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.instance
                val espais = apiService.getEspais()

                val espaiNames = espais.map { it.nom }

                withContext(Dispatchers.Main) {
                    val spinner = Spinner(this@EventDetailsActivity).apply {
                        adapter = ArrayAdapter(
                            this@EventDetailsActivity,
                            android.R.layout.simple_spinner_item,
                            espaiNames
                        ).apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        }
                    }

                    if (activeDialog?.isShowing == true) return@withContext

                    activeDialog = AlertDialog.Builder(this@EventDetailsActivity)
                        .setTitle("Seleccionar Espai")
                        .setView(spinner)
                        .setPositiveButton("Aceptar") { dialog, _ ->
                            val selected = espais.find { it.nom == spinner.selectedItem.toString() }
                            selected?.let {
                                val parts = it.ubicacio.split(",", limit = 2)
                                cityText.text = parts.getOrNull(0)?.trim() ?: "Desconocido"
                                locationText.text = parts.getOrNull(1)?.trim() ?: "Desconocido"
                                espai = it.espai_id
                            } ?: showError("Espai no encontrado")
                            dialog.dismiss()
                            activeDialog = null
                        }
                        .setNegativeButton("Cancelar") { dialog, _ ->
                            dialog.dismiss()
                            activeDialog = null
                        }
                        .create()

                    activeDialog?.show()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@EventDetailsActivity,
                        "Error: ${e.message ?: "Desconocido"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /**
     * Muestra diálogo para editar la descripción
     */
    private fun showDescriptionEditDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_description_input, null)
        val inputField = dialogView.findViewById<EditText>(R.id.inputDescription)

        AlertDialog.Builder(this)
            .setTitle("Edit description")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                eventDescription.text = inputField.text.toString()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Muestra diálogo para editar el título
     */
    private fun showTitleEditDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_title_input, null)
        val inputTitle = dialogView.findViewById<EditText>(R.id.inputTitle)

        AlertDialog.Builder(this)
            .setTitle("Edit Title")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                eventName.text = inputTitle.text.toString()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Crea un nuevo evento con los datos ingresados
     */
    private fun createEvent() {
        val nom = eventName.text.toString()
        val descripcio = eventDescription.text.toString()
        val dataText = dayText.text.toString()
        val timeText = hourText.text.toString()
        val city = cityText.text.toString()
        val location = locationText.text.toString()

        // Validar campos obligatorios
        if (nom.isBlank() || descripcio.isBlank() || dataText.isBlank() || timeText.isBlank() || city.isBlank() || location.isBlank()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedDateTime = "$dataText $timeText"

        // Formatear fecha a ISO
        val isoDateTime = try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val outputFormat =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val parsedDate = inputFormat.parse(selectedDateTime)
            val formattedDate = outputFormat.format(parsedDate!!)

            // Validar que la fecha no sea anterior a la actual
            val currentDateTime = Calendar.getInstance().time
            if (parsedDate.before(currentDateTime)) {
                Toast.makeText(this, "La fecha debe ser posterior a la fecha actual", Toast.LENGTH_SHORT).show()
                return
            }

            formattedDate
        } catch (_: Exception) {
            Toast.makeText(this, "Fecha inválida", Toast.LENGTH_SHORT).show()
            return
        }

        // Enviar solicitud de creación al servidor
        lifecycleScope.launch {
            try {
                val eventRequest = MainActivity.UserSession.id?.let { userId ->
                    EventRequest(
                        nom = nom,
                        descripcio = descripcio,
                        dataHora = isoDateTime,
                        espaiId = espai!!,
                        organitzadorId = userId
                    )
                }

                val response = RetrofitClient.instance.createEvent(eventRequest!!)

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@EventDetailsActivity,
                        "Evento creado correctamente",
                        Toast.LENGTH_LONG
                    ).show()
                    startActivity(Intent(this@EventDetailsActivity, ExploreActivity::class.java))
                } else {
                    showError("Error al crear el evento")
                }
            } catch (e: Exception) {
                showError("Error: ${e.message}")
            }
        }
    }

    /**
     * Muestra un mensaje de error
     */
    private fun showError(message: String) {
        Toast.makeText(this@EventDetailsActivity, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Configura la actividad en modo visualización
     */
    @SuppressLint("SetTextI18n")
    private fun configureViewMode() {
        // Obtener datos del intent
        val title = intent.getStringExtra("title") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val rawDate = intent.getStringExtra("raw_date") ?: ""
        val location = intent.getStringExtra("location") ?: ""
        val organizer = intent.getIntExtra("organizer", 0)
        id = intent.getIntExtra("event_id", 0)
        espaiId = intent.getIntExtra("espai_id", 0)

        // Dividir ubicación en ciudad y dirección
        val parts = location.split(",", limit = 2)
        cityText.text = parts.getOrNull(0)?.trim() ?: "Desconocido"
        locationText.text = parts.getOrNull(1)?.trim() ?: "Desconocido"

        // Configurar textos
        eventName.text = title
        eventDescription.text = description

        // Cargar datos del organizador si es válido
        if (organizer != -1) {
            loadOrganizer(organizer)
        }

        // Formatear fecha
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(rawDate)

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            dayText.text = dateFormat.format(date)
            hourText.text = timeFormat.format(date)
        } catch (_: Exception) {
            dayText.text = "Fecha desconocida"
            hourText.text = "Hora desconocida"
        }

        // Ocultar elementos de edición
        findViewById<Button>(R.id.select_date_button).visibility = View.GONE
        findViewById<Button>(R.id.select_city_button).visibility = View.GONE
        findViewById<Button>(R.id.create_button).visibility = View.GONE
        findViewById<TextView>(R.id.backround_create).visibility = View.GONE

        // Deshabilitar edición
        eventName.isEnabled = false
        eventDescription.isEnabled = false
    }

    /**
     * Carga los datos del organizador desde el servidor
     */
    private fun loadOrganizer(organizerId: Int) {
        lifecycleScope.launch {
            try {
                val organizer = RetrofitClient.instance.getOrganizer(organizerId)
                organizerName.text = organizer.nombre
                val organizerPicture = organizer.image_url
                if (!organizerPicture.isNullOrEmpty()) {
                    Glide.with(this@EventDetailsActivity)
                        .load(organizerPicture)
                        .placeholder(R.drawable.profile_p)
                        .error(R.drawable.profile_p)
                        .into(organizer_picture)
                }
            } catch (_: Exception) {
                Toast.makeText(this@EventDetailsActivity, "Error cargando organizador", Toast.LENGTH_SHORT).show()
            }
        }
    }
}