package com.example.evencat_android.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.evencat_android.R
import com.example.evencat_android.UserResponse
import java.util.Random

class UserBubbleAdapter(private val userList: List<UserResponse>) :
    RecyclerView.Adapter<UserBubbleAdapter.UserViewHolder>() {

    private val random = Random()
    private var onItemClick: ((UserResponse) -> Unit)? = null

    // Permite asignar un listener para clicks en los items
    fun setOnItemClickListener(listener: (UserResponse) -> Unit) {
        onItemClick = listener
    }

    // ViewHolder que contiene solo un TextView
    class UserViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView) {
        // Asigna el nombre del usuario y el color de fondo
        fun bind(user: UserResponse, color: Int) {
            textView.text = user.name
            textView.background.setTint(color)
        }
    }

    // Infla la vista que será un TextView (item_user_bubble)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_bubble, parent, false) as TextView
        return UserViewHolder(view)
    }

    // Vincula los datos con la vista y genera un color aleatorio para cada burbuja
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        val color = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256))

        holder.bind(user, color)
        holder.itemView.setOnClickListener { onItemClick?.invoke(user) }
    }

    override fun getItemCount() = userList.size
}
