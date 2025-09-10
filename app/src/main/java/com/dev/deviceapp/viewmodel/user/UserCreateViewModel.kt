package com.dev.deviceapp.viewmodel.user

import android.util.Log
import com.dev.deviceapp.repository.user.UserCreateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.deviceapp.model.user.UserCreateRequest
import com.dev.deviceapp.model.user.UserCreateResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class UserCreateViewModel @Inject constructor(
    private val repository: UserCreateRepository
): ViewModel(){

    private val _state = MutableStateFlow<UserCreateUiState>(UserCreateUiState.Idle)
    val state: StateFlow<UserCreateUiState> = _state

    fun createUser(user: UserCreateRequest){
        viewModelScope.launch {

            _state.value = UserCreateUiState.Loading

            try {

                val result = repository.createUser(user)

                _state.value = when (result) {
                    is UserCreateResponse.Success -> UserCreateUiState.Success(
                        result
                    )

                    is UserCreateResponse.Error -> UserCreateUiState.Error(
                        result.errorMessage
                    )
                }

            }catch (e: Exception){
                Log.e("UserCreateViewModel", "Error: $e")
                _state.value = UserCreateUiState.Error(
                    "Application Error"
                )
            }
        }
    }
}


// UI states
sealed class UserCreateUiState {
    object Idle : UserCreateUiState()
    object Loading : UserCreateUiState()
    data class Success(val user: UserCreateResponse.Success) : UserCreateUiState()
    data class Error(val message: String) : UserCreateUiState()
}