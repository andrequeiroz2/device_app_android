package com.dev.deviceapp.viewmodel.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.deviceapp.model.login.LoginRequest
import com.dev.deviceapp.model.login.LoginResponse
import com.dev.deviceapp.repository.login.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: LoginRepository

): ViewModel(){

    private val _state = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val state: StateFlow<LoginUiState> = _state

    fun login(login: LoginRequest){
        viewModelScope.launch{
            _state.value = LoginUiState.Loading

            val result = repository.login(login)

            _state.value = when(result){
                is LoginResponse.Success -> LoginUiState.Success(
                    "Login successful: $result"
                )
                is LoginResponse.Error -> LoginUiState.Error(
                    "Error: $result")
            }
        }
    }
}

// UI states
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val message: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}