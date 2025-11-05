@file:OptIn(ExperimentalMaterial3Api::class)

package com.dev.deviceapp.view.device

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.dev.deviceapp.viewmodel.device.DeviceOptionsUiState
import com.dev.deviceapp.viewmodel.device.DeviceOptionsViewModel

@androidx.annotation.RequiresPermission(
    allOf = [android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT]
)
@Composable
fun DeviceOptionsScreen(
    navController: NavController,
    deviceMac: String?,
    vm: DeviceOptionsViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(deviceMac) {
        deviceMac?.let { mac ->
            val bluetoothManager = context.getSystemService(android.bluetooth.BluetoothManager::class.java)
            val bluetoothAdapter = bluetoothManager?.adapter

            if (bluetoothAdapter == null) {
                Toast.makeText(context, "Bluetooth not available", Toast.LENGTH_LONG).show()
                navController.popBackStack()
                return@LaunchedEffect
            }

            val device = bluetoothAdapter.getRemoteDevice(mac)
            vm.loadDevice(device, context)
        }
    }

    val state by vm.uiState.collectAsState()

    LaunchedEffect(state) {
        if (state is DeviceOptionsUiState.Error) {
            val msg = (state as DeviceOptionsUiState.Error).message
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            Log.e("DeviceOptionsScreen", msg)
            navController.popBackStack()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF121212)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Text(
                text = "Device Options",
                style = MaterialTheme.typography.headlineLarge,
                color = Color(0xFF00A86B)
            )

            Spacer(Modifier.height(20.dp))

            when (state) {

                DeviceOptionsUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF00A86B),
                                strokeWidth = 4.dp
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Connecting...",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                is DeviceOptionsUiState.AdoptAvailable -> {
                    val deviceInf = (state as DeviceOptionsUiState.AdoptAvailable).device
                    Log.i("DeviceOptionsScreen", "state is DeviceOptionsUiState.AdoptAvailable, device: $deviceInf")

                    Spacer(Modifier.height(20.dp))


                    Button(
                        onClick = {
                            navController.navigate("device/adopt/details/${deviceMac}")
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00A86B),
                            contentColor = Color.White
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Adopt")
                    }

                }

                is DeviceOptionsUiState.Owner -> {
                    Log.i("DeviceOptionsScreen", "state is DeviceOptionsUiState.Owner")

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = { /* TODO action */ },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00A86B),
                            contentColor = Color.White
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Update")
                    }
                }

                DeviceOptionsUiState.Unauthorized -> {
                    Toast.makeText(context, "Device already adopted by another user", Toast.LENGTH_LONG).show()
                    Log.e("DeviceOptionsScreen", "Device already adopted by another user")
                    navController.popBackStack()
                }

                is DeviceOptionsUiState.Error -> Unit
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    navController.navigate("device/adopt/details/${deviceMac}")
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00A86B),
                    contentColor = Color.White
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Details")
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00A86B),
                    contentColor = Color.White
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Back")
            }
        }
    }
}
