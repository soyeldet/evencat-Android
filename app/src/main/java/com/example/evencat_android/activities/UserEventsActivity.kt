package com.example.evencat_android.activities

// Importaciones necesarias para la actividad
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.evencat_android.adapters.Event2
import com.example.evencat_android.adapters.Event2Adapter
import com.example.evencat_android.R
import com.example.evencat_android.RetrofitClient
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// Actividad para mostrar los eventos reservados por el usuario
class UserEventsActivity : AppCompatActivity() {

    // Declaración de variables de clase
    private lateinit var allEvents: MutableList<Event2>
    private lateinit var eventAdapter: Event2Adapter
    private lateinit var myEvents: RecyclerView
    private lateinit var image: ImageView
    private lateinit var switch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_events)

        // Permite que el contenido se extienda hasta los bordes
        enableEdgeToEdge()

        // Ajusta los márgenes del layout principal para evitar solapamientos con barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        // Inicialización de vistas del layout
        val imageButtonMenu: ImageButton = findViewById(R.id.menu_image_utton)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        myEvents = findViewById(R.id.my_events)
        image = findViewById(R.id.image_ucpoming_events)
        switch = findViewById(R.id.switch_my_events)

        // Botones del menú lateral
        val buttonExplore: Button = findViewById(R.id.explore_button_menu)
        val buttonEvents: Button = findViewById(R.id.events_button_menu)
        val buttonProfile: Button = findViewById(R.id.profile_button_menu)
        val buttonProfile2: CircleImageView = findViewById(R.id.profile_image_button)
        val buttonExit: Button = findViewById(R.id.exit)
        val username: TextView = findViewById(R.id.username)
        val buttonSettings: Button = findViewById(R.id.settings)

        // Muestra el nombre de usuario desde la sesión
        username.setText(MainActivity.UserSession.username.toString())

        // Carga la imagen del perfil usando Glide
        val imageUrl = MainActivity.UserSession.imageUrl
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.profile_p)
                .error(R.drawable.profile_p)
                .into(buttonProfile2)
        }

        // Inicializa adaptador y lista de eventos
        allEvents = mutableListOf()
        eventAdapter = Event2Adapter(allEvents)

        // Listener de clic sobre un evento de la lista
        eventAdapter.setOnItemClickListener { event -> showEventDialog(event) }

        // Configura el RecyclerView
        myEvents.layoutManager = LinearLayoutManager(this)
        myEvents.adapter = eventAdapter

        // Llama a la API para obtener los eventos reservados
        getMyEventsFromApi()

        // Cambia entre eventos pasados y futuros con el switch
        switch.setOnCheckedChangeListener { _, isChecked ->
            getMyEventsFromApi(showPastEvents = isChecked)
        }

        // Cierra sesión y vuelve al login
        buttonExit.setOnClickListener {
            MainActivity.UserSession.clearSession(this)
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Navega a otras actividades
        buttonExplore.setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }

        buttonSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        buttonEvents.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        buttonProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        buttonProfile2.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        imageButtonMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    // Función que obtiene los eventos reservados del usuario desde la API
    private fun getMyEventsFromApi(showPastEvents: Boolean = false) {
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.instance
                val userId = MainActivity.UserSession.id ?: return@launch

                val eventsResponse = apiService.getReservedEvents(userId)
                val reservedEvents = mutableListOf<Event2>()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                val now = System.currentTimeMillis()

                // Filtra eventos según si se muestran pasados o futuros
                eventsResponse.forEach { event ->
                    val eventDate = dateFormat.parse(event.date ?: "")?.time ?: 0
                    if (showPastEvents && eventDate >= now) return@forEach
                    if (!showPastEvents && eventDate < now) return@forEach

                    val espai = try { apiService.getEspais(event.espai_id) } catch (e: Exception) { null }
                    val organizerName = try { apiService.getOrganizer(event.organitzador_id).nombre } catch (e: Exception) { "Desconocido" }

                    val formattedDate = formatDateTime(event.date ?: "")
                    val ubicacion = espai?.ubicacio ?: ""
                    val parts = ubicacion.split(",", limit = 2)
                    val city = parts.getOrNull(0)?.trim() ?: "Desconocido"

                    reservedEvents.add(
                        Event2(
                            id = event.id,
                            title = event.name ?: "Sin título",
                            description = event.description ?: "Sin descripción",
                            date = formattedDate,
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

                // Actualiza la lista de eventos
                allEvents = reservedEvents.toMutableList()
                image.visibility = if (allEvents.isNotEmpty()) View.GONE else View.VISIBLE

                // Aplica el nuevo adaptador
                eventAdapter = Event2Adapter(allEvents).apply {
                    setOnItemClickListener { event -> showEventDialog(event) }
                }
                myEvents.adapter = eventAdapter

            } catch (e: Exception) {
                Toast.makeText(this@UserEventsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Da formato legible a la fecha del evento
    private fun formatDateTime(rawDate: String): String {
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
            "1st Jan - Mon - 12:00 AM" // fallback
        }
    }

    // Devuelve el sufijo ordinal del día (1st, 2nd, 3rd, etc.)
    private fun getDayOfMonthSuffix(day: Int): String {
        return when {
            day in 11..13 -> "th"
            day % 10 == 1 -> "st"
            day % 10 == 2 -> "nd"
            day % 10 == 3 -> "rd"
            else -> "th"
        }
    }

    // Abre detalles del evento en otra actividad (pantalla)
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

    // Muestra un diálogo con detalles del evento y un QR
    private fun showEventDialog(event: Event2) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_event_details, null)

        val title = dialogView.findViewById<TextView>(R.id.text_event_title)
        val description = dialogView.findViewById<TextView>(R.id.text_event_description)
        val date = dialogView.findViewById<TextView>(R.id.text_event_date)
        val location = dialogView.findViewById<TextView>(R.id.text_event_location)
        val organizer = dialogView.findViewById<TextView>(R.id.text_event_organizer)
        val imageView = dialogView.findViewById<ImageView>(R.id.image_event)
        val closeButton = dialogView.findViewById<Button>(R.id.button_close_dialog)
        val chatButton = dialogView.findViewById<Button>(R.id.button_chat)

        // Asigna información del evento al diálogo
        title.text = event.title
        description.text = event.description
        date.text = "Fecha: ${event.date}"
        location.text = "Ubicación: ${event.location}"
        organizer.text = "Organizador: ${event.organizerName}"

        // Genera un código QR a partir del ID de usuario y del evento
        val qrText = MainActivity.UserSession.id.toString() + "-" + event.id.toString()
        val qrBitmap = generateQrCode(qrText)
        if (qrBitmap != null) {
            imageView.setImageBitmap(qrBitmap)
        } else {
            imageView.setImageResource(R.drawable.sample_event)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Abre el chat relacionado con el evento
        chatButton.setOnClickListener {
            val intent = Intent(this@UserEventsActivity, ChatActivity::class.java)
            intent.putExtra("chat_id", event.id)
            startActivity(intent)
        }

        // Cierra el diálogo
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // Genera un código QR en forma de Bitmap
    private fun generateQrCode(text: String): Bitmap? {
        return try {
            val width = 400
            val height = 400
            val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
