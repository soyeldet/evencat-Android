package com.example.evencat_android

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class User(
    @SerializedName("UserId") val id: Int,
    @SerializedName("UserName") val nombre: String,
    @SerializedName("UserEmail") val correo: String,
    @SerializedName("Password") val contrasena: String,
    @SerializedName("Rol") val rol: String,
    @SerializedName("Description") val descripcion: String,
    @SerializedName("ImageUrl") val image_url: String
): Serializable

data class UserLogin(
    @SerializedName("UserEmail") val correo: String,
    @SerializedName("Password") val contrasena: String
)

data class Organizer(
    @SerializedName("UserId") val id: String,
    @SerializedName("UserName") val nombre: String,
    @SerializedName("ImageUrl") val image_url: String
)

data class EventRequest(
    @SerializedName("nom") val nom: String,
    @SerializedName("descripcio") val descripcio: String,
    @SerializedName("data_hora") val dataHora: String, // en formato ISO 8601
    @SerializedName("espai_id") val espaiId: Int,
    @SerializedName("organitzador_id") val organitzadorId: Int
)

data class Event(
    @SerializedName("EventId") var id: Int,
    @SerializedName("Name") var name: String,
    @SerializedName("Description") val description: String,
    @SerializedName("Date") val date: String,
    @SerializedName("ImageUrl") val image_url: String,
    @SerializedName("State") val state: String,
    @SerializedName("EspaiId") var espai_id: Int,
    @SerializedName("OrganitzadorId") val organitzador_id: Int
)

data class Reservation(
    @SerializedName("reserva_id") val reservaId: Int, // ID de la reserva
    @SerializedName("event_id") val eventId: Int,   // ID del evento
    @SerializedName("butaca_id") val butacaId: Int?, // ID de la butaca (puede ser null si no está asignada)
    @SerializedName("data_reserva") val dataReserva: String // Fecha de la reserva (como String o Date)
)

data class ReservationRequest(
    @SerializedName("EventId") val eventoId: Int,
    @SerializedName("ButacaId") val butacaId: Int?,
    @SerializedName("FechaReserva") val fechaReserva: String,
    @SerializedName("UserId") val userId: Int
)

data class ReservaResponse(
    @SerializedName("ReservaId") val reservaId: Int,
    @SerializedName("EventoId") val eventoId: Int,
    @SerializedName("ButacaId") val butacaId: Int?,
    @SerializedName("FechaReserva") val fechaReserva: String,
)

data class Place(
    @SerializedName("espai_id")
    val espai_id: Int,
    @SerializedName("nom")
    val nom: String,
    @SerializedName("ubicacio")
    val ubicacio: String,
    @SerializedName("cadires_fixes")
    val cadires_fixes: Int?,
    @SerializedName("Butaques")
    val seats: List<Butaques>?
)

data class Butaques(
    @SerializedName("butaca_id")
    val butaca_id: Int,
    @SerializedName("fila")
    val fila: String,
    @SerializedName("numero")
    val numero: Int,
    @SerializedName("espai_id")
    val espai_id: Int
)

data class MessageResponse(
    val content: String?,
    val from: Int,
    val type: String = "message"
)

data class Message(
    val id: Int,
    val from: Int,
    val text: String?, // Null si es audio
    val audioBytes: ByteArray? = null, // Contiene los bytes del audio si es mensaje de audio
    val timestamp: String,
    val isSentByUser: Boolean,
    val isAudio: Boolean = false // Nuevo flag
)

data class SocketsDTO(
    val sender_id: Int,
    val chat_id: Int,
    val content: String?,
    val type: String = "message"
)

class SocketClient(
    private val userId: Int,
    private val chatId: Int,
    private val onMessageReceived: (String?, Int, Boolean, ByteArray?) -> Unit
)

data class UploadResponse(
    @SerializedName("message") val message: String,
    @SerializedName("fileName") val fileName: String,
    @SerializedName("url") val url: String
)

data class UserResponse(
    @SerializedName("usuari_id") val id: Int,
    @SerializedName("nom") val name: String,
)

data class AmicsRequest(
    @SerializedName("usuari1_id") val usuari1_id: Int,
    @SerializedName("usuari2_id") val usuari2_id: Int
)

data class AmicsResponse(
    @SerializedName("usuari_id") val id: Int,
    @SerializedName("usuari1_id") val usuari1_id: Int,
    @SerializedName("usuari2_id") val usuari2_id: Int
)

data class Seat(
    val id: Int,
    val seatNumber: String,
    var isAvailable: Boolean,
    var isSelected: Boolean = false
)










