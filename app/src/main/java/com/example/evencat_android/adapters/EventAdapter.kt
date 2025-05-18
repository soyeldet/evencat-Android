package com.example.evencat_android.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.evencat_android.R

// Adaptador para mostrar una lista de eventos en un RecyclerView
class EventAdapter(private val events: List<Event>) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    // Listener para manejar clics en un evento
    private var onItemClick: ((Event) -> Unit)? = null

    // Método para asignar el listener externo
    fun setOnItemClickListener(listener: (Event) -> Unit) {
        onItemClick = listener
    }

    // ViewHolder que mantiene las vistas para cada elemento
    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image = view.findViewById<ImageView>(R.id.eventImage)       // Imagen del evento
        val day = view.findViewById<TextView>(R.id.eventDay)            // Día del evento
        val month = view.findViewById<TextView>(R.id.eventMonth)        // Mes del evento
        val title = view.findViewById<TextView>(R.id.eventTitle)        // Título del evento
        val location = view.findViewById<TextView>(R.id.eventLocation)  // Ubicación del evento
    }

    // Infla el layout para cada ítem y crea el ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    // Asocia los datos del evento a las vistas
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]

        // Carga la imagen con Glide, con placeholder y error por si falla la carga
        Glide.with(holder.itemView.context)
            .load(event.imageUrl)
            .placeholder(R.drawable.sample_event)
            .error(R.drawable.sample_event)
            .into(holder.image)

        // Setea los textos del día, mes, título y ubicación
        holder.day.text = event.day
        holder.month.text = event.month
        holder.title.text = event.title
        holder.location.text = event.location

        // Configura el clic para notificar al listener externo
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(event)
        }
    }

    // Devuelve el tamaño de la lista
    override fun getItemCount() = events.size

    // Data class que representa un evento con sus campos
    data class Event(
        val id: Int,              // ID único del evento
        val imageUrl: String,     // URL de la imagen del evento
        val day: String,          // Día para mostrar
        val month: String,        // Mes para mostrar
        val title: String,        // Título del evento
        val location: String,     // Ubicación
        val description: String,  // Descripción del evento
        val rawDate: String,      // Fecha completa sin formato
        val espaiId: Int,         // ID del espacio o lugar
        val organizer: Int        // ID del organizador
    )
}
