package com.dev.deviceapp.view.device

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.dev.deviceapp.AppDestinations
import com.dev.deviceapp.viewmodel.device.DeviceBleScanViewModel
import com.dev.deviceapp.model.device.DeviceBleModel
import kotlinx.coroutines.delay
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun DeviveBleScanListScreen(
    navController: NavController,
    scanViewModel: DeviceBleScanViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val devices by scanViewModel.scannedDevices.collectAsState()
    val isScanning by scanViewModel.isScanning.collectAsState()

    var latestDeviceAddress by remember { mutableStateOf<String?>(null) }
    var previousDevices by remember { mutableStateOf(emptyList<DeviceBleModel>()) }

    // Detecta novo dispositivo para animação
    LaunchedEffect(devices) {
        val newDevice = devices.find { d -> previousDevices.none { it.address == d.address } }
        if (newDevice != null) {
            latestDeviceAddress = newDevice.address
        }
        previousDevices = devices
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF121212)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isScanning) "Scanning... (${devices.size})"
                    else "Available Devices (${devices.size})",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF00A86B)
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(devices, key = { it.address }) { device ->
                        val isNew = device.address == latestDeviceAddress
                        val cardColor by animateColorAsState(
                            targetValue = if (isNew)
                                Color(0xFF00A86B).copy(alpha = 0.2f)
                            else
                                Color(0xFF1E1E1E),
                            animationSpec = tween(800)
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable {
                                    navController.navigate("${AppDestinations.DEVICE_OPTION_TREE_SCREEN}?deviceMac=${device.address}")
                                },
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(device.name.ifEmpty { "Unnamed Device" }, color = Color.White)
                                Text("Address: ${device.address}", color = Color.LightGray)
                                Text("RSSI: ${device.rssi} dBm", color = Color.Gray)
                                if (device.uuids.isNotEmpty()) {
                                    Text(
                                        "Services: ${device.uuids.joinToString()}",
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        if (isNew) {
                            LaunchedEffect(device.address) {
                                delay(800)
                                latestDeviceAddress = null
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botões Stop / Back
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 48.dp), // Ajuste para não ficar atrás da barra inferior
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { scanViewModel.stopScan() },
                        enabled = isScanning,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00A86B),
                            contentColor = Color.White
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Stop")
                    }

                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
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
    }

    // Lifecycle-aware scanning
    DisposableEffect(Unit) {
        scanViewModel.startScan()
        onDispose { scanViewModel.stopScan() }
    }
}
