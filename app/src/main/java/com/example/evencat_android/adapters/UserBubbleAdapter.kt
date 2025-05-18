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

    // Para manejar clics si es necesario
    fun setOnItemClickListener(listener: (UserResponse) -> Unit) {
        onItemClick = listener

    }

    class UserViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView) {
        fun bind(user: UserResponse, color: Int) {
            textView.text = user.name
            textView.background.setTint(color)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_bubble, parent, false) as TextView
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        val color = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256))

        holder.bind(user, color)
        holder.itemView.setOnClickListener { onItemClick?.invoke(user) }
    }

    override fun getItemCount() = userList.size

}