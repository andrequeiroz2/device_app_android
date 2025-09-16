package com.dev.deviceapp.viewmodel.broker

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.deviceapp.model.broker.BrokerResponse
import com.dev.deviceapp.model.broker.BrokerUpdateRequest
import com.dev.deviceapp.model.broker.BrokerUpdateResponse
import com.dev.deviceapp.repository.broker.BrokerUpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

@HiltViewModel
class BrokerUpdateViewModel @Inject constructor(
    private val repository: BrokerUpdateRepository
): ViewModel(){

    private val _state = MutableStateFlow<BrokerUpdateUiState>(BrokerUpdateUiState.Idle)
    val state: StateFlow<BrokerUpdateUiState> = _state

    @OptIn(ExperimentalTime::class)
    fun updateBroker(broker: BrokerUpdateRequest){
        viewModelScope.launch {
            _state.value = BrokerUpdateUiState.Loading

            try{
                val result = repository.updateBroker(broker)

                _state.value = when (result) {
                    is BrokerUpdateResponse.Success -> BrokerUpdateUiState.Success(
                        BrokerResponse.Success(
                            result.broker.uuid,
                            result.broker.host,
                            result.broker.port,
                            result.broker.clientId,
                            result.broker.version,
                            result.broker.versionText,
                            result.broker.keepAlive,
                            result.broker.cleanSession,
                            result.broker.lastWillTopic,
                            result.broker.lastWillMessage,
                            result.broker.lastWillQos,
                            result.broker.lastWillRetain,
                            result.broker.connected,
                            result.broker.createdAt,
                            result.broker.updatedAt,
                        )
                    )

                    is BrokerUpdateResponse.Error -> BrokerUpdateUiState.Error(
                        result.errorMessage
                    )
                }
            }catch (e: Exception){
                Log.e("BrokerUpdateViewModel", "Error: $e")
                _state.value = BrokerUpdateUiState.Error(
                    "Application Error"
                )
            }
        }
    }

}

sealed class BrokerUpdateUiState {
    object Idle : BrokerUpdateUiState()
    object Loading : BrokerUpdateUiState()
    data class Success(val broker: BrokerResponse.Success) : BrokerUpdateUiState()
    data class Error(val message: String) : BrokerUpdateUiState()
}