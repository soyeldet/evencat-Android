package com.example.prueba_beat_on_jeans.api

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class User(
    @SerializedName("UserId") val id: Int,
    @SerializedName("UserName") val nombre: String,
    @SerializedName("UserEmail") val correo: String,
    @SerializedName("Password") val contrasena: String,
    @SerializedName("Rol") val rol: String,
    @SerializedName("Description") val descripcion: String
): Serializable

data class UserLogin(
    @SerializedName("UserEmail") val correo: String,
    @SerializedName("Password") val contrasena: String
)

data class UserRecievedWithDescription(
    @SerializedName("ID") val id: Int,
    @SerializedName("Nombre") val nombre: String?,
    @SerializedName("Contrasena") val contrasena: String?,
    @SerializedName("Descripcion") val descripcion: String,
    @SerializedName("ValoracionTotal") val valoracion: Double?,
    @SerializedName("ROL_ID") val rolId: Int?,
    @SerializedName("Ubicacion")val ubicacion: String?,
    @SerializedName("Url_Imagen") val imagen: String,
)

data class UserUpdated(
    @SerializedName("ID") val ID: Int,
    @SerializedName("Nombre") var Nombre: String?,
    @SerializedName("Correo") var Correo: String?,
    @SerializedName("Contrasena") var Contrasena: String?,
    @SerializedName("Ubicacion") var Ubicacion: String?
)



data class ActualizarGenerosRequest(
    val usuarioId: Int,
    val generosIds: List<Int>
)

data class MusicalGender(
    @SerializedName("ID") val id: Int,
    @SerializedName("Nombre_Genero") val genero: String,
)

data class Tag(
    val id: Int,
    val genero: String,
)

data class MusicalGenders(val id: Int, val name: String)
val allGenders = listOf(
    MusicalGender(1, "Rock"),
    MusicalGender(2, "Pop"),
    MusicalGender(3, "Jazz"),
    MusicalGender(4, "Hip Hop"),
    MusicalGender(5, "Clásica"),
    MusicalGender(6, "Reggaeton"),
    MusicalGender(7, "Blues"),
    MusicalGender(8, "Folk"),
    MusicalGender(9, "Salsa"),
    MusicalGender(10, "Bachata"),
    MusicalGender(11, "Reggae"),
    MusicalGender(12, "Country"),
    MusicalGender(13, "Indie"),
    MusicalGender(14, "Punk"),
    MusicalGender(15, "Disco"),
    MusicalGender(16, "Heavy Metal"),
    MusicalGender(17, "Gospel"),
    MusicalGender(18, "R&B"),
    MusicalGender(19, "Electronica"),
    MusicalGender(20, "Techno"),
    MusicalGender(21, "K-pop"),
    MusicalGender(22, "Trap"),
    MusicalGender(23, "Ska"),
    MusicalGender(24, "Tango"),
    MusicalGender(25, "Flamenco"),
    MusicalGender(26, "Soul"),
    MusicalGender(27, "Psychedelic"),
    MusicalGender(28, "Metalcore"),
    MusicalGender(29, "Grunge"),
    MusicalGender(30, "Glam Rock")
)


data class Match(
    @SerializedName("ID") val id: Int,
    @SerializedName("CreadorId") val creador_id: Int,
    @SerializedName("FinalizadorId") val finalizador_id: Int,
    @SerializedName("Estado") val estado: Int,
)

data class Matches(
              @SerializedName("ID") val id: Int,
              @SerializedName("Nombre") val name: String,
              @SerializedName("Descripcion") val description: String,
              @SerializedName("Generos") val arrayTags: MutableList<String>,
              @SerializedName("Url_Imagen") val img: String): Serializable


data class Event(@SerializedName("ID") val id: Int? = null,
            @SerializedName("Fecha") val date: String,
            @SerializedName("Creador_ID") val creador_Id: Int,
            @SerializedName("Finalizador_ID") val finalizador_Id: Int,
            @SerializedName("Estado") var estado: Int): Serializable {
    constructor(date: String, creador_Id: Int, finalizador_Id: Int, estado: Int)
            : this(null,date,creador_Id,finalizador_Id,estado)
}

data class Rating(@SerializedName("ID") val id: Int?,
                  @SerializedName("Valor") var rating: Int,
                  @SerializedName("Valorado_ID") val rated:Int,
                  @SerializedName("Valorador_ID") val rater:Int): Serializable {
    constructor(rating: Int, rated: Int, rater: Int)
    : this(null,rating,rated,rater)
}

data class Notification(@SerializedName("Notificacion_ID") val id: Int?)

data class Support(
    @SerializedName("ID") val id: Int,
    @SerializedName("Tipo_Incidencia") val id_incidencia: Int?,
)