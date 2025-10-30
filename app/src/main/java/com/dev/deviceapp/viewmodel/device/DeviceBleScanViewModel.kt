package com.dev.deviceapp.viewmodel.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.deviceapp.repository.device.DeviceBleScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceBleScanViewModel @Inject constructor(
    private val deviceBleScanRepository: DeviceBleScanRepository
) : ViewModel() {

    val scannedDevices = deviceBleScanRepository.scannedDevices
    val isScanning = deviceBleScanRepository.isScanning

    fun startScan() {
        viewModelScope.launch {
            deviceBleScanRepository.startScan()
        }
    }

    fun stopScan() {
        viewModelScope.launch {
            deviceBleScanRepository.stopScan()
        }
    }
}
