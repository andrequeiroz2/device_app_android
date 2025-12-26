package com.dev.deviceapp.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.deviceapp.database.device.DeviceDao
import com.dev.deviceapp.model.token.TokenInfo
import com.dev.deviceapp.repository.login.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val deviceDao: DeviceDao
) : ViewModel() {

    val tokenInfo: TokenInfo? = tokenRepository.getTokenInfoRepository()

    fun logout() {
        viewModelScope.launch {
            // Limpar o banco de dados em cache
            deviceDao.deleteAllDevices()
            // Limpar o token
            tokenRepository.clearToken()
        }
    }
}