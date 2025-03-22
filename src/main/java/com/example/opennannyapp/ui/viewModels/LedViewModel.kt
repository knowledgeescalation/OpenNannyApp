package com.example.opennannyapp.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opennannyapp.api.ApiService
import com.example.opennannyapp.data.LedRequest
import com.example.opennannyapp.data.LedResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLHandshakeException

class LedViewModel(
    private val api: ApiService
) : ViewModel() {

    val initState = MutableStateFlow<LedState>(LedState.Loading)
    val state = MutableStateFlow<LedState>(LedState.Success(""))

    init {
        getStatus()
    }

    fun stopInit() {
        initState.value = LedState.Success("")
    }

    fun sendCmd(cmd: String) {
        viewModelScope.launch(Dispatchers.IO) {

            state.value = LedState.Loading

            try {
                val status = api.led(LedRequest(cmd))
                state.value = LedState.Success(status.status)
            } catch (exception: ConnectException) {
                initState.value = LedState.Error(message = exception.message.orEmpty())
            } catch (exception: HttpException) {
                initState.value = LedState.Error(message = exception.message.orEmpty())
            } catch (exception: SSLHandshakeException) {
                initState.value = LedState.Error(message = exception.message.orEmpty())
            } catch (exception: SocketTimeoutException) {
                initState.value = LedState.Error(message = exception.message.orEmpty())
                return@launch
            }

        }
    }

    private fun getStatus() {
        viewModelScope.launch(Dispatchers.IO) {

            initState.value = LedState.Loading

            try {
                val status = api.led(LedRequest("status"))
                initState.value = LedState.Success(status.status)

            } catch (exception: ConnectException) {
                initState.value = LedState.Error(message = exception.message.orEmpty())
            } catch (exception: HttpException) {
                initState.value = LedState.Error(message = exception.message.orEmpty())
            } catch (exception: SSLHandshakeException) {
                initState.value = LedState.Error(message = exception.message.orEmpty())
            } catch (exception: SocketTimeoutException) {
                initState.value = LedState.Error(message = exception.message.orEmpty())
                return@launch
            }

        }
    }
}

sealed class LedState{
    data class Success(val status: String): LedState()
    data class Error(val message: String): LedState()
    data object Loading: LedState()
}