package com.example.opennannyapp.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opennannyapp.api.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLHandshakeException

class SensorsViewModel(
    private val api: ApiService
) : ViewModel() {

    val state = MutableStateFlow<InfoState>(InfoState.Loading)

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {

            state.value = InfoState.Loading

            try {
                val info = api.getSensors()
                state.value =
                    InfoState.Success(info.map { SensorsViewItem(name = it.name, value = it.value) })
            } catch (exception: ConnectException) {
                state.value = InfoState.Error(message = exception.message.orEmpty())
            } catch (exception: HttpException) {
                state.value = InfoState.Error(message = exception.message.orEmpty())
            }
            catch (exception: SSLHandshakeException) {
                state.value = InfoState.Error(message = exception.message.orEmpty())
            }
            catch (exception: SocketTimeoutException) {
                state.value = InfoState.Error(message = exception.message.orEmpty())
                return@launch
            }

        }
    }
}

data class SensorsViewItem(
    val name: String,
    val value: String
)

sealed class InfoState{
    data class Success(val list: List<SensorsViewItem>): InfoState()
    data class Error(val message: String): InfoState()
    data object Loading: InfoState()
}