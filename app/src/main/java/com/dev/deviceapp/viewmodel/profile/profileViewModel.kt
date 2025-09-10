package com.dev.deviceapp.viewmodel.profile

import androidx.lifecycle.ViewModel
import com.dev.deviceapp.model.token.TokenInfo
import com.dev.deviceapp.repository.login.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val tokenRepository: TokenRepository
) : ViewModel() {

    val tokenInfo: TokenInfo? = tokenRepository.getTokenInfoRepository()

    fun logout() {
        tokenRepository.clearToken()
    }
}