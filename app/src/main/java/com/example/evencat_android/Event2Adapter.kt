package com.example.evencat_android

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

// Versión corregida del adaptador:
class Event2Adapter(private val events: List<Event2>) :
    RecyclerView.Adapter<Event2Adapter.ViewHolder>() {

    private var onItemClickListener: ((Event2) -> Unit)? = null

    fun setOnItemClickListener(listener: (Event2) -> Unit) {
        onItemClickListener = listener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventImage: ImageView = view.findViewById(R.id.eventImage)
        val eventTitle: TextView = view.findViewById(R.id.eventTitle)
        val eventDate: TextView = view.findViewById(R.id.eventDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = events[position]

        // Cargar imagen usando una librería como Glide o Picasso
        Glide.with(holder.itemView.context)
            .load(event.imageResId)
            .into(holder.eventImage)

        holder.eventTitle.text = event.title
        holder.eventDate.text = event.date

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(event) // Notificar al listener externo
        }
    }

    override fun getItemCount() = events.size
}

// Data class debe estar fuera del adaptador
data class Event2(
    val id: Int,  // Necesario para identificar el evento
    val title: String,
    val description: String,
    val date: String,
    val imageResId: String,
    val organizerName: String,
    val organizerId: Int,  // Para posibles consultas al organizador
    val city: String,
    val location: String,
    val rawDate: String,  // Fecha original para detalles
    val espaiId: Int
)