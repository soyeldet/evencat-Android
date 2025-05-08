package com.example.prueba_beat_on_jeans.api

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

    @GET("api/Usuarios/Musicos")
    fun getMusicos(): Call<List<UserRecievedWithDescription>>

    @GET("api/Usuarios/Locales")
    fun getLocales(): Call<List<UserRecievedWithDescription>>

    @POST("api/Usuarios")
    fun createUser(@Body user: User): Call<User>

    @GET("Api/MusicGenders/{userID}")
    suspend fun getMusicGenders(@Path("userID") userID: Int): MutableList<Tag>

    @PUT("api/GenerosUsuarios/ActualizarGeneros/{usuarioId}")
    suspend fun actualizarGenerosUsuario(
        @Path("usuarioId") usuarioId: Int,
        @Body generosIds: List<Int>
    ): Response<ResponseBody>

    @GET("api/GenerosUsuarios/ObtenerGeneros/{usuarioId}")
    suspend fun obtenerGenerosUsuario(
        @Path("usuarioId") usuarioId: Int
    ): Response<List<MusicalGender>>

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

    @GET("api/UsuarioMobils/{id}/Descripcion")
    suspend fun getDescription(@Path("id") userId: Int): Response<String>

    @GET("api/Usuarios/Matches_Music/{Ubicacion}/{userID}")
    suspend fun getMusicMatches(@Path("Ubicacion") Ubicacion: String, @Path("userID") userID: Int): MutableList<Matches>

    @POST("api/Matches/{Creador_ID}/{Finalizador_ID}")
    fun createNewMatch(@Path("Creador_ID") creador_id: Int, @Path("Finalizador_ID") finalizador_id: Int): Call<ResponseBody>

    @GET("api/Usuarios/Matches_Locales/{Ubicacion}/{userID}")
    suspend fun getLocalMatches(@Path("Ubicacion") Ubicacion: String, @Path("userID") userID: Int): MutableList<Matches>

    @GET("api/Matches/GetUserMatches/{userId}")
    fun getUserMatches(@Path("userId") userId: Int): Call<List<Match>>

    @PUT("api/Matches/{Creador_ID}/{Finalizador_ID}")
    fun updateMatchStatusToDislike(
        @Path("Creador_ID") creadorId: Int,
        @Path("Finalizador_ID") finalizadorId: Int
    ): Call<ResponseBody>

    @POST("api/Actuacions")
    suspend fun createNewEvent(@Body newEvent: Event): Call<ResponseBody>

    @GET("api/Actuacions/GetUpcomingNewActuacion/{creatorID}/{userID}")
    suspend fun getUpcomingNewActuacion(@Path("creatorID") creatorID: Int,@Path("userID") userID: Int): MutableList<Event>

    @PUT("api/Actuacions/CreateEvent/{event}")
    suspend fun createEvent(@Body event: Event): Event

    @DELETE("api/Actuacions/DeleteEvent/{event}")
    suspend fun deleteEvent(@Body event: Event): Call<ResponseBody>

    @GET("api/UsuarioMobils/{UserID}/Valoraciones")
    suspend fun obtainUserRating(@Path("UserID") userId: Int): Float?

    @GET("api/Valoraciones/isNewRatting/{userID}")
    suspend fun obtainIsNewRatting(@Path("userID") userId: Int): MutableList<Rating>?

    @PUT("api/Valoracions/{rating}")
    suspend fun setRatting(@Body rating: Rating): Response<ResponseBody>

    @GET("api/UsuarioMobils/Notificaiones/{userID}")
    suspend fun getUserLatestNotification(@Path("userID") userId: Int): Notification?

    @PUT("api/Usuarios/UpdateUser/{usuarioMobilId}")
    fun updateUser(
        @Path("usuarioMobilId") usuarioMobilId: Int,
        @Body usuarioRecibido: UserUpdated
    ): Call<String>

    @PUT("api/Soportes/{id}/Incidencia")
    fun crearIncidencia(
        @Path("id") usuarioId: Int,
        @Query("incidencia_id") incidenciaId: Int
    ): Call<Void>

}