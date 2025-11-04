package com.dev.deviceapp.view.device


import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.dev.deviceapp.viewmodel.device.DeviceOptionsUiState
import com.dev.deviceapp.viewmodel.device.DeviceOptionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")

@Composable
fun DeviceCharacteristicReadInfoScreen(
    navController: NavController,
    deviceMac: String?,
    context: Context,
    vm: DeviceOptionsViewModel = hiltViewModel()
) {
    // Dispara leitura BLE assim que a tela abre
    LaunchedEffect(deviceMac) {
        deviceMac?.let {
            val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
            val device = bluetoothManager?.adapter?.getRemoteDevice(it)
            device?.let { vm.loadDevice(it, context) }
        }
    }

    when (val state = vm.uiState) {
        DeviceOptionsUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        DeviceOptionsUiState.Unauthorized -> {
            Toast.makeText(context, "Device belongs to another user", Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
    }
}
