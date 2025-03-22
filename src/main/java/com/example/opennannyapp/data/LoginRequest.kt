package com.example.opennannyapp.data

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    @SerializedName("access_token")
    val token: String,
    @SerializedName("token_type")
    val type: String,
)