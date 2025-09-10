package com.dev.deviceapp.viewmodel.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.deviceapp.model.user.UserUpdateRequest
import com.dev.deviceapp.model.user.UserUpdateResponse
import com.dev.deviceapp.repository.user.UserUpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class UserUpdateViewModel @Inject constructor(
    private val repository: UserUpdateRepository
): ViewModel(){

    private val _state = MutableStateFlow<UserUpdateUiState>(UserUpdateUiState.Idle)
    val state: StateFlow<UserUpdateUiState> = _state

    fun updateUser(param: UserUpdateRequest) {
        viewModelScope.launch {
            _state.value = UserUpdateUiState.Loading

            try{

                val result = repository.updateUser(param)

                _state.value = when (result) {

                    is UserUpdateResponse.Success -> {
                        Log.i(
                            "UserUpdateViewModel",
                            "User update successful: data: $param, result: $result"
                        )
                        UserUpdateUiState.Success(
                            result
                        )
                    }

                    is UserUpdateResponse.Error -> {
                        Log.e(
                            "UserUpdateViewModel",
                            "Error: ${result.errorMessage}, data: $param")
                        UserUpdateUiState.Error(
                            result.errorMessage
                        )
                    }
                }

            }catch (e: Exception){
                Log.e("UserUpdateViewModel", "Error: $e")
                _state.value = UserUpdateUiState.Error(
                    "Application Error"
                )
            }
        }
    }
}

sealed class UserUpdateUiState {
    object Idle : UserUpdateUiState()
    object Loading : UserUpdateUiState()
    data class Success(val user: UserUpdateResponse.Success) : UserUpdateUiState()
    data class Error(val message: String) : UserUpdateUiState()
}