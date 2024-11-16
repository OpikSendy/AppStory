package com.example.appstory.data.api

import android.content.Context
import com.example.appstory.data.model.MyApp
import com.example.appstory.data.model.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object Retrofit {

    private var instance: ApiService? = null

    @Synchronized
    fun getInstance(context: Context): ApiService {
        if (instance == null) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    val token = (context.applicationContext as MyApp)
                        .getSystemService(SessionManager::class.java)
                        .getAuthToken()

                    if (!token.isNullOrEmpty()) {
                        val originalRequest = chain.request()
                        val newRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer $token")
                            .build()
                        chain.proceed(newRequest)
                    } else {
                        chain.proceed(chain.request())
                    }
                }
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            instance = Retrofit.Builder()
                .baseUrl("https://story-api.dicoding.dev/v1/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
        return instance!!
    }
}
