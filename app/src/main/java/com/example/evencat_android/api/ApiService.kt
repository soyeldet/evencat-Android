package com.example.evencat_android.api

import com.example.evencat_android.AmicsRequest
import com.example.evencat_android.AmicsResponse
import com.example.evencat_android.Event
import com.example.evencat_android.EventRequest
import com.example.evencat_android.Organizer
import com.example.evencat_android.Place
import com.example.evencat_android.ReservaResponse
import com.example.evencat_android.ReservationRequest
import com.example.evencat_android.UploadResponse
import com.example.evencat_android.User
import com.example.evencat_android.UserLogin
import com.example.evencat_android.UserResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiService {

    @GET("Api/Usuarios")
    suspend fun getUsers(): List<User>

    @GET("Api/Usuarios/{userID}")
    suspend fun getUser(@Path("userID") userID: Int): User

    @Multipart
    @POST("api/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>

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

    @POST("api/Reserves/CreateWithDetails")
    suspend fun createReservation(@Body request: ReservationRequest): Response<ReservaResponse>

    @GET("api/Reserves/AvailableSeats/{eventId}")
    suspend fun getAvailableSeats(@Path("eventId") eventId: Int): Response<List<Int>> // <-- Lista de IDs

    @GET("api/Esdeveniments/Reservats/{usuariId}")
    suspend fun getReservedEvents(@Path("usuariId") usuariId: Int): List<Event>

    @PUT("api/Usuaris/{id}/UpdateProfile")
    fun updateUser(@Path("id") id: Int, @Body user: User): Call<ResponseBody>

    @PUT("api/Usuaris/{id}/ImageUrl")
    suspend fun updateImageUrl(@Path("id") userId: Int, @Body imageUrl: String): Response<Void>

    @GET("api/reservations/check")
    suspend fun checkReservation(
        @Query("userId") userId: Int,
        @Query("eventId") eventId: Int
    ): Response<Boolean>

    @GET("api/friends/{userId}")
    suspend fun getFriends(@Path("userId") userId: Int): Response<List<UserResponse>>

    @POST("api/Amics")
    suspend fun addFriend(@Body amicsRequest: AmicsRequest): Response<AmicsResponse>

}