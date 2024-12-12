package com.dicoding.easytour.retrofit

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {
    private const val BASE_URL = "https://app-service-13797535012.asia-southeast2.run.app/"

    // Create the logging interceptor
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    // Function to get ApiService
    fun getApiService(context: Context): ApiService {
        // Create an OkHttpClient instance
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)  // Add logging interceptor
//            .addInterceptor(ChuckerInterceptor(context))  // Add Chucker interceptor
            .connectTimeout(30, TimeUnit.SECONDS)  // Set connection timeout (optional)
            .readTimeout(30, TimeUnit.SECONDS)  // Set read timeout (optional)
            .writeTimeout(30, TimeUnit.SECONDS)  // Set write timeout (optional)
            .build()

        // Create Retrofit instance using OkHttpClient
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)  // Use OkHttpClient with interceptors
            .addConverterFactory(GsonConverterFactory.create())  // Add Gson converter for JSON response
            .build()

        return retrofit.create(ApiService::class.java)  // Return ApiService instance
    }
}
