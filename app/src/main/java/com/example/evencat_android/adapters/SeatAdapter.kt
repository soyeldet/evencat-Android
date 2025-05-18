package com.example.evencat_android.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.evencat_android.R
import com.example.evencat_android.Seat

// Adaptador para mostrar una lista de asientos en un RecyclerView
class SeatAdapter(
    private val seats: List<Seat>,                    // Lista de asientos
    private val onSeatClick: (Seat) -> Unit           // Callback para click en asiento
) : RecyclerView.Adapter<SeatAdapter.SeatViewHolder>() {

    // ViewHolder que contiene el botón del asiento
    inner class SeatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val seatButton: Button = view.findViewById(R.id.seat_button)
    }

    // Infla el layout para cada asiento
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_seat, parent, false)
        return SeatViewHolder(view)
    }

    // Asocia los datos del asiento a la vista y maneja el estado visual
    override fun onBindViewHolder(holder: SeatViewHolder, position: Int) {
        val seat = seats[position]

        // Mostrar número de asiento
        holder.seatButton.text = seat.seatNumber

        // Habilitar o deshabilitar según disponibilidad
        holder.seatButton.isEnabled = seat.isAvailable

        // Cambia el color del botón según el estado del asiento
        when {
            !seat.isAvailable -> holder.seatButton.setBackgroundColor(Color.RED)    // Ocupado
            seat.isSelected -> holder.seatButton.setBackgroundColor(Color.GREEN)     // Seleccionado
            else -> holder.seatButton.setBackgroundColor(Color.LTGRAY)               // Disponible
        }

        // Manejar click solo si el asiento está disponible
        holder.seatButton.setOnClickListener {
            if (seat.isAvailable) {
                onSeatClick(seat)
            }
        }
    }

    // Retorna la cantidad total de asientos
    override fun getItemCount(): Int = seats.size
}
