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
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.dev.deviceapp.AppDestinations
import com.dev.deviceapp.model.device.DeviceBleModel
import com.dev.deviceapp.repository.device.BluetoothScanner
import com.dev.deviceapp.viewmodel.profile.ProfileViewModel



@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviveBleScanListScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
    scanner: BluetoothScanner,
    onBack: () -> Unit,
    onDeviceClick: (DeviceBleModel) -> Unit,
    onLogout: () -> Unit = {}
) {

    val context = LocalContext.current
    var devices by remember { mutableStateOf(listOf<DeviceBleModel>()) }
    var isScanning by remember { mutableStateOf(false) }
    var latestDeviceAddress by remember { mutableStateOf<String?>(null) }
    val tokenInfo = viewModel.tokenInfo

    Log.d("DeviveBleScanListScreen", "$tokenInfo")

    val hasPermission = ContextCompat.checkSelfPermission(
        scanner.context,
        Manifest.permission.BLUETOOTH_SCAN
    ) == PackageManager.PERMISSION_GRANTED

    if (tokenInfo == null) {
        Log.e("DeviveBleScanListScreen", "Token is null")
        LaunchedEffect(Unit) { onLogout() }
        return
    }

    DisposableEffect(Unit) {
        if (hasPermission) {
            isScanning = true

            scanner.startScan { bleDevice ->
                if (devices.none { it.address == bleDevice.address }) {
                    devices = devices + bleDevice
                    latestDeviceAddress = bleDevice.address
                }
            }
        } else {
            Log.e("DeviceBleScanListScreen", "Missing Bluetooth permissions")
            Toast.makeText(
                context,
                "This app requires Bluetooth permissions. Please enable them in your settings",
                Toast.LENGTH_LONG
            ).show()
            navController.navigate(AppDestinations.MAIN_SCREEN)
        }

        onDispose {
            scanner.stopScan()
            isScanning = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF121212)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isScanning) "Scanning... (${devices.size})"
                    else "Available Devices (${devices.size})",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFF00A86B)
                )

                Spacer(modifier = Modifier.height(24.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(devices) { device ->
                        val isNew = device.address == latestDeviceAddress
                        val cardColor by animateColorAsState(
                            targetValue = if (isNew)
                                Color(0xFF00A86B).copy(alpha = 0.2f)
                            else
                                Color(0xFF1E1E1E),
                            animationSpec = tween(800)
                        )

                        Log.i("DeviveBleScanListScreen", "$device")

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable {
                                    onDeviceClick(device)
                                },
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = device.name.ifEmpty { "Unnamed Device" },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Address: ${device.address}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.LightGray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "RSSI: ${device.rssi} dBm",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                if (device.uuids.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Services: ${device.uuids.joinToString()}",
                                        style = MaterialTheme.typography.bodySmall,
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            scanner.stopScan()
                            isScanning = false
                        },
                        enabled = isScanning,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00A86B),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Stop")
                    }

                    Button(
                        onClick = {
                            scanner.stopScan()
                            onBack()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00A86B),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Back")
                    }
                }
            }
        }
    }
}