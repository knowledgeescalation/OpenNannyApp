package com.example.opennannyapp.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opennannyapp.api.ApiService
import com.example.opennannyapp.data.MusicRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLHandshakeException

class MusicViewModel(
    private val api: ApiService
) : ViewModel() {

    val dirState = MutableStateFlow<DirState>(DirState.Loading)
    val mp3State = MutableStateFlow<Mp3State>(Mp3State.Loading)
    val songState = MutableStateFlow<SongState>(SongState.Initial)

    init {
        getDirs()
    }

    private fun getDirs() {

        viewModelScope.launch(Dispatchers.IO) {

            dirState.value = DirState.Loading

            try {
                val dirs = api.getDirs(MusicRequest(cmd = "lsdir", parameters = ""))
                dirState.value =
                    DirState.Success(dirs.map { DirViewItem(dir = it.dir) })
            } catch (exception: ConnectException) {
                dirState.value = DirState.Error(message = exception.message.orEmpty())
            } catch (exception: HttpException) {
                dirState.value = DirState.Error(message = exception.message.orEmpty())
            }
            catch (exception: SSLHandshakeException) {
                dirState.value = DirState.Error(message = exception.message.orEmpty())
            }
            catch (exception: SocketTimeoutException) {
                dirState.value = DirState.Error(message = exception.message.orEmpty())
                return@launch
            }

        }

    }

    fun getMp3(dir: String) {

        viewModelScope.launch(Dispatchers.IO) {

            mp3State.value = Mp3State.Loading

            try {
                val dirs = api.getMp3(MusicRequest(cmd = "lsmp3", parameters = dir))
                mp3State.value =
                    Mp3State.Success(dirs.map { Mp3ViewItem(file = it.file) })
            } catch (exception: ConnectException) {
                mp3State.value = Mp3State.Error(message = exception.message.orEmpty())
            } catch (exception: HttpException) {
                mp3State.value = Mp3State.Error(message = exception.message.orEmpty())
            }
            catch (exception: SSLHandshakeException) {
                mp3State.value = Mp3State.Error(message = exception.message.orEmpty())
            }
            catch (exception: SocketTimeoutException) {
                mp3State.value = Mp3State.Error(message = exception.message.orEmpty())
                return@launch
            }

        }

    }

    fun playSong(songName: String, directory: String) {
        viewModelScope.launch(Dispatchers.IO) {
            songState.value = SongState.Loading
            try {
                val response = api.songPlay(MusicRequest(cmd = "play", parameters = "$directory/$songName.mp3"))
                if (response.status == "OK") {
                    songState.value = SongState.Playing(
                        songName = songName,
                        directory = directory,
                        duration = response.duration ?: 0L,
                        currentProgress = 0L,
                        volume = response.volume ?: 0.0f,
                    )
                    startStatusUpdates()
                } else {
                    songState.value = SongState.Error(message = response.status)
                }
            } catch (e: Exception) {
                songState.value = SongState.Error(message = e.message.orEmpty())
            }
        }
    }

    fun pauseSong() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = api.songPause(MusicRequest(cmd = "pause", parameters = ""))
                if (response.status == "OK" && songState.value is SongState.Playing) {
                    val current = songState.value as SongState.Playing
                    songState.value = current.copy(isPaused = true)
                }
            } catch (e: Exception) {
                songState.value = SongState.Error(message = e.message.orEmpty())
            }
        }
    }

    fun resumeSong() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = api.songUnPause(MusicRequest(cmd = "unpause", parameters = ""))
                if (response.status == "OK" && songState.value is SongState.Playing) {
                    val current = songState.value as SongState.Playing
                    songState.value = current.copy(isPaused = false)
                }
            } catch (e: Exception) {
                songState.value = SongState.Error(message = e.message.orEmpty())
            }
        }
    }

    fun stopSong() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = api.songStop(MusicRequest(cmd = "stop", parameters = ""))
                if (response.status == "OK") {
                    songState.value = SongState.Stopped
                }
            } catch (e: Exception) {
                songState.value = SongState.Error(message = e.message.orEmpty())
            }
        }
    }

    fun rewindSong(seconds: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = api.songRewind(MusicRequest(cmd = "rewind", parameters = seconds.toString()))
                if (response.status == "OK") {
                    updateSongStatus()
                }
            } catch (e: Exception) {
                songState.value = SongState.Error(message = e.message.orEmpty())
            }
        }
    }

    fun setVolume(volume: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = api.songSetVolume(MusicRequest(cmd = "set_volume", parameters = volume.toString()))
                if (response.status == "OK" && songState.value is SongState.Playing) {
                    val current = songState.value as SongState.Playing
                    songState.value = current.copy(volume = volume)
                }
            } catch (e: Exception) {
                songState.value = SongState.Error(message = e.message.orEmpty())
            }
        }
    }

    private fun startStatusUpdates() {
        viewModelScope.launch(Dispatchers.IO) {
            while (songState.value is SongState.Playing && !(songState.value as SongState.Playing).isPaused) {
                delay(5000) // Update every 5 seconds
                updateSongStatus()
            }
        }
    }

    suspend fun updateSongStatus() {
        try {
            val response = api.songStatus(MusicRequest(cmd = "status", parameters = ""))

            when (response.status) {
                "playing" -> {
                    songState.value = SongState.Playing(
                        songName = response.song!!,
                        directory = response.directory!!,
                        currentProgress = response.currentProgress!!,
                        duration = response.duration!!,
                        volume = response.volume!!,
                        isPaused = false
                    )

                }
                "paused" -> {
                    songState.value = SongState.Playing(
                        songName = response.song!!,
                        directory = response.directory!!,
                        currentProgress = response.currentProgress!!,
                        duration = response.duration!!,
                        volume = response.volume!!,
                        isPaused = true
                    )
                }
                "stopped" -> {
                    songState.value = SongState.Stopped
                }
            }
        } catch (exception: Exception) {
            // Don't update state on error to avoid disrupting the player
            exception.printStackTrace()
        }
    }



}

data class DirViewItem(
    val dir: String
)

sealed class DirState{
    data class Success(val list: List<DirViewItem>): DirState()
    data class Error(val message: String): DirState()
    data object Loading: DirState()
}

data class Mp3ViewItem(
    val file: String
)

sealed class Mp3State{
    data class Success(val list: List<Mp3ViewItem>): Mp3State()
    data class Error(val message: String): Mp3State()
    data object Loading: Mp3State()
}

sealed class SongState {
    data object Initial : SongState()
    data object Loading : SongState()
    data object Stopped : SongState()
    data class Playing(
        val songName: String,
        val directory: String,
        var duration: Long,
        var currentProgress: Long,
        var volume: Float,
        val isPaused: Boolean = false
    ) : SongState()
    data class Error(val message: String) : SongState()
}

