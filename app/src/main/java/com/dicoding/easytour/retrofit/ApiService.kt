package com.dicoding.easytour.retrofit

import com.dicoding.easytour.request.FilterRequest
import com.dicoding.easytour.response.HomeResponse
import com.dicoding.easytour.response.CategoriesResponse
import com.dicoding.easytour.response.FilterResponse
import com.dicoding.easytour.response.LoginResponse
import com.dicoding.easytour.response.RecommendationResponse
import com.dicoding.easytour.response.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// Data class untuk request body kategori
data class EmailRequest(
    val email: String
)

data class IdRequest(
    val userID: String
)
data class PredictRequest(
    val place_name: String
)

// Data class untuk request body registrasi
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

// Data class untuk request body login
data class LoginRequest(
    val email: String,
    val password: String
)
data class CategoryRequest(
    val userID: String, // Change to String
    val TamanHiburan: Boolean,
    val Budaya: Boolean,
    val Bahari: Boolean,
    val CagarAlam: Boolean,
    val PusatPerbelanjaan: Boolean,
    val TempatIbadah: Boolean
)



interface ApiService {
    @POST("categories")
    suspend fun getCategories(
        @Body categoryRequest: CategoryRequest
    ): CategoriesResponse

    @POST("predict")
    suspend fun predict(
        @Body predict: PredictRequest
    ): RecommendationResponse

    @POST("home")
    suspend fun getHome(
        @Body idRequest: IdRequest
    ): HomeResponse

    @POST("register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("filter")
    suspend fun filter(
        @Body request: FilterRequest
    ): FilterResponse
}
