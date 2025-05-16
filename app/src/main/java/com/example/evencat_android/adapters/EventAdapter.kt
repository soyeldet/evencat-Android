package com.example.evencat_android.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.evencat_android.R

class EventAdapter(private val events: List<Event>) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private var onItemClick: ((Event) -> Unit)? = null

    fun setOnItemClickListener(listener: (Event) -> Unit) {
        onItemClick = listener
    }

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image = view.findViewById<ImageView>(R.id.eventImage)
        val day = view.findViewById<TextView>(R.id.eventDay)
        val month = view.findViewById<TextView>(R.id.eventMonth)
        val title = view.findViewById<TextView>(R.id.eventTitle)
        val location = view.findViewById<TextView>(R.id.eventLocation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]

        // Cargar imagen desde URL
        Glide.with(holder.itemView.context)
            .load(event.imageUrl)
            .placeholder(R.drawable.sample_event)
            .error(R.drawable.sample_event) // Imagen de error si falla la carga
            .into(holder.image)

        holder.day.text = event.day
        holder.month.text = event.month
        holder.title.text = event.title
        holder.location.text = event.location

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(event)
        }
    }


    override fun getItemCount() = events.size

    data class Event(
        val id: Int,  // Añadir ID del evento
        val imageUrl: String,
        val day: String,
        val month: String,
        val title: String,
        val location: String,
        val description: String,  // Nuevo campo
        val rawDate: String,  // Nueva propiedad para fecha completa
        val espaiId: Int,
        val organizer: Int  // Nuevo campo
    )
}
