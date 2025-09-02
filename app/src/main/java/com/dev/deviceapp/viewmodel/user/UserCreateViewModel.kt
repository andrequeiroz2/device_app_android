package com.dev.deviceapp.viewmodel.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dev.deviceapp.repository.user.UserCreateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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

    private val _state = MutableStateFlow<UserUiState>(UserUiState.Idle)
    val state: StateFlow<UserUiState> = _state

    fun createUser(user: UserCreateRequest){
        viewModelScope.launch {

            _state.value = UserUiState.Loading

            val result = repository.createUser(user)

            _state.value = when(result){
                is UserCreateResponse.Success -> UserUiState.Success(
                    "User create successfully: $result"
                )
                is UserCreateResponse.Error -> UserUiState.Error(
                    "Error: $result"
                )
            }
        }
    }
}


// UI states
sealed class UserUiState {
    object Idle : UserUiState()
    object Loading : UserUiState()
    data class Success(val message: String) : UserUiState()
    data class Error(val message: String) : UserUiState()
}