package com.example.opennannyapp.api

import com.example.opennannyapp.data.DirObject
import com.example.opennannyapp.data.InfoObject
import com.example.opennannyapp.data.LedRequest
import com.example.opennannyapp.data.LedResponse
import com.example.opennannyapp.data.LoginRequest
import com.example.opennannyapp.data.LoginResponse
import com.example.opennannyapp.data.Mp3Object
import com.example.opennannyapp.data.MusicRequest
import com.example.opennannyapp.data.SongDefaultResponse
import com.example.opennannyapp.data.SongPlayResponse
import com.example.opennannyapp.data.SongStatusResponse
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("/sensors")
    suspend fun getSensors(): InfoObject

    @FormUrlEncoded
    @POST("/token")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): LoginResponse

    @POST("/led")
    suspend fun led(@Body user: LedRequest): LedResponse

    @POST("/music")
    suspend fun getDirs(@Body user: MusicRequest): DirObject

    @POST("/music")
    suspend fun getMp3(@Body user: MusicRequest): Mp3Object

    @POST("/music")
    suspend fun songPlay(@Body user: MusicRequest): SongPlayResponse

    @POST("/music")
    suspend fun songStop(@Body user: MusicRequest): SongDefaultResponse

    @POST("/music")
    suspend fun songPause(@Body user: MusicRequest): SongDefaultResponse

    @POST("/music")
    suspend fun songUnPause(@Body user: MusicRequest): SongDefaultResponse

    @POST("/music")
    suspend fun songRewind(@Body user: MusicRequest): SongDefaultResponse

    @POST("/music")
    suspend fun songSetVolume(@Body user: MusicRequest): SongDefaultResponse

    @POST("/music")
    suspend fun songStatus(@Body user: MusicRequest): SongStatusResponse
}

class TokenAuthenticator(
    private val apiService: ApiService,
    private val getCredentials: () -> LoginRequest,
    private val saveToken: (String) -> Unit
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Check if the response has already failed multiple times
        if (responseCount(response) >= 3) {
            return null // Give up after 3 attempts
        }

        return try {
            // Perform the login request to refresh the token
            val newToken = runBlocking {
                val credentials = getCredentials()
                val loginResponse = apiService.login(credentials.username, credentials.password)
                loginResponse.token
            }

            // Save the new token
            saveToken(newToken)

            // Retry the original request with the new token
            response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()

        } catch (e: Exception) {
            null // Failed to refresh the token
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 0
        var currentResponse: Response? = response
        while (currentResponse != null) {
            count++
            currentResponse = currentResponse.priorResponse
        }
        return count
    }
}

class NetworkModule(val api_ip: String, val api_user: String, val api_pass: String) {


    private var token = ""

    val serviceAPI = createApiService()

    fun saveToken(token: String) {
        // Implement token saving logic here
        this.token = token
    }


    fun createApiService(): ApiService {
        // Create an OkHttpClient with interceptors and authenticator
        val okHttpClient = OkHttpClient.Builder()
            .authenticator(TokenAuthenticator(
                apiService = Retrofit.Builder()
                    .baseUrl("https://$api_ip")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService::class.java),
                getCredentials = { LoginRequest(api_user, api_pass) },
                saveToken = { token -> this.saveToken(token) }
            ))
            .addInterceptor { chain ->
                val token = this.token
                val request = chain.request().newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
                chain.proceed(request)
            }
            .build()

        // Create and return the Retrofit instance with the configured OkHttpClient
        return Retrofit.Builder()
            .baseUrl("https://$api_ip")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun getToken(): String {
        return this.token
    }

}