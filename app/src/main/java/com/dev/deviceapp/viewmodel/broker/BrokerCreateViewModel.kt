package com.dev.deviceapp.viewmodel.broker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.deviceapp.model.broker.BrokerCreateRequest
import com.dev.deviceapp.model.broker.BrokerResponse
import com.dev.deviceapp.repository.broker.BrokerCreateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrokerCreateViewModel @Inject constructor(
    private val repository: BrokerCreateRepository
): ViewModel(){


    private val _state = MutableStateFlow<BrokerCreateUiState>(BrokerCreateUiState.Idle)
    val state: StateFlow<BrokerCreateUiState> = _state

    fun createBroker(broker: BrokerCreateRequest){
        viewModelScope.launch {

            _state.value = BrokerCreateUiState.Loading

            val result = repository.createBroker(broker)

            _state.value = when(result){
                is BrokerResponse.Success -> BrokerCreateUiState.Success(
                    "Broker create successfully: $result"
                )
                is BrokerResponse.Error -> BrokerCreateUiState.Error(
                    "Error: $result"
                )
            }
        }
    }
}

// UI states
sealed class BrokerCreateUiState {
    object Idle : BrokerCreateUiState()
    object Loading : BrokerCreateUiState()
    data class Success(val message: String) : BrokerCreateUiState()
    data class Error(val message: String) : BrokerCreateUiState()
}