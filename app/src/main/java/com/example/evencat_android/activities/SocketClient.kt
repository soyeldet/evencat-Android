import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.evencat_android.MessageResponse
import com.example.evencat_android.SocketsDTO
import com.example.evencat_android.activities.BlowfishHelper
import com.example.evencat_android.activities.BlowfishHelper.encryptMessage
import com.google.gson.Gson
import java.io.*
import java.net.Socket

class SocketClient(
    private val userId: Int,
    private val chatId: Int,
    private val onMessageReceived: (String, Int) -> Unit
) {
    private var socket: Socket? = null
    private var writer: BufferedWriter? = null
    private var reader: BufferedReader? = null

    fun connect() {
        Thread {
            try {
                socket = Socket("192.168.4.80", 6969)
                writer = BufferedWriter(OutputStreamWriter(socket!!.getOutputStream()))
                reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))

                // Enviar datos iniciales
                val init = SocketsDTO(userId, chatId, null)
                val initJson = Gson().toJson(init)
                writer?.write(initJson + "\n")
                writer?.flush()

                Log.d("SocketClient", "Conexión establecida y datos iniciales enviados.")

                // Leer mensajes
                var line: String?
                while (reader?.readLine().also { line = it } != null) {
                    val json = line ?: continue
                    Log.d("SocketClient", "Mensaje recibido (JSON): $json")

                    try {
                        val obj = Gson().fromJson(json, MessageResponse::class.java)
                        if (obj.type == "message") {
                            val encrypted = obj.content ?: ""
                            val decrypted = try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    BlowfishHelper.decryptMessage(encrypted)
                                } else {
                                    TODO("VERSION.SDK_INT < O")
                                }
                            } catch (e: Exception) {
                                Log.e("SocketClient", "Error al desencriptar mensaje: ${e.message}")
                                encrypted // fallback al texto original
                            }

                            Log.d("SocketClient", "Mensaje descifrado: $decrypted | De: ${obj.from}")
                            onMessageReceived(decrypted, obj.from)
                        }
                    } catch (e: Exception) {
                        Log.e("SocketClient", "Error al parsear JSON: $json\nExcepción: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("SocketClient", "Connection error: ${e.message}")
            }
        }.start()
    }


    fun sendMessage(content: String) {
        Thread {
            try {
                val dto = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    SocketsDTO(userId, chatId, encryptMessage(content))
                } else {
                }
                val json = Gson().toJson(dto)
                writer?.write(json + "\n")
                writer?.flush()
            } catch (e: Exception) {
                Log.e("SocketClient", "Send error: ${e.message}")
            }
        }.start()
    }

    fun disconnect() {
        try {
            writer?.close()
            reader?.close()
            socket?.close()
        } catch (e: Exception) {
            Log.e("SocketClient", "Disconnect error: ${e.message}")
        }
    }
}
