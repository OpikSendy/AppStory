package com.example.appstory.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appstory.data.model.MyApp
import com.example.appstory.data.model.SessionManager
import com.example.appstory.data.model.StoryEntity
import com.example.appstory.data.repository.StoryRepository
import com.example.appstory.data.response.AddStoryResponse
import com.example.appstory.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val repository: StoryRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _stories = MutableLiveData<Resource<List<StoryEntity>>>()
    val stories: LiveData<Resource<List<StoryEntity>>> = _stories

    private val _storyList = MutableLiveData<Resource<List<StoryEntity>>>()
    val storyList: LiveData<Resource<List<StoryEntity>>> get() = _storyList

    private val _storyDetail = MutableLiveData<Resource<StoryEntity>>()
    val storyDetail: LiveData<Resource<StoryEntity>> = _storyDetail

    private val _addStoryStatus = MutableLiveData<Resource<AddStoryResponse>>()
    val addStoryStatus: LiveData<Resource<AddStoryResponse>> get() = _addStoryStatus

    fun getAllStories(page: Int, size: Int) {
        val token = sessionManager.getAuthToken()
        if (token != null) {
            viewModelScope.launch {
                repository.getAllStories(
                    token = token,
                    page = page,
                    size = size
                ).observeForever { resource ->
                    _storyList.value = resource
                }
            }
        } else {
            _storyList.value = Resource.Error("Token is missing or expired")
        }
    }

    fun getStoryDetail(storyId: String) {
        viewModelScope.launch {
            _storyDetail.value = Resource.Loading()

            try {
                Log.d("ViewModel", "Getting story detail for ID: $storyId")

                val token = sessionManager.getAuthToken()
                if (token.isNullOrEmpty()) {
                    _storyDetail.value = Resource.Error("Token is missing or expired")
                    return@launch
                }

                Log.d("ViewModel", "Token present: ${token.isNotEmpty()}")

                val result = repository.getStoryDetail(token, storyId)
                _storyDetail.value = result

            } catch (e: Exception) {
                Log.e("ViewModel", "Error getting story detail", e)
                _storyDetail.value = Resource.Error("Error: ${e.message}")
            }
        }
    }

    fun addStory(photo: MultipartBody.Part, description: RequestBody,
                 lat: RequestBody? = null, lon: RequestBody? = null) {
        val token = sessionManager.getAuthToken()
        if (token != null) {
            viewModelScope.launch {
                _addStoryStatus.value = Resource.Loading()
                try {
                    when(val response = repository.addStory(token, photo, description, lat, lon)) {
                        is Resource.Error -> {
                            _addStoryStatus.value = Resource.Error("Error: ${response.message}")
                        }
                        is Resource.Success -> {
                            _addStoryStatus.value = Resource.Success(response.data!!)
                        }
                        is Resource.Loading -> {
                        }
                    }
                } catch (e: Exception) {
                    _addStoryStatus.value = Resource.Error("Error: ${e.message}")
                }
            }
        } else {
            _addStoryStatus.value = Resource.Error("Token is missing or expired")
        }
    }
}
