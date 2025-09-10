package com.dev.deviceapp.viewmodel.broker

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.deviceapp.model.broker.BrokerGetRequest
import com.dev.deviceapp.model.broker.BrokerResponse
import com.dev.deviceapp.repository.broker.BrokerGetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class BrokerGetViewModel @Inject constructor(
    private val repository: BrokerGetRepository
): ViewModel(){

    private val _state = MutableStateFlow<BrokerGetUiState>(BrokerGetUiState.Idle)
    val state: StateFlow<BrokerGetUiState> = _state

    fun getBroker(param: BrokerGetRequest){
        viewModelScope.launch {

            _state.value = BrokerGetUiState.Loading

            val result = repository.getBroker(param)

            _state.value = when(result){
                is BrokerResponse.Success -> BrokerGetUiState.Success(
                    "Broker get successfully: $result"
                )
                is BrokerResponse.Error -> BrokerGetUiState.Error(
                    "Error: $result"
                )
            }
        }
    }
}

// UI states
sealed class BrokerGetUiState {
    object Idle : BrokerGetUiState()
    object Loading : BrokerGetUiState()
    data class Success(val message: String) : BrokerGetUiState()
    data class Error(val message: String) : BrokerGetUiState()
}