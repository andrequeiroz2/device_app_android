package com.dev.deviceapp.viewmodel.auth

import androidx.lifecycle.ViewModel
import com.dev.deviceapp.repository.login.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val tokenRepository: TokenRepository
): ViewModel(){
    val tokenFlow = tokenRepository.getToken()
}