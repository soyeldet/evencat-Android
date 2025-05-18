package com.example.evencat_android.activities

import com.example.evencat_android.classes.SocketClient
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
import com.example.evencat_android.adapters.ChatAdapter
import java.io.File
import java.io.FileOutputStream

/**
 * Actividad que maneja el chat entre usuarios.
 * Permite enviar mensajes de texto y grabaciones de audio.
 */
class ChatActivity : AppCompatActivity() {

    // Cliente para la conexión con el servidor de sockets
    private lateinit var socketClient: SocketClient

    // Adaptador para el RecyclerView que muestra los mensajes
    private lateinit var adapter: ChatAdapter

    // Lista que almacena los mensajes del chat
    private val messages = mutableListOf<Message>()

    // Objetos para la grabación de audio
    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecording = false

    /**
     * Método llamado cuando se crea la actividad.
     * Configura la interfaz de usuario y las funcionalidades del chat.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Solicitar permiso para grabar audio
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 0)

        // Obtener el ID del usuario y el ID del chat de los extras del Intent
        val userId = MainActivity.UserSession.id
        val chatId = intent.getIntExtra("chat_id", 0)

        // Obtener referencias a los elementos de la interfaz
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerMessages)
        val editText = findViewById<EditText>(R.id.editMessage)
        val sendButton = findViewById<ImageButton>(R.id.buttonSend)
        val audioButton = findViewById<ImageButton>(R.id.buttonRecord)
        val backButton = findViewById<ImageButton>(R.id.back_image_button)

        // Configurar el adaptador del RecyclerView
        adapter = ChatAdapter(messages)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Configurar el botón de retroceso para volver a ExploreActivity
        backButton.setOnClickListener {
            val intent = Intent(this, ExploreActivity::class.java)
            startActivity(intent)
        }

        // Inicializar el cliente de socket con un callback para manejar mensajes entrantes
        socketClient = SocketClient(
            this@ChatActivity,
            userId!!,
            chatId,
        ) { msg, from, isAudio, audioBytes ->
            runOnUiThread {
                // Crear un nuevo mensaje con los datos recibidos
                val message = Message(
                    id = 0,
                    from = from,
                    text = msg,
                    audioBytes = audioBytes,
                    timestamp = "now",
                    isSentByUser = (from == userId),
                    isAudio = isAudio
                )
                // Añadir el mensaje a la lista y notificar al adaptador
                messages.add(message)
                adapter.notifyItemInserted(messages.size - 1)
                recyclerView.scrollToPosition(messages.size - 1)
            }
        }

        // Conectar al servidor de sockets
        socketClient.connect()

        // Configurar el botón de enviar mensaje
        sendButton.setOnClickListener {
            val content = editText.text.toString()
            if (content.isNotBlank()) {
                // Enviar el mensaje a través del socket
                socketClient.sendMessage(content)
                editText.text.clear()
            }
        }

        // Configurar el botón de grabar audio
        audioButton.setOnClickListener {
            if (!isRecording) {
                startRecording()
                audioButton.setImageResource(R.drawable.boton_de_verificacion) // Cambiar a ícono de parar
            } else {
                stopRecording()
                audioButton.setImageResource(R.drawable.microfono) // Cambiar a ícono de grabar
            }
        }
    }

    /**
     * Inicia la grabación de audio.
     * Crea un archivo temporal y configura el MediaRecorder.
     */
    private fun startRecording() {
        isRecording = true
        // Crear archivo temporal para almacenar la grabación
        audioFile = File.createTempFile("audio_", ".m4a", cacheDir)

        // Configurar y preparar el MediaRecorder
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4) // Formato MPEG-4
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)    // Codificador AAC
            setOutputFile(audioFile!!.absolutePath)
            prepare()
            start()
        }
    }

    /**
     * Detiene la grabación de audio y envía el audio grabado a través del socket.
     */
    private fun stopRecording() {
        isRecording = false
        // Detener y liberar el MediaRecorder
        recorder?.apply {
            stop()
            release()
        }
        recorder = null

        // Leer los bytes del archivo de audio y enviarlos
        val audioBytes = audioFile!!.readBytes()
        socketClient.sendAudio(audioBytes)
    }

    /**
     * Método llamado cuando la actividad es destruida.
     * Se desconecta del servidor de sockets.
     */
    override fun onDestroy() {
        super.onDestroy()
        socketClient.disconnect()
    }

    /**
     * Reproduce un audio a partir de un array de bytes.
     *
     * @param audioBytes Bytes del audio a reproducir
     * @param context Contexto de la aplicación
     */
    fun playAudio(audioBytes: ByteArray, context: Context) {
        try {
            // Crear archivo temporal para reproducir el audio
            val tempFile = File.createTempFile("temp_audio", ".3gp", context.cacheDir)
            val fos = FileOutputStream(tempFile)
            fos.write(audioBytes)
            fos.close()

            // Configurar y preparar el MediaPlayer
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                prepare()
                start()
            }

            // Eliminar el archivo temporal cuando termine la reproducción
            mediaPlayer.setOnCompletionListener {
                tempFile.delete()
            }

        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error al reproducir audio: ${e.message}")
        }
    }
}