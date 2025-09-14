package com.dev.deviceapp.viewmodel.broker


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.deviceapp.model.broker.BrokerGetFilterRequest
import com.dev.deviceapp.model.broker.BrokerGetFilterResponse
import com.dev.deviceapp.repository.broker.BrokerGetFilterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class BrokerGetFilterViewModel @Inject constructor(
    private val repository: BrokerGetFilterRepository
): ViewModel(){

    private val _state = MutableStateFlow<BrokerGetFilterUiState>(BrokerGetFilterUiState.Idle)
    val state: StateFlow<BrokerGetFilterUiState> = _state

    fun getBroker(param: BrokerGetFilterRequest){
        viewModelScope.launch {

            _state.value = BrokerGetFilterUiState.Loading

            try {

                val result = repository.getBroker(param)

                _state.value = when (result) {
                    is BrokerGetFilterResponse.Success -> BrokerGetFilterUiState.Success(
                        result
                    )

                    is BrokerGetFilterResponse.Error -> BrokerGetFilterUiState.Error(
                        result.errorMessage
                    )
                }

            }catch (e: Exception){
                Log.e("BrokerGetFilterViewModel", "Error: $e")
                _state.value = BrokerGetFilterUiState.Error(
                    "Application Error"
                )
            }
        }
    }
}

// UI states
sealed class BrokerGetFilterUiState {
    object Idle : BrokerGetFilterUiState()
    object Loading : BrokerGetFilterUiState()
    data class Success(val broker: BrokerGetFilterResponse.Success) : BrokerGetFilterUiState()
    data class Error(val message: String) : BrokerGetFilterUiState()
}