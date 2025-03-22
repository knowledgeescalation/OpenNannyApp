package com.example.opennannyapp.data

import com.google.gson.annotations.SerializedName


data class InfoObjectItem(
    @SerializedName("name")
    val name: String,
    @SerializedName("value")
    val value: String
)

class InfoObject: ArrayList<InfoObjectItem>()