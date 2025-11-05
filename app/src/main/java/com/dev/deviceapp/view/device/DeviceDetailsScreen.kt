package com.dev.deviceapp.view.device

import android.bluetooth.BluetoothManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.dev.deviceapp.model.device.DeviceBleInfoModel
import com.dev.deviceapp.viewmodel.device.DeviceOptionsUiState
import com.dev.deviceapp.viewmodel.device.DeviceOptionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT])
@Composable
fun DeviceDetailsScreen(
    navController: NavController,
    mac: String?,
    vm: DeviceOptionsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by vm.uiState.collectAsState()

    LaunchedEffect(mac) {
        mac?.let {
            val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
            val bt = bluetoothManager?.adapter
            val device = bt?.getRemoteDevice(mac)
            if (device != null) vm.loadDevice(device, context)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF121212)
    ) {

        Column(
            modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .statusBarsPadding()
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Device Details",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFF00A86B)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            when(val s = state) {

                DeviceOptionsUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text("Reading device info...", color = Color.White)
                    }
                }

                is DeviceOptionsUiState.AdoptAvailable -> {
                    DeviceInfoReadOnly(info = s.device)

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            // future: vm.adoptDevice()
                            navController.popBackStack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00A86B),
                            contentColor = Color.White
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Confirm Adoption")
                    }
                }

                is DeviceOptionsUiState.Unauthorized -> {
                    Text(
                        text = "Device already adopted by another user",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Red
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }

                is DeviceOptionsUiState.Owner -> {
                    DeviceInfoReadOnly(info = s.device)

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00A86B),
                            contentColor = Color.White
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("This device already belongs to you")
                    }
                }

                else -> {
                    Text("Error reading device info", color = Color.Red)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
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

@Composable
fun DeviceInfoReadOnly(info: DeviceBleInfoModel) {
    DetailRow("Device UUID", info.device_uuid)
    DividerSpace()
    DetailRow("Device Name", info.device_name)
    DividerSpace()
    DetailRow("MAC Address", info.mac_address)
    DividerSpace()
    DetailRow("Board Type", info.boarder_type)
    DividerSpace()
    DetailRow("Device Type", info.device_type)
    DividerSpace()
    DetailRow("Sensor Type", info.sensor_type)
    DividerSpace()
    DetailRow("Actuator Type", info.actuator_type)
    DividerSpace()
    DetailRow("Broker URL", info.broker_url)
    DividerSpace()
    DetailRow("MQTT Topic", info.topic)
    DividerSpace()
    val scaleText = info.device_scale.joinToString("\n") { row ->
        row.joinToString(" = ")
    }
    DetailRow("Device Scale", scaleText)
    DividerSpace()
    DetailRow(
        "Adoption Status",
        if (info.adopted_status == 0) "Unadopted" else "Adopted",
    )
    DividerSpace()
    DetailRow("User UUID", info.user_uuid)
}

@Composable
fun DetailRow(label: String, value: String, valueColor: Color = Color.White) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF00A86B)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor
        )
    }
}

@Composable
fun DividerSpace() {
    Spacer(Modifier.height(16.dp))
}