package com.dev.deviceapp.viewmodel.broker

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.deviceapp.model.broker.BrokerDeleteResponse
import com.dev.deviceapp.repository.broker.BrokerDeleteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class BrokerDeleteViewModel @Inject constructor(
    private val repository: BrokerDeleteRepository
): ViewModel() {

    private val _state = MutableStateFlow<BrokerDeleteUiState> (BrokerDeleteUiState.Idle)
    val state: StateFlow<BrokerDeleteUiState> = _state

    fun deleteBroker(brokerUuid: String){
        viewModelScope.launch {
            _state.value = BrokerDeleteUiState.Loading

            try{

                val result = repository.deleteBroker(brokerUuid)

                _state.value = when(result) {
                    is BrokerDeleteResponse.Success -> BrokerDeleteUiState.Success(
                        "Broker deleted successfully"
                    )

                    is BrokerDeleteResponse.Error -> BrokerDeleteUiState.Error(
                        result.errorMessage
                    )
                }

            }catch (e: Exception){
                Log.e("BrokerDeleteViewModel", "Error: $e")
                _state.value = BrokerDeleteUiState.Error(
                    "Application Error"
                )
            }
        }
    }
}

// UI states
sealed class BrokerDeleteUiState {
    object Idle : BrokerDeleteUiState()
    object Loading : BrokerDeleteUiState()
    data class Success(val message: String) : BrokerDeleteUiState()
    data class Error(val message: String) : BrokerDeleteUiState()
}