package com.dev.deviceapp.viewmodel.broker

import android.util.Log
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

            try {

                val result = repository.createBroker(broker)

                _state.value = when (result) {
                    is BrokerResponse.Success -> BrokerCreateUiState.Success(
                        result
                    )

                    is BrokerResponse.Error -> BrokerCreateUiState.Error(
                        result.errorMessage
                    )
                }

            }catch (e: Exception){
                Log.e("BrokerCreateViewModel", "Error: $e")
                _state.value = BrokerCreateUiState.Error(
                    "Application Error"
                )
            }
        }
    }
}

// UI states
sealed class BrokerCreateUiState {
    object Idle : BrokerCreateUiState()
    object Loading : BrokerCreateUiState()
    data class Success(val broker: BrokerResponse.Success) : BrokerCreateUiState()
    data class Error(val message: String) : BrokerCreateUiState()
}