package com.example.evencat_android.activities

import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.evencat_android.Message
import com.example.evencat_android.R
import java.io.File
import java.io.IOException

class ChatAdapter(private val messages: List<Message>) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutRes = if (viewType == 1) R.layout.item_message_sent else R.layout.item_message_received
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return MessageViewHolder(view)
    }

    inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.messageText)
        val audioButton: ImageButton = view.findViewById(R.id.audioPlayButton)
        var mediaPlayer: MediaPlayer? = null

        fun releaseMediaPlayer() {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        holder.releaseMediaPlayer()
        holder.audioButton.setImageResource(R.drawable.jugar)
        holder.audioButton.setOnClickListener(null)

        if (message.isAudio && message.audioBytes != null) {
            holder.textView.visibility = View.GONE
            holder.audioButton.visibility = View.VISIBLE

            holder.audioButton.setOnClickListener {
                val tempFile = File.createTempFile("audio_${System.currentTimeMillis()}", ".m4a", holder.itemView.context.cacheDir)
                tempFile.writeBytes(message.audioBytes)

                holder.mediaPlayer = MediaPlayer().apply {
                    setDataSource(tempFile.absolutePath)
                    setOnPreparedListener {
                        start()
                        holder.audioButton.setImageResource(R.drawable.ooooooooooooooooooooooooooooooooooooo)
                    }
                    setOnCompletionListener {
                        release()
                        holder.audioButton.setImageResource(R.drawable.jugar)
                        tempFile.delete()
                        holder.mediaPlayer = null
                    }
                    prepareAsync()
                }
            }

        } else {
            holder.audioButton.visibility = View.GONE
            holder.textView.visibility = View.VISIBLE
            holder.textView.text = message.text ?: ""
        }
    }

    override fun getItemViewType(position: Int): Int = if (messages[position].isSentByUser) 1 else 0
    override fun getItemCount(): Int = messages.size
}

