package com.example.appstory.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.appstory.data.api.ApiService
import com.example.appstory.data.model.StoryDao
import com.example.appstory.data.model.StoryEntity
import com.example.appstory.data.model.toStoryEntity
import com.example.appstory.data.request.LoginRequest
import com.example.appstory.data.request.RegisterRequest
import com.example.appstory.data.response.AddStoryResponse
import com.example.appstory.data.request.LoginResponse
import com.example.appstory.data.request.RegisterResponse
import com.example.appstory.utils.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class StoryRepository @Inject constructor(
    private val apiService: ApiService,
    private val storyDao: StoryDao
) {

    suspend fun registerUser(registerData: RegisterRequest): Resource<RegisterResponse> {
        Log.d("Register Data", "Name: ${registerData.name}, Email: ${registerData.email}, Password: ${registerData.password}")
        return safeApiCall { apiService.registerUser(registerData) }
    }

    suspend fun loginUser(loginData: LoginRequest): Resource<LoginResponse> {
        return safeApiCall { apiService.loginUser(loginData) }
    }

    suspend fun addStory(
        token: String,
        photo: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody? = null,
        lon: RequestBody? = null
    ): Resource<AddStoryResponse> {
        return try {
            val response = apiService.addStory("Bearer $token", photo, description, lat, lon)
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("AddStory", "Response: $body")
                body?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Response body is null")
            } else {
                Log.d("AddStory", "Error response: ${response.message()}")
                Resource.Error("Error: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to add story: ${e.message}")
        }
    }


    fun getAllStories(token: String, page: Int, size: Int): LiveData<Resource<List<StoryEntity>>> {
        val result = MediatorLiveData<Resource<List<StoryEntity>>>()
        result.value = Resource.Loading()

        CoroutineScope(Dispatchers.IO).launch {
            val apiResult = safeApiCall { apiService.getAllStories("Bearer $token", page, size) }

            when (apiResult) {
                is Resource.Success -> {
                    val stories = apiResult.data?.listStory?.map { it.toStoryEntity() } ?: emptyList()

                    if (stories.isNotEmpty()) {
                        storyDao.clearAllStories()
                        storyDao.insertStories(stories)
                    }

                    withContext(Dispatchers.Main) {
                        result.value = Resource.Success(stories)
                    }
                }
                is Resource.Error -> {
                    val cachedStories = storyDao.getAllStories().firstOrNull()
                    withContext(Dispatchers.Main) {
                        if (!cachedStories.isNullOrEmpty()) {
                            result.value = Resource.Success(cachedStories)
                        } else {
                            result.value = Resource.Error("Failed to load stories from API and no cached data available")
                        }
                    }
                }
                else -> {
                    withContext(Dispatchers.Main) {
                        result.value = Resource.Error("Unknown error occurred")
                    }
                }
            }
        }
        return result
    }

    suspend fun getStoryDetail(token: String, storyId: String): Resource<StoryEntity> {
        return try {
            val response = apiService.getStoryDetail("Bearer $token", storyId)
            Log.d("Repository", "Raw Response: ${response.body()}")

            if (response.isSuccessful) {
                val storyDetailResponse = response.body()
                if (storyDetailResponse?.error == false) {
                    storyDetailResponse.story?.let { story ->
                        Log.d("Repository", "Story data received: $story")
                        Resource.Success(story.toStoryEntity())
                    } ?: run {
                        Log.e("Repository", "Story data is null")
                        Resource.Error("Story data is empty")
                    }
                } else {
                    Log.e("Repository", "API error: ${storyDetailResponse?.message}")
                    Resource.Error(storyDetailResponse?.message ?: "Unknown error occurred")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("Repository", "Error response: $errorBody")
                Resource.Error("Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("Repository", "Exception: ${e.message}", e)
            when (e) {
                is IOException -> Resource.Error("Network error: Please check your internet connection")
                else -> Resource.Error("Error: ${e.message}")
            }
        }
    }


    private suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Resource<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Empty response body")
            } else {
                Resource.Error("API error: ${response.message()}")
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.localizedMessage}")
        } catch (e: Exception) {
            Resource.Error("Unexpected error: ${e.localizedMessage}")
        }
    }
}
