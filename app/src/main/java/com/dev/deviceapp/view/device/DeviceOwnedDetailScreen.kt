package com.dev.deviceapp.view.device

import android.util.Log
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.dev.deviceapp.ui.theme.components.DetailRow
import com.dev.deviceapp.viewmodel.device.DeviceOwnedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceOwnedDetailScreen(
    navController: NavController,
    deviceOwnedViewModel: DeviceOwnedViewModel = hiltViewModel()
) {
    val deviceUuid = remember {
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.get<String>("deviceUuid")
    }
    
    var device by remember { mutableStateOf<com.dev.deviceapp.database.device.DeviceEntity?>(null) }
    
    LaunchedEffect(deviceUuid) {
        deviceUuid?.let { uuid ->
            Log.i("DeviceOwnedDetailScreen", "Loading device with UUID: $uuid")
            val foundDevice = deviceOwnedViewModel.getDeviceByUuid(uuid)
            Log.i("DeviceOwnedDetailScreen", "Device found: ${foundDevice?.uuid ?: "null"}")
            device = foundDevice
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
            
            device?.let { dev ->
                DetailRow(label = "Name", value = dev.name)
                Spacer(modifier = Modifier.height(16.dp))
                
                DetailRow(label = "UUID", value = dev.uuid)
                Spacer(modifier = Modifier.height(16.dp))
                
                DetailRow(label = "MAC Address", value = dev.macAddress)
                Spacer(modifier = Modifier.height(16.dp))
                
                DetailRow(label = "Device Type", value = dev.deviceTypeText)
                Spacer(modifier = Modifier.height(16.dp))
                
                DetailRow(label = "Board Type", value = dev.boardTypeText)
                Spacer(modifier = Modifier.height(16.dp))
                
                dev.sensorType?.let {
                    DetailRow(label = "Sensor Type", value = it)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                dev.actuatorType?.let {
                    DetailRow(label = "Actuator Type", value = it)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                DetailRow(label = "Condition", value = dev.deviceConditionText)
                Spacer(modifier = Modifier.height(16.dp))
                
                DetailRow(label = "Topic", value = dev.topic)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Messages section
                dev.messages?.let { messages ->
                    if (messages.isNotEmpty()) {
                        Text(
                            text = "Messages",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF00A86B),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        messages.forEach { messageEntity ->
                            messageEntity.messages?.forEach { (key, received) ->
                                DetailRow(
                                    label = key,
                                    value = "${received.value}${received.scale}",
                                    valueColor = Color(0xFF00A86B)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
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
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Device not found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Red
                    )
                }
            }
        }
    }
}

