package com.example.evencat_android.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.evencat_android.R

// Adaptador para mostrar una lista de eventos (Event2) en un RecyclerView
class Event2Adapter(private val events: List<Event2>) :
    RecyclerView.Adapter<Event2Adapter.ViewHolder>() {

    // Listener para manejar clics en un elemento de la lista
    private var onItemClickListener: ((Event2) -> Unit)? = null

    // Método para asignar el listener desde fuera del adaptador
    fun setOnItemClickListener(listener: (Event2) -> Unit) {
        onItemClickListener = listener
    }

    // ViewHolder que mantiene las referencias a las vistas de cada ítem
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventImage: ImageView = view.findViewById(R.id.eventImage)  // Imagen del evento
        val eventTitle: TextView = view.findViewById(R.id.eventTitle)  // Título del evento
        val eventDate: TextView = view.findViewById(R.id.eventDate)    // Fecha del evento
    }

    // Infla el layout para cada ítem y crea un ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event_2, parent, false)  // Layout del ítem
        return ViewHolder(view)
    }

    // Vincula los datos del evento con las vistas del ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = events[position]

        // Carga la imagen del evento con Glide en el ImageView
        Glide.with(holder.itemView.context)
            .load(event.imageResId)
            .into(holder.eventImage)

        // Establece el texto del título y fecha
        holder.eventTitle.text = event.title
        holder.eventDate.text = event.date

        // Configura el clic en el ítem para notificar al listener externo
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(event)
        }
    }

    // Devuelve la cantidad de eventos que contiene la lista
    override fun getItemCount() = events.size
}

// Data class que representa un evento con sus propiedades
data class Event2(
    val id: Int,              // Identificador único del evento
    val title: String,        // Título del evento
    val description: String,  // Descripción del evento
    val date: String,         // Fecha formateada para mostrar
    val imageResId: String,   // URL o recurso de la imagen del evento
    val organizerName: String,// Nombre del organizador
    val organizerId: Int,     // ID del organizador para posibles consultas
    val city: String,         // Ciudad donde se realiza el evento
    val location: String,     // Ubicación exacta del evento
    val rawDate: String,      // Fecha original sin formato (para detalles)
    val espaiId: Int          // ID del espacio o lugar del evento
)
