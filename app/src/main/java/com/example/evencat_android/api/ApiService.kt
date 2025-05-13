package com.example.evencat_android.api

import com.example.evencat_android.Event
import com.example.evencat_android.EventRequest
import com.example.evencat_android.Organizer
import com.example.evencat_android.Place
import com.example.evencat_android.User
import com.example.evencat_android.UserLogin
import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @GET("Api/Usuarios")
    suspend fun getUsers(): List<User>

    @GET("Api/Usuarios/{userID}")
    suspend fun getUser(@Path("userID") userID: Int): User

    @Multipart
    @POST("api/upload")
    fun uploadImage(
        @Part image: MultipartBody.Part,
    ): Call<ResponseBody>

    @POST("api/Usuaris")
    fun registerUser(@Body user: User): Call<ResponseBody>

    @POST("api/Usuaris/Login")
    fun loginUser(@Body user: UserLogin): Call<User>

    @PUT("api/Usuaris/{id}/Descripcio")
    suspend fun updateDescription(
        @Path("id") id: Int,
        @Body description: String
    ): Response<Unit>

    @PUT("api/Usuaris/{id}/Nom")
    suspend fun updateName(
        @Path("id") id: Int,
        @Body name: String
    ): Response<Unit>

    @GET("api/Espais")
    suspend fun getEspais(): List<Place>

    @GET("api/Espais/{id}")
    suspend fun getEspais(
        @Path("id") id: Int
    ): Place

    @POST("api/Esdeveniments")
    suspend fun createEvent(@Body event: EventRequest): Response<Unit>

    @GET("api/Esdeveniments")
    suspend fun getEvents(): List<Event>

    @GET("api/Usuaris/organizer/{id}")
    suspend fun getOrganizer(@Path("id") id: Int): Organizer


}