package com.example.evencat_android.activities

import SocketClient
import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.evencat_android.Message
import com.example.evencat_android.R
import java.io.File
import java.io.FileOutputStream

class ChatActivity : AppCompatActivity() {

    private lateinit var socketClient: SocketClient
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<Message>()

    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 0)

        val userId = MainActivity.UserSession.id
        val chatId = intent.getIntExtra("chat_id", 0)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerMessages)
        val editText = findViewById<EditText>(R.id.editMessage)
        val sendButton = findViewById<ImageButton>(R.id.buttonSend)
        val audioButton = findViewById<ImageButton>(R.id.buttonRecord)
        val backButton = findViewById<ImageButton>(R.id.back_image_button)

        adapter = ChatAdapter(messages)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        backButton.setOnClickListener {
            val intent = Intent(this, ExploreActivity::class.java)
            startActivity(intent)
        }

        socketClient = SocketClient(
            this@ChatActivity,
            userId!!,
            chatId,
        ) { msg, from, isAudio, audioBytes ->
            runOnUiThread {
                val message = Message(
                    id = 0,
                    from = from,
                    text = msg,
                    audioBytes = audioBytes,
                    timestamp = "now",
                    isSentByUser = (from == userId),
                    isAudio = isAudio
                )
                messages.add(message)
                adapter.notifyItemInserted(messages.size - 1)
                recyclerView.scrollToPosition(messages.size - 1)

            }
        }

        socketClient.connect()

        sendButton.setOnClickListener {
            val content = editText.text.toString()
            if (content.isNotBlank()) {
                socketClient.sendMessage(content)
                editText.text.clear()
            }
        }

        audioButton.setOnClickListener {
            if (!isRecording) {
                startRecording()
                audioButton.setImageResource(R.drawable.boton_de_verificacion) // ícono de parar
            } else {
                stopRecording()
                audioButton.setImageResource(R.drawable.microfono) // ícono de grabar
            }
        }
    }

    private fun startRecording() {
        isRecording = true
        audioFile = File.createTempFile("audio_", ".m4a", cacheDir)

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4) // Cambiado a MPEG_4
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)    // Cambiado a AAC
            setOutputFile(audioFile!!.absolutePath)
            prepare()
            start()
        }
    }

    private fun stopRecording() {
        isRecording = false
        recorder?.apply {
            stop()
            release()
        }
        recorder = null

        val audioBytes = audioFile!!.readBytes()
        socketClient.sendAudio(audioBytes)
    }

    override fun onDestroy() {
        super.onDestroy()
        socketClient.disconnect()
    }

    fun playAudio(audioBytes: ByteArray, context: Context) {
        try {
            // Crear archivo temporal
            val tempFile = File.createTempFile("temp_audio", ".3gp", context.cacheDir)
            val fos = FileOutputStream(tempFile)
            fos.write(audioBytes)
            fos.close()

            // Usar MediaPlayer para reproducir
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                prepare()
                start()
            }

            mediaPlayer.setOnCompletionListener {
                tempFile.delete() // Limpieza después de reproducir
            }

        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error al reproducir audio: ${e.message}")
        }
    }


}
