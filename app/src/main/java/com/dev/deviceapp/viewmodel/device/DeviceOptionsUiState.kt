package com.dev.deviceapp.viewmodel.device

import com.dev.deviceapp.model.device.DeviceBleInfoModel

sealed interface DeviceOptionsUiState {
    data object Loading : DeviceOptionsUiState
    /** Device belongs to another user */
    data object Unauthorized : DeviceOptionsUiState
    /** Device available for adoption */
    data class AdoptAvailable(val device: DeviceBleInfoModel) : DeviceOptionsUiState
    /** Device belongs to the logged-in user */
    data class Owner(val device: DeviceBleInfoModel) : DeviceOptionsUiState
    /** All error */
    data class Error(val message: String) : DeviceOptionsUiState
}