package com.example.appstory.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appstory.data.model.MyApp
import com.example.appstory.data.model.SessionManager
import com.example.appstory.data.model.TokenManager
import com.example.appstory.data.repository.StoryRepository
import com.example.appstory.data.request.LoginRequest
import com.example.appstory.data.request.RegisterRequest
import com.example.appstory.data.request.LoginResponse
import com.example.appstory.data.request.RegisterResponse
import com.example.appstory.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: StoryRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _loginStatus = MutableLiveData<Resource<LoginResponse>>()
    val loginStatus: LiveData<Resource<LoginResponse>> get() = _loginStatus

    private val _registrationStatus = MutableLiveData<Resource<RegisterResponse>>()
    val registrationStatus: LiveData<Resource<RegisterResponse>> get() = _registrationStatus

    private val _authState = MutableLiveData<Resource<String?>>()
    val authState: LiveData<Resource<String?>> = _authState

    init {
        val token = TokenManager.getToken(MyApp.context)
        if (token != null) {
            _authState.value = Resource.Success(token)
        } else {
            _authState.value = Resource.Error("Token not found")
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginStatus.value = Resource.Loading()
            try {
                val loginData = LoginRequest(email, password)
                when (val response = repository.loginUser(loginData)) {
                    is Resource.Success -> {
                        response.data?.loginResult?.token?.let { token ->
                            sessionManager.saveAuthToken(token)
                            _loginStatus.value = Resource.Success(response.data)
                        } ?: run {
                            _loginStatus.value = Resource.Error("Login failed: Token is missing")
                        }
                    }
                    is Resource.Error -> {
                        _loginStatus.value = Resource.Error(response.message ?: "Login failed")
                    }
                    is Resource.Loading -> {
                        _loginStatus.value = Resource.Loading()
                    }
                }
            } catch (e: Exception) {
                _loginStatus.value = Resource.Error("Login failed: ${e.message}")
            }
        }
    }

    fun registerUser(name: String, email: String, password: String) {
        viewModelScope.launch {
            _registrationStatus.value = Resource.Loading()
            try {
                val registerData = RegisterRequest(name, email, password)
                when (val response = repository.registerUser(registerData)){
                    is Resource.Error ->
                        _registrationStatus.value = Resource.Error(response.message ?: "Register failed")
                    is Resource.Loading ->
                        _registrationStatus.value = Resource.Loading()
                    is Resource.Success ->
                        if (response.data?.error == true) {
                            _registrationStatus.value = Resource.Error(response.data.message)
                        } else {
                            _registrationStatus.value = Resource.Error(response.data?.message ?: "Unknown error")
                        }
                }
            } catch (e: Exception) {
                _registrationStatus.value = Resource.Error("Register failed: ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                _authState.value = Resource.Loading()
                sessionManager.clearSession()
                _authState.value = Resource.Success(null)
            } catch (e: Exception) {
                _authState.value = Resource.Error(e.message ?: "Logout failed")
            }
        }
    }
}
