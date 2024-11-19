package com.example.appstory.data.api

import com.example.appstory.data.request.LoginRequest
import com.example.appstory.data.request.RegisterRequest
import com.example.appstory.data.response.AddStoryResponse
import com.example.appstory.data.request.LoginResponse
import com.example.appstory.data.request.RegisterResponse
import com.example.appstory.data.response.ResetPasswordResponse
import com.example.appstory.data.response.StoryDetailResponse
import com.example.appstory.data.response.StoryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("register")
    suspend fun registerUser(
        @Body registerData: RegisterRequest
    ): Response<RegisterResponse>

    @POST("login")
    suspend fun loginUser(
        @Body loginData: LoginRequest
    ): Response<LoginResponse>

    @Multipart
    @POST("stories")
    suspend fun addStory(
        @Header("Authorization") token: String,
        @Part photo: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") lat: RequestBody? = null,
        @Part("lon") lon: RequestBody? = null
    ): Response<AddStoryResponse>

    @GET("stories")
    suspend fun getAllStories(
        @Header("Authorization") token: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("location") location: Int? = null
    ): Response<StoryResponse>

    @GET("stories/{id}")
    suspend fun getStoryDetail(
        @Header("Authorization") token: String,
        @Path("id") storyId: String
    ): Response<StoryDetailResponse>

    @POST("auth/reset-password")
    fun sendResetPasswordLink(@Body email: String): Call<ResetPasswordResponse>
}