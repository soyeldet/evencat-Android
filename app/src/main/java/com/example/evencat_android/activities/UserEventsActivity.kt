package com.example.evencat_android.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
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

class UserEventsActivity : AppCompatActivity() {
    private lateinit var allEvents: MutableList<Event2>
    private lateinit var eventAdapter: Event2Adapter
    private lateinit var myEvents: RecyclerView
    private lateinit var image: ImageView
    private lateinit var switch: Switch

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
        myEvents = findViewById(R.id.my_events)
        image = findViewById(R.id.image_ucpoming_events)
        switch = findViewById(R.id.switch_my_events)

        val buttonExplore: Button = findViewById(R.id.explore_button_menu)
        val buttonEvents: Button = findViewById(R.id.events_button_menu)
        val buttonProfile: Button = findViewById(R.id.profile_button_menu)
        val buttonProfile2: CircleImageView = findViewById(R.id.profile_image_button)
        val buttonExit: Button = findViewById(R.id.exit)
        val username: TextView = findViewById(R.id.username)
        username.setText(MainActivity.UserSession.username.toString())
        val buttonSettings: Button = findViewById(R.id.settings)

        myEvents = findViewById(R.id.my_events)
        allEvents = mutableListOf()
        eventAdapter = Event2Adapter(allEvents)

        eventAdapter.setOnItemClickListener { event -> showEventDialog(event) }


        myEvents.layoutManager = LinearLayoutManager(this)
        myEvents.adapter = eventAdapter

        getMyEventsFromApi()

        switch.setOnCheckedChangeListener { _, isChecked ->
            getMyEventsFromApi(showPastEvents = isChecked)
        }

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

        buttonSettings.setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
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


    private fun getMyEventsFromApi(showPastEvents: Boolean = false) {
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.instance
                val userId = MainActivity.UserSession.id ?: return@launch

                val eventsResponse = apiService.getReservedEvents(userId)

                val reservedEvents = mutableListOf<Event2>()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                val now = System.currentTimeMillis()

                eventsResponse.forEach { event ->
                    val eventDate = dateFormat.parse(event.date ?: "")?.time ?: 0

                    if (showPastEvents && eventDate >= now) return@forEach  // Saltar eventos futuros
                    if (!showPastEvents && eventDate < now) return@forEach  // Saltar eventos pasados

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

                allEvents = reservedEvents.toMutableList()

                image.visibility = if (allEvents.isNotEmpty()) View.GONE else View.VISIBLE

                eventAdapter = Event2Adapter(allEvents).apply {
                    setOnItemClickListener { event -> showEventDialog(event) }
                }
                myEvents.adapter = eventAdapter

            } catch (e: Exception) {
                Toast.makeText(this@UserEventsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }



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
            "1st Jan - Mon - 12:00 AM"
        }
    }

        private fun getDayOfMonthSuffix(day: Int): String {
        return when {
            day in 11..13 -> "th"
            day % 10 == 1 -> "st"
            day % 10 == 2 -> "nd"
            day % 10 == 3 -> "rd"
            else -> "th"
        }
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

    private fun showEventDialog(event: Event2) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_event_details, null)

        val title = dialogView.findViewById<TextView>(R.id.text_event_title)
        val description = dialogView.findViewById<TextView>(R.id.text_event_description)
        val date = dialogView.findViewById<TextView>(R.id.text_event_date)
        val location = dialogView.findViewById<TextView>(R.id.text_event_location)
        val organizer = dialogView.findViewById<TextView>(R.id.text_event_organizer)
        val imageView = dialogView.findViewById<ImageView>(R.id.image_event)
        val closeButton = dialogView.findViewById<Button>(R.id.button_close_dialog)

        title.text = event.title
        description.text = event.description
        date.text = "Fecha: ${event.date}"
        location.text = "Ubicación: ${event.location}"
        organizer.text = "Organizador: ${event.organizerName}"

        // Generar y mostrar código QR con la ID del evento
        val qrText = MainActivity.UserSession.id.toString() + "-" + event.id.toString()

        val qrBitmap = generateQrCode(qrText.toString())
        if (qrBitmap != null) {
            imageView.setImageBitmap(qrBitmap)
        } else {
            imageView.setImageResource(R.drawable.sample_event) // fallback
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


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