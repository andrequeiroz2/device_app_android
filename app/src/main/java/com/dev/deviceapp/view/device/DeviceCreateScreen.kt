package com.dev.deviceapp.view.device

import android.bluetooth.BluetoothManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material3.DropdownMenuItem
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.dev.deviceapp.model.device.DeviceBleInfoModel
import com.dev.deviceapp.model.device.DeviceCreateRequest
import com.dev.deviceapp.model.device.DeviceMessageCreateRequest
import com.dev.deviceapp.ui.theme.components.DetailRow
import com.dev.deviceapp.ui.theme.components.DividerSpace
import com.dev.deviceapp.ui.theme.components.EditableDetailRow
import com.dev.deviceapp.viewmodel.device.DeviceCreateUiState
import com.dev.deviceapp.viewmodel.device.DeviceCreateViewModel
import com.dev.deviceapp.viewmodel.device.DeviceOptionsUiState
import com.dev.deviceapp.viewmodel.device.DeviceOptionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.RequiresPermission(
    allOf = [android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT]
)
@Composable
fun DeviceCreateScreen(
    navController: NavController,
    mac: String?,
    optionsViewModel: DeviceOptionsViewModel = hiltViewModel(),
    deviceCreateViewModel: DeviceCreateViewModel = hiltViewModel(),
    onLogout: () -> Unit = {}
) {
    val tokenInfo = remember { deviceCreateViewModel.getTokenInfo() }
    if (tokenInfo == null) {
        Log.e("ProfileScreen", "Unauthorized")
        LaunchedEffect(Unit) { onLogout() }
        return
    }

    val context = LocalContext.current

    var deviceName by remember { mutableStateOf("") }
    var qos by remember { mutableStateOf("1") }
    var retained by remember { mutableStateOf("true") }
    var publisher by remember { mutableStateOf("true") }
    var subscriber by remember { mutableStateOf("true") }
    var commandStart by remember { mutableStateOf("1") }
    var commandEnd by remember { mutableStateOf("0") }
    var bleValidationPassed by remember { mutableStateOf(false) }

    val createState by deviceCreateViewModel.state.collectAsState()
    val state by optionsViewModel.uiState.collectAsState()

    val isAdoptEnabled = deviceName.isNotBlank() &&
            commandStart.isNotBlank() &&
            commandEnd.isNotBlank() &&
            tokenInfo.uuid.isNotBlank() &&
            bleValidationPassed

    LaunchedEffect(createState) {
        when (createState) {
            is DeviceCreateUiState.Success -> {
                Toast.makeText(
                    context,
                    "Device adopted successfully.",
                    Toast.LENGTH_LONG
                ).show()
            }

            is DeviceCreateUiState.Error -> {
                Log.e("DeviceCreateScreen", (createState as DeviceCreateUiState.Error).message)
                Toast.makeText(
                    context,
                    (createState as DeviceCreateUiState.Error).message,
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> Unit
        }
    }

    LaunchedEffect(mac) {
        mac?.let {
            val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
            val bt = bluetoothManager?.adapter
            val device = bt?.getRemoteDevice(mac)
            if (device != null) optionsViewModel.loadDevice(device, context)
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
                    text = "Device Adoption",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFF00A86B)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (val s = state) {
                DeviceOptionsUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF00A86B),
                            strokeWidth = 4.dp
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Reading device info...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                is DeviceOptionsUiState.AdoptAvailable -> {
                    val info = s.device

                    LaunchedEffect(info.mac_address, info.device_uuid, info.adopted_status) {
                        val validationError = validateBleData(info)
                        if (validationError != null) {
                            bleValidationPassed = false
                            Log.e("DeviceCreateScreen", validationError.logMessage)
                            Toast.makeText(context, validationError.toastMessage, Toast.LENGTH_LONG)
                                .show()
                            navController.popBackStack()
                            return@LaunchedEffect
                        }

                        bleValidationPassed = true
                        if (deviceName.isBlank() && info.device_name.isNotBlank()) {
                            deviceName = info.device_name
                        }
                    }

                    if (bleValidationPassed) {
                        DeviceAdoptionForm(
                            info = info,
                            userUuid = tokenInfo.uuid,
                            deviceName = deviceName,
                            onDeviceNameChange = { deviceName = it },
                            qos = qos,
                            onQosChange = { qos = it },
                            retained = retained,
                            onRetainedChange = { retained = it },
                            publisher = publisher,
                            onPublisherChange = { publisher = it },
                            subscriber = subscriber,
                            onSubscriberChange = { subscriber = it },
                            commandStart = commandStart,
                            onCommandStartChange = { value ->
                                if (value.all { it.isDigit() } || value.isBlank()) {
                                    commandStart = value
                                }
                            },
                            commandEnd = commandEnd,
                            onCommandEndChange = { value ->
                                if (value.all { it.isDigit() } || value.isBlank()) {
                                    commandEnd = value
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                val sensorType = info.sensor_type.takeIf { it.isNotBlank() }
                                val actuatorType = info.actuator_type.takeIf { it.isNotBlank() }

                                val device_scale_list: List<List<String>> =
                                    info.device_scale.map { item ->
                                        listOf(item[0], item[1])
                                    }

                                Log.d(
                                    "DeviceCreateScreen",
                                    "Sending payload: ble_info=${info}, " +
                                            "sensor=$sensorType, " +
                                            "actuator=$actuatorType, " +
                                            "name=$deviceName"
                                )

                                deviceCreateViewModel.createDevice(
                                    DeviceCreateRequest(
                                        name = deviceName,
                                        device_type_str = info.device_type,
                                        board_type_str = info.boarder_type,
                                        sensor_type = sensorType,
                                        actuator_type = actuatorType,
                                        adopted_status = "adopted",
                                        mac_address = info.mac_address,
                                        scale = device_scale_list,
                                        message = DeviceMessageCreateRequest(
                                            qos = qos.toIntOrNull() ?: 1,
                                            retained = retained.equals("true", ignoreCase = true),
                                            publisher = publisher.equals("true", ignoreCase = true),
                                            subscriber = subscriber.equals("true", ignoreCase = true),
                                            command_start = commandStart.toIntOrNull() ?: 1,
                                            command_end = commandEnd.toIntOrNull() ?: 0
                                        )
                                    )
                                )
                            },
                            enabled = isAdoptEnabled && createState !is DeviceCreateUiState.Loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00A86B),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF666666),
                                disabledContentColor = Color(0xFF999999)
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Adopt")
                        }

                        if (createState is DeviceCreateUiState.Loading) {
                            Spacer(modifier = Modifier.height(16.dp))
                            CircularProgressIndicator(
                                color = Color(0xFF00A86B),
                                strokeWidth = 4.dp
                            )
                        }
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
                    Text(
                        text = "This device already belongs to you",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF00A86B)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                is DeviceOptionsUiState.Error -> {
                    Text(
                        text = "Error reading device info",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.height(20.dp))
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
fun DeviceAdoptionForm(
    info: DeviceBleInfoModel,
    userUuid: String,
    deviceName: String,
    onDeviceNameChange: (String) -> Unit,
    qos: String,
    onQosChange: (String) -> Unit,
    retained: String,
    onRetainedChange: (String) -> Unit,
    publisher: String,
    onPublisherChange: (String) -> Unit,
    subscriber: String,
    onSubscriberChange: (String) -> Unit,
    commandStart: String,
    onCommandStartChange: (String) -> Unit,
    commandEnd: String,
    onCommandEndChange: (String) -> Unit
) {
    DetailRow("MAC Address", formatBleValue(info.mac_address))
    DividerSpace()

    DetailRow("Board Type", formatBleValue(info.boarder_type))
    DividerSpace()

    DetailRow("Device Type", formatBleValue(info.device_type))
    DividerSpace()

    DetailRow("Sensor Type", formatBleValue(info.sensor_type))
    DividerSpace()

    DetailRow("Actuator Type", formatBleValue(info.actuator_type))
    DividerSpace()

    val scaleText = info.device_scale.takeIf { it.isNotEmpty() }?.joinToString("\n") { row ->
        row.joinToString(" = ")
    } ?: "---"
    DetailRow("Device Scale", scaleText)
    DividerSpace()

    DetailRow(
        "Adoption Status",
        if (info.adopted_status == 0) "Unadopted" else "Adopted",
    )
    DividerSpace()

    DetailRow("User UUID", formatBleValue(userUuid))
    DividerSpace()

    EditableDetailRow(
        label = "Device Name",
        value = deviceName,
        onValueChange = onDeviceNameChange,
        placeholder = "Enter device name"
    )
    DividerSpace()

    DropdownSelector(
        label = "QoS",
        options = listOf("0", "1", "2", "3"),
        selectedOption = qos,
        onOptionSelected = onQosChange
    )
    DividerSpace()

    DropdownSelector(
        label = "Retained",
        options = listOf("true", "false"),
        selectedOption = retained,
        onOptionSelected = onRetainedChange
    )
    DividerSpace()

    DropdownSelector(
        label = "Publisher",
        options = listOf("true", "false"),
        selectedOption = publisher,
        onOptionSelected = onPublisherChange
    )
    DividerSpace()

    DropdownSelector(
        label = "Subscriber",
        options = listOf("true", "false"),
        selectedOption = subscriber,
        onOptionSelected = onSubscriberChange
    )
    DividerSpace()

    EditableDetailRow(
        label = "Command Start",
        value = commandStart,
        onValueChange = onCommandStartChange,
        placeholder = "1"
    )
    DividerSpace()

    EditableDetailRow(
        label = "Command End",
        value = commandEnd,
        onValueChange = onCommandEndChange,
        placeholder = "1"
    )
    DividerSpace()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSelector(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF00A86B)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        ) {
            androidx.compose.material3.OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.White,
                    disabledBorderColor = Color(0xFF666666),
                    disabledContainerColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            androidx.compose.material3.DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = Color.White) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

private data class BleValidationError(
    val logMessage: String,
    val toastMessage: String
)

private fun validateBleData(info: DeviceBleInfoModel): BleValidationError? {
    val missingFields = mutableListOf<String>()

    if (info.mac_address.isBlank()) missingFields.add("MAC Address")
    if (info.boarder_type.isBlank()) missingFields.add("Board Type")
    if (info.adopted_status_desc.isBlank()) missingFields.add("Adopted Status")

    if (missingFields.isNotEmpty()) {
        return BleValidationError(
            logMessage = "Inconsistent device data. Missing required fields: ${missingFields.joinToString()}",
            toastMessage = "Inconsistent device data"
        )
    }

    if (info.adopted_status != 0) {
        return BleValidationError(
            logMessage = "Inconsistent device, `Adopted Status`: ${info.adopted_status}",
            toastMessage = "Inconsistent device data"
        )
    }

    if (info.sensor_type.isBlank() && info.actuator_type.isBlank()) {
        return BleValidationError(
            logMessage = "Device data is inconsistent when sensor_type and actuator_type are null.",
            toastMessage = "Inconsistent device data"
        )
    }

    return null
}

private fun formatBleValue(value: String?): String =
    value?.takeIf { it.isNotBlank() } ?: "---"
