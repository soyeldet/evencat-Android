import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Build
import android.util.Base64
import android.util.Log
import com.example.evencat_android.MessageResponse
import com.example.evencat_android.SocketsDTO
import com.example.evencat_android.activities.BlowfishHelper
import com.example.evencat_android.activities.ChatActivity
import com.google.gson.Gson
import java.io.*
import java.net.Socket

class SocketClient(
    private val context: ChatActivity,
    private val userId: Int,
    private val chatId: Int,
    private val onMessageReceived: (String, Int, Boolean, ByteArray?) -> Unit
) {
    private var socket: Socket? = null
    private var writer: BufferedWriter? = null
    private var reader: BufferedReader? = null

    @SuppressLint("NewApi")
    fun connect() {
        Thread {
            try {
                socket = Socket("192.168.4.80", 6969)
                writer = BufferedWriter(OutputStreamWriter(socket!!.getOutputStream()))
                reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))

                val init = SocketsDTO(userId, chatId, null)
                writer?.write(Gson().toJson(init) + "\n")
                writer?.flush()

                Log.d("SocketClient", "Conexión establecida.")

                var line: String?
                while (reader?.readLine().also { line = it } != null) {
                    val json = line ?: continue
                    val obj = Gson().fromJson(json, MessageResponse::class.java)
                    val rawContent = obj.content ?: continue

                    when (obj.type) {
                        "message" -> {
                            var text = obj.content ?: ""
                            try {
                                text = BlowfishHelper.decryptMessage(text)
                                Log.d("SocketClient", "Mensaje listo para UI: $text")
                                context.runOnUiThread {
                                    onMessageReceived(text, obj.from ?: 0, false, null)
                                }
                            } catch (e: Exception) {
                                Log.e("SocketClient", "Error al desencriptar", e)
                            }
                        }
                        "audio" -> {
                            val audioBase64 = obj.content ?: ""
                            try {
                                val audioBytes = Base64.decode(audioBase64, Base64.DEFAULT)
                                Log.d("SocketClient", "Audio recibido, tamaño: ${audioBytes.size}")
                                onMessageReceived("", obj.from ?: 0, true, audioBytes)
                            } catch (e: Exception) {
                                Log.e("SocketClient", "Error decodificando audio base64: ${e.message}")
                            }
                        }
                        else -> {
                            Log.w("SocketClient", "Tipo de mensaje desconocido: ${obj.type}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SocketClient", "Error de conexión: ${e.message}")
            }
        }.start()
    }

    fun sendMessage(content: String) {
        Thread {
            try {
                val encryptedContent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    BlowfishHelper.encryptMessage(content)
                else content

                val dto = SocketsDTO(userId, chatId, encryptedContent, type = "message")
                val json = Gson().toJson(dto)
                writer?.write(json + "\n")
                writer?.flush()

                // Mostrar mensaje en la UI inmediatamente después de enviarlo
                context.runOnUiThread {
                    onMessageReceived(content, userId, false, null)
                }

            } catch (e: Exception) {
                Log.e("SocketClient", "Error al enviar mensaje: ${e.message}")
            }
        }.start()
    }


    fun sendAudio(audioBytes: ByteArray) {
        Thread {
            try {
                val base64Audio = Base64.encodeToString(audioBytes, Base64.NO_WRAP)
                val dto = SocketsDTO(userId, chatId, base64Audio, type = "audio")
                val json = Gson().toJson(dto)
                writer?.write(json + "\n")
                writer?.flush()
            } catch (e: Exception) {
                Log.e("SocketClient", "Error al enviar audio: ${e.message}")
            }
        }.start()
    }

    fun disconnect() {
        try {
            writer?.close()
            reader?.close()
            socket?.close()
        } catch (e: Exception) {
            Log.e("SocketClient", "Error al desconectar: ${e.message}")
        }
    }


}
