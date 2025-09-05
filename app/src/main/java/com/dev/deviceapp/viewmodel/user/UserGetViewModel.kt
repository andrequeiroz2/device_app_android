package com.dev.deviceapp.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.deviceapp.model.user.UserGetRequest
import com.dev.deviceapp.model.user.UserGetResponse
import com.dev.deviceapp.repository.user.UserGetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserGetViewModel @Inject constructor(
    private val repository: UserGetRepository
): ViewModel() {

    private val _state = MutableStateFlow<UserGetUiState>(UserGetUiState.Idle)

    val state: StateFlow<UserGetUiState> = _state

    fun getUser(param: UserGetRequest) {
        viewModelScope.launch {
            _state.value = UserGetUiState.Loading

            val result = repository.getUser(param)

            _state.value = when(result){
                is UserGetResponse.Success -> UserGetUiState.Success(
                    "User get successfully: $result"
                )

                is UserGetResponse.Error -> UserGetUiState.Error(
                    "Error: $result"
                )
            }
        }
    }
}

// UI states
sealed class UserGetUiState {
    object Idle : UserGetUiState()
    object Loading : UserGetUiState()
    data class Success(val message: String) : UserGetUiState()
    data class Error(val message: String) : UserGetUiState()
}