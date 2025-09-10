package com.dev.deviceapp.viewmodel.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.deviceapp.model.user.UserGetRequest
import com.dev.deviceapp.model.user.UserGetResponse
import com.dev.deviceapp.repository.user.UserGetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import jakarta.inject.Inject

@HiltViewModel
class UserGetViewModel @Inject constructor(
    private val repository: UserGetRepository,
): ViewModel() {

    private val _state = MutableStateFlow<UserGetUiState>(UserGetUiState.Idle)
    val state: StateFlow<UserGetUiState> = _state

    fun getUser(param: UserGetRequest) {
        viewModelScope.launch {
            _state.value = UserGetUiState.Loading

            try{

                val result = repository.getUser(param)

                _state.value = when(result){

                    is UserGetResponse.Success -> {
                        Log.i(
                            "UserGetViewModel",
                            "Login successful: data: $param, result: $result")
                        UserGetUiState.Success(
                            result
                        )
                    }

                    is UserGetResponse.Error -> {
                        Log.e(
                            "UserGetViewModel",
                            "Error: ${result.errorMessage}, data: $param")
                        UserGetUiState.Error(
                            result.errorMessage
                        )
                    }
                }

            }catch (e: Exception){
                Log.e("LoginViewModel", "Error: $e")
                _state.value = UserGetUiState.Error(
                    "Application Error"
                )
            }
        }
    }
}

// UI states
sealed class UserGetUiState {
    object Idle : UserGetUiState()
    object Loading : UserGetUiState()
    data class Success(val user: UserGetResponse.Success) : UserGetUiState()
    data class Error(val message: String) : UserGetUiState()
}