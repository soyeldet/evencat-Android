package com.example.evencat_android

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

data class Organizer(
    @SerializedName("UserId") val id: String,
    @SerializedName("UserName") val nombre: String
)

data class Place(
    @SerializedName("espai_id") val espai_id: Int,
    @SerializedName("nom") val nom: String,
    @SerializedName("ubicacio") val ubicacio: String,
    @SerializedName("cadires_fixes")val cadires_fixes: Int
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




