package com.dev.deviceapp.view.device

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.Manifest
import kotlinx.coroutines.delay
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import com.dev.deviceapp.model.device.DeviceBleModel
import com.dev.deviceapp.repository.device.BluetoothScanner


@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviveBleScanListScreen(
    scanner: BluetoothScanner,
    onBack: () -> Unit,
    onDeviceClick: (DeviceBleModel) -> Unit // Novo callback ao clicar em um device
) {
    var devices by remember { mutableStateOf(listOf<DeviceBleModel>()) }
    var isScanning by remember { mutableStateOf(false) }
    var latestDeviceAddress by remember { mutableStateOf<String?>(null) }

    // Verifica permissão BLE
    val hasPermission = ContextCompat.checkSelfPermission(
        scanner.context,
        Manifest.permission.BLUETOOTH_SCAN
    ) == PackageManager.PERMISSION_GRANTED

    DisposableEffect(Unit) {
        if (hasPermission) {
            isScanning = true

            scanner.startScan { bleDevice ->
                if (devices.none { it.address == bleDevice.address }) {
                    devices = devices + bleDevice
                    latestDeviceAddress = bleDevice.address
                }
            }
        }

        onDispose {
            scanner.stopScan()
            isScanning = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isScanning) "Scanning... (${devices.size} found)"
                else "Available Devices (${devices.size})",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(devices) { device ->
                    val isNew = device.address == latestDeviceAddress

                    val cardColor by animateColorAsState(
                        targetValue = if (isNew)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        animationSpec = tween(durationMillis = 800)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onDeviceClick(device) }, // Clicável!
                        colors = CardDefaults.cardColors(
                            containerColor = cardColor
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Name: ${device.name}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Address: ${device.address}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "RSSI: ${device.rssi} dBm",
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (device.uuids.isNotEmpty()) {
                                Text(
                                    text = "Services: ${device.uuids.joinToString()}",
                                    style = MaterialTheme.typography.bodySmall
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scanner.stopScan()
                        isScanning = false
                    },
                    enabled = isScanning,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Stop Scan")
                }

                Button(
                    onClick = {
                        scanner.stopScan()
                        onBack()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Back")
                }
            }
        }
    }
}

