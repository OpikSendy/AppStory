package com.example.appstory.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appstory.data.model.SessionManager
import com.example.appstory.data.model.StoryEntity
import com.example.appstory.data.repository.StoryRepository
import com.example.appstory.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: StoryRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _storiesWithLocation = MediatorLiveData<Resource<List<StoryEntity>>>()
    val storiesWithLocation: LiveData<Resource<List<StoryEntity>>> = _storiesWithLocation

    private val _mapState = MutableLiveData<MapState>()
    val mapState: LiveData<MapState> = _mapState

    fun refreshStories() {
        val token = sessionManager.getAuthToken()
        if (!token.isNullOrEmpty()) {
            _storiesWithLocation.value = Resource.Loading()

            val source = repository.getStoriesWithLocation(token)
            _storiesWithLocation.addSource(source) { resource ->
                when (resource) {
                    is Resource.Success -> {
                        if (resource.data.isNullOrEmpty()) {
                            _mapState.value = MapState.EMPTY
                        } else {
                            _mapState.value = MapState.CONTENT
                        }
                    }
                    is Resource.Error -> {
                        if (resource.message?.contains("Unauthorized") == true) {
                            _mapState.value = MapState.UNAUTHORIZED
                        } else {
                            _mapState.value = MapState.ERROR
                        }
                    }
                    is Resource.Loading -> _mapState.value = MapState.LOADING
                }
                _storiesWithLocation.value = resource
                _storiesWithLocation.removeSource(source)
            }
        } else {
            _storiesWithLocation.value = Resource.Error("Token is missing or expired")
            _mapState.value = MapState.UNAUTHORIZED
        }
    }

    enum class MapState {
        LOADING, CONTENT, EMPTY, ERROR, UNAUTHORIZED
    }
}