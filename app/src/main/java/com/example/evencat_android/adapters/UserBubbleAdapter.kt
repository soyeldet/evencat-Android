package com.example.evencat_android.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.evencat_android.R
import java.util.Random

class UserBubbleAdapter(private val userList: List<String>) :
    RecyclerView.Adapter<UserBubbleAdapter.UserViewHolder>() {

    class UserViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_bubble, parent, false) as TextView
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val userName = userList[position]
        holder.textView.text = userName

        // Generar un color aleatorio
        val rnd = Random()
        val color = Color.rgb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
        holder.textView.background.setTint(color)
    }

    override fun getItemCount() = userList.size
}