package com.example.opennannyapp.data

import com.google.gson.annotations.SerializedName

data class LedRequest(
    @SerializedName("cmd") val cmd: String
)

data class LedResponse(
    @SerializedName("status") val status: String
)