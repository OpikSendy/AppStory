package com.example.appstory.utils

sealed class AuthState {
    data object Loading : AuthState()
    data class Error(val message: String?) : AuthState()
}