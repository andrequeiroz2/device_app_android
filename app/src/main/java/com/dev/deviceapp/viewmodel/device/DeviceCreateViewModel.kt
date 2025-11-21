package com.dev.deviceapp.viewmodel.device

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.deviceapp.model.device.DeviceAdoptionResponse
import com.dev.deviceapp.model.device.DeviceCreateRequest
import com.dev.deviceapp.model.token.TokenInfo
import com.dev.deviceapp.repository.device.DeviceCreateRepository
import com.dev.deviceapp.repository.login.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceCreateViewModel @Inject constructor(
    private val repository: DeviceCreateRepository,
    private val tokenRepository: TokenRepository
): ViewModel(){

    private val _state = MutableStateFlow<DeviceCreateUiState>(DeviceCreateUiState.Idle)
    val state: StateFlow<DeviceCreateUiState> = _state

    fun createDevice(device: DeviceCreateRequest) {
        viewModelScope.launch {

            _state.value = DeviceCreateUiState.Loading

            try {
                val result = repository.createDevice(device)

                _state.value = when (result) {
                    is DeviceAdoptionResponse.Success -> DeviceCreateUiState.Success(
                        result
                    )

                    is DeviceAdoptionResponse.Error -> DeviceCreateUiState.Error(
                        result.errorMessage
                    )
                }
            }catch (e: Exception){
                Log.e("DeviceCreateViewModel", "Error: $e")
                _state.value = DeviceCreateUiState.Error(
                    "Application Error"
                )
            }
        }
    }

    fun getTokenInfo(): TokenInfo? = tokenRepository.getTokenInfoRepository()
}

sealed class DeviceCreateUiState {
    object  Idle: DeviceCreateUiState()
    object Loading : DeviceCreateUiState()
    data class Success(val device: DeviceAdoptionResponse.Success) : DeviceCreateUiState()
    data class Error(val message: String) : DeviceCreateUiState()
}