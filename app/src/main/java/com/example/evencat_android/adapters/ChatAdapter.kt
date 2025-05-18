package com.example.evencat_android.adapters

// Importaciones necesarias
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.evencat_android.Message
import com.example.evencat_android.R
import java.io.File

// Adaptador personalizado para mostrar mensajes en un RecyclerView (texto o audio)
class ChatAdapter(private val messages: List<Message>) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    // Crea y devuelve el ViewHolder adecuado según el tipo de mensaje (enviado o recibido)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        // Dependiendo de quién envía el mensaje, se infla un layout distinto
        val layoutRes = if (viewType == 1) R.layout.item_message_sent else R.layout.item_message_received
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return MessageViewHolder(view)
    }

    // ViewHolder interno que gestiona las vistas individuales de cada mensaje
    inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.messageText) // Texto del mensaje
        val audioButton: ImageButton = view.findViewById(R.id.audioPlayButton) // Botón para reproducir audio
        var mediaPlayer: MediaPlayer? = null // Reproductor de audio

        // Libera los recursos del MediaPlayer
        fun releaseMediaPlayer() {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // Enlaza los datos de un mensaje con su vista correspondiente
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        // Libera el reproductor anterior si lo hubiera
        holder.releaseMediaPlayer()
        holder.audioButton.setImageResource(R.drawable.jugar) // Icono por defecto del botón de audio
        holder.audioButton.setOnClickListener(null) // Limpia listener previo

        // Si el mensaje es de audio y contiene bytes de audio
        if (message.isAudio && message.audioBytes != null) {
            // Oculta el texto y muestra el botón de audio
            holder.textView.visibility = View.GONE
            holder.audioButton.visibility = View.VISIBLE

            // Configura el listener para reproducir el audio al hacer clic
            holder.audioButton.setOnClickListener {
                // Crea un archivo temporal para almacenar el audio
                val tempFile = File.createTempFile("audio_${System.currentTimeMillis()}", ".m4a", holder.itemView.context.cacheDir)
                tempFile.writeBytes(message.audioBytes)

                // Configura y prepara el MediaPlayer
                holder.mediaPlayer = MediaPlayer().apply {
                    setDataSource(tempFile.absolutePath)
                    setOnPreparedListener {
                        start() // Inicia la reproducción
                        holder.audioButton.setImageResource(R.drawable.ooooooooooooooooooooooooooooooooooooo) // Icono cuando está reproduciendo
                    }
                    setOnCompletionListener {
                        release() // Libera el reproductor al terminar
                        holder.audioButton.setImageResource(R.drawable.jugar) // Restaura el icono
                        tempFile.delete() // Borra el archivo temporal
                        holder.mediaPlayer = null
                    }
                    prepareAsync() // Prepara el audio de forma asíncrona
                }
            }

        } else {
            // Si no es audio, muestra el texto del mensaje
            holder.audioButton.visibility = View.GONE
            holder.textView.visibility = View.VISIBLE
            holder.textView.text = message.text ?: ""
        }
    }

    // Determina el tipo de vista según si el mensaje fue enviado por el usuario (1) o recibido (0)
    override fun getItemViewType(position: Int): Int = if (messages[position].isSentByUser) 1 else 0

    // Devuelve el número total de mensajes
    override fun getItemCount(): Int = messages.size
}
