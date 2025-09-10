package com.dev.deviceapp.viewmodel.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.deviceapp.model.login.LoginRequest
import com.dev.deviceapp.model.login.LoginResponse
import com.dev.deviceapp.repository.login.LoginRepository
import com.dev.deviceapp.repository.login.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch



@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: LoginRepository,
    private val tokenRepository: TokenRepository
): ViewModel(){

    private val _state = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val state: StateFlow<LoginUiState> = _state

    fun login(login: LoginRequest){
        viewModelScope.launch{
            _state.value = LoginUiState.Loading

            try{

                val result = repository.login(login)

                _state.value = when(result){

                    is LoginResponse.Success -> {
                        tokenRepository.saveToken(result.token)
                        Log.i("LoginViewModel", "Login successful: data: $login, result: $result")
                        LoginUiState.Success(
                            "$result"
                        )
                    }

                    is LoginResponse.Error -> {
                        tokenRepository.clearToken()
                        Log.e("LoginViewModel", "Error: ${result.errorMessage}, data: $login")
                        LoginUiState.Error(
                            result.errorMessage
                        )
                    }
                }

            }catch (e: Exception){

                Log.e("LoginViewModel", "Error: $e")
                _state.value = LoginUiState.Error(
                    "Application Error"
                )

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