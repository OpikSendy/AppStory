package com.example.appstory.data.response

import com.google.gson.annotations.SerializedName

data class ResetPasswordResponse(
    @field:SerializedName("error") val error: Boolean,
    @field:SerializedName("message") val message: String,
    @field:SerializedName("email") val email: String
)