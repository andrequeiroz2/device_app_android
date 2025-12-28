package com.dev.deviceapp.viewmodel.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.deviceapp.database.device.DeviceDao
import com.dev.deviceapp.database.device.DeviceEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class DeviceOwnedViewModel @Inject constructor(
    private val deviceDao: DeviceDao
) : ViewModel() {
    
    private val _devices = MutableStateFlow<List<DeviceEntity>>(emptyList())
    val devices: StateFlow<List<DeviceEntity>> = _devices.asStateFlow()
    
    init {
        loadDevices()
    }
    
    fun loadDevices() {
        viewModelScope.launch {
            _devices.value = deviceDao.getAllDevices()
        }
    }
    
    suspend fun getDeviceByUuid(uuid: String): DeviceEntity? {
        return deviceDao.getDeviceByUuid(uuid)
    }
}

