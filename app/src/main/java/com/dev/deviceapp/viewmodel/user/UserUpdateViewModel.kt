package com.dev.deviceapp.viewmodel.user

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

            val result = repository.updateUser(param)

            _state.value = when(result){
                is UserUpdateResponse.Success -> UserUpdateUiState.Success(
                    "User update successfully: $result"
                )

                is UserUpdateResponse.Error -> UserUpdateUiState.Error(
                    "Error: $result"
                )
            }
        }
    }
}

sealed class UserUpdateUiState {
    object Idle : UserUpdateUiState()
    object Loading : UserUpdateUiState()
    data class Success(val message: String) : UserUpdateUiState()
    data class Error(val message: String) : UserUpdateUiState()
}