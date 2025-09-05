package com.dev.deviceapp.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.deviceapp.model.login.LoginRequest
import com.dev.deviceapp.model.user.UserDeleteResponse
import com.dev.deviceapp.repository.user.UserDeleteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import jakarta.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


@HiltViewModel
class UserDeleteViewModel @Inject constructor(
    private val repository: UserDeleteRepository
): ViewModel() {

    private val _state = MutableStateFlow<UserDeleteUiState> (UserDeleteUiState.Idle)

    val state: StateFlow<UserDeleteUiState> = _state

    fun deleteUser(param: LoginRequest){
        viewModelScope.launch {
            _state.value = UserDeleteUiState.Loading

            val result = repository.deleteUser(param)

            _state.value = when(result) {
                is UserDeleteResponse.Success -> UserDeleteUiState.Success(
                    "User deleted successfully"
                )

                is UserDeleteResponse.Error -> UserDeleteUiState.Error(
                    "Error: $result"
                )
            }
        }
    }
}

// UI states
sealed class UserDeleteUiState {
    object Idle : UserDeleteUiState()
    object Loading : UserDeleteUiState()
    data class Success(val message: String) : UserDeleteUiState()
    data class Error(val message: String) : UserDeleteUiState()
}