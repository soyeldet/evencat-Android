package com.example.evencat_android.activities

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.evencat_android.R
import com.example.evencat_android.Seat

class SeatAdapter(
    private val seats: List<Seat>,
    private val onSeatClick: (Seat) -> Unit
) : RecyclerView.Adapter<SeatAdapter.SeatViewHolder>() {

    inner class SeatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val seatButton: Button = view.findViewById(R.id.seat_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_seat, parent, false)
        return SeatViewHolder(view)
    }

    override fun onBindViewHolder(holder: SeatViewHolder, position: Int) {
        val seat = seats[position]

        holder.seatButton.text = seat.seatNumber
        holder.seatButton.isEnabled = seat.isAvailable

        // Cambia el color según estado
        if (!seat.isAvailable) {
            holder.seatButton.setBackgroundColor(Color.RED) // ocupada
        } else if (seat.isSelected) {
            holder.seatButton.setBackgroundColor(Color.GREEN) // seleccionada
        } else {
            holder.seatButton.setBackgroundColor(Color.LTGRAY) // libre
        }

        holder.seatButton.setOnClickListener {
            if (seat.isAvailable) {
                onSeatClick(seat)
            }
        }
    }

    override fun getItemCount(): Int = seats.size
}
