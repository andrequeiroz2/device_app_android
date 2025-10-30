package com.dev.deviceapp.view.device

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dev.deviceapp.ble.BleConstants
import com.dev.deviceapp.model.device.DeviceBleInfoModel
import kotlinx.coroutines.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun DeviceCharacteristicReadInfoScreen(
    navController: NavController,
    deviceAddress: String?,
    context: Context
) {
    var deviceInfo by remember { mutableStateOf<JSONObject?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val serviceUuid = BleConstants.BLE_SERVICE_UUID
    val characteristicUuid = BleConstants.BLE_DEVICE_INFO_UUID

    LaunchedEffect(deviceAddress) {
        if (deviceAddress.isNullOrEmpty()) {
            Toast.makeText(context, "Invalid device address", Toast.LENGTH_LONG).show()
            isLoading = false
            return@LaunchedEffect
        }

        val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
        val adapter = bluetoothManager?.adapter
        val device = adapter?.getRemoteDevice(deviceAddress)

        if (device == null) {
            Toast.makeText(context, "Device not found", Toast.LENGTH_LONG).show()
            isLoading = false
            return@LaunchedEffect
        }

        try {
            deviceInfo = readDeviceInfo(
                context = context,
                device = device,
                serviceUuid = serviceUuid,
                characteristicUuid = characteristicUuid
            )
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("BLE", "Error reading device info", e)
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Information", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF00A86B)) // verde
                }
            } else if (deviceInfo != null) {
                val parsedDevice = remember(deviceInfo) {
                    try {
                        Json.decodeFromString<DeviceBleInfoModel>(deviceInfo.toString())
                    } catch (e: Exception) {
                        Log.e("BLE", "Error parsing device info $deviceInfo", e)
                        null
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    parsedDevice?.let { device ->
                        DetailRow("Boarder Type", device.boarder_type)
                        DetailRow("MAC Address", device.mac_address)
                        DetailRow("Device Type", device.device_type)
                        DetailRow("Sensor Type", device.sensor_type)
                        DetailRow("Actuator Type", device.actuator_type)
                        DetailRow("Adopted Status", device.adopted_status.toString())
                        DetailRow("Adopted Status Desc", device.adopted_status_desc)
                        DetailRow("Broker URL", device.broker_url)
                        DetailRow("Topic", device.topic)
                        DetailRow("User UUID", device.user_uuid)
                        DetailRow("Device UUID", device.device_uuid)
                        DetailRow("Device Name", device.device_name)

                        if (device.device_scale.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Device Scale:",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF00A86B)
                            )
                            device.device_scale.forEach { scale ->
                                Text(
                                    text = "- ${scale.joinToString(" → ")}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, valueColor: Color = Color.White) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color(0xFF00A86B))
        Text(text = value.ifEmpty { "—" }, style = MaterialTheme.typography.bodyMedium, color = valueColor)
    }
}

@RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
suspend fun readDeviceInfo(
    context: Context,
    device: BluetoothDevice,
    serviceUuid: UUID,
    characteristicUuid: UUID,
    timeoutMillis: Long = 25000L
): JSONObject = withContext(Dispatchers.IO) {
    withTimeout(timeoutMillis) {
        suspendCancellableCoroutine { cont ->
            var gatt: BluetoothGatt? = null
            val receivedData = StringBuilder()
            val mainHandler = Handler(Looper.getMainLooper())
            var operationCompleted = false
            var chunkTimeoutRunnable: Runnable? = null

            fun cleanup() {
                chunkTimeoutRunnable?.let { mainHandler.removeCallbacks(it) }
                gatt?.disconnect()
                mainHandler.postDelayed({ gatt?.close() }, 300)
            }

            fun resetChunkTimeout() {
                chunkTimeoutRunnable?.let { mainHandler.removeCallbacks(it) }
                chunkTimeoutRunnable = Runnable {
                    if (!operationCompleted) {
                        operationCompleted = true
                        val currentData = receivedData.toString()
                        if (currentData.isNotEmpty()) {
                            try {
                                val fallbackJson = JSONObject().apply {
                                    put("raw_data", currentData)
                                    put("partial_data", true)
                                    put("received_chars", currentData.length)
                                }
                                cont.resume(fallbackJson)
                            } catch (e: Exception) {
                                Log.e("BLE", "Incomplete data received", e)
                                cont.resumeWithException(Exception("Incomplete data received"))
                            }
                        } else {
                            Log.e("BLE", "No data received from device")
                            cont.resumeWithException(Exception("No data received from device"))
                        }
                        cleanup()
                    }
                }
                mainHandler.postDelayed(chunkTimeoutRunnable, 3000)
            }

            val gattCallback = object : BluetoothGattCallback() {
                @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                override fun onConnectionStateChange(gattLocal: BluetoothGatt, status: Int, newState: Int) {
                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        if (!operationCompleted) {
                            operationCompleted = true
                            cont.resumeWithException(Exception("Connection failed: $status"))
                        }
                        cleanup()
                        return
                    }
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        gattLocal.discoverServices()
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        if (!operationCompleted) {
                            operationCompleted = true
                            cont.resumeWithException(Exception("Device disconnected"))
                        }
                        cleanup()
                    }
                }

                @RequiresApi(Build.VERSION_CODES.TIRAMISU)
                @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                override fun onServicesDiscovered(gattLocal: BluetoothGatt, status: Int) {
                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        if (!operationCompleted) {
                            operationCompleted = true
                            cont.resumeWithException(Exception("Service discovery failed"))
                        }
                        cleanup()
                        return
                    }

                    val service = gattLocal.getService(serviceUuid)
                    val characteristic = service?.getCharacteristic(characteristicUuid)
                    if (characteristic == null) {
                        if (!operationCompleted) {
                            operationCompleted = true
                            cont.resumeWithException(Exception("Characteristic not found"))
                        }
                        cleanup()
                        return
                    }

                    gattLocal.setCharacteristicNotification(characteristic, true)
                    val bleClientUuid = BleConstants.BLE_CLIENT_UUID
                    val descriptor = characteristic.getDescriptor(bleClientUuid)
                    descriptor?.let {
                        gattLocal.writeDescriptor(it, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                    }

                    mainHandler.postDelayed({
                        val command = "get_info"
                        val status = gattLocal.writeCharacteristic(
                            characteristic,
                            command.encodeToByteArray(),
                            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                        )
                        if (status != BluetoothStatusCodes.SUCCESS) {
                            if (!operationCompleted) {
                                operationCompleted = true
                                cont.resumeWithException(Exception("Failed to write characteristic, status: $status"))
                            }
                            cleanup()
                        }
                    }, 500)
                }

                @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                override fun onCharacteristicChanged(
                    gattLocal: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic,
                    value: ByteArray
                ) {
                    val chunk = value.decodeToString()

                    if (chunk.isNotEmpty()) {
                        receivedData.append(chunk)
                        try {
                            val json = JSONObject(receivedData.toString())
                            if (!operationCompleted) {
                                operationCompleted = true
                                cont.resume(json)
                                cleanup()
                            }
                        } catch (e: Exception) {
                            Log.e("BLE", "Incomplete data received", e)
                            resetChunkTimeout()
                        }
                    }
                }
            }

            gatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)

            cont.invokeOnCancellation {
                operationCompleted = true
                cleanup()
            }
        }
    }
}
