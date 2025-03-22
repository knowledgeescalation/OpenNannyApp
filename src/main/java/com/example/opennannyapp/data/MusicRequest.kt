package com.example.opennannyapp.data

import com.google.gson.annotations.SerializedName

data class MusicRequest(
    @SerializedName("cmd") val cmd: String,
    @SerializedName("parameters") val parameters: String
)


data class DirbObjectItem(
    @SerializedName("dir")
    val dir: String
)

class DirObject: ArrayList<DirbObjectItem>()

data class Mp3ObjectItem(
    @SerializedName("file")
    val file: String
)

class Mp3Object: ArrayList<Mp3ObjectItem>()

data class SongPlayResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("duration")
    val duration: Long? = null,
    @SerializedName("volume")
    val volume: Float? = null
)

data class SongDefaultResponse(
    @SerializedName("status")
    val status: String,
)

data class SongStatusResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("song")
    val song: String? = null,
    @SerializedName("directory")
    val directory: String? = null,
    @SerializedName("duration")
    val duration: Long? = null,
    @SerializedName("currentProgress")
    val currentProgress: Long? = null,
    @SerializedName("volume")
    val volume: Float? = null

)