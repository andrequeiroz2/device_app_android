package com.dev.deviceapp.view.device

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.json.JSONObject
import java.util.*

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
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val characteristicUuid = UUID.fromString("19b10003-e8f2-537e-4f6c-d104768a1214")
    val serviceUuid = UUID.fromString("19b10000-e8f2-537e-4f6c-d104768a1214")

    LaunchedEffect(deviceAddress) {
        if (deviceAddress.isNullOrEmpty()) {
            errorMessage = "Invalid device address"
            isLoading = false
            return@LaunchedEffect
        }

        val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
        val adapter = bluetoothManager?.adapter
        val device = adapter?.getRemoteDevice(deviceAddress)

        if (device == null) {
            errorMessage = "Device not found"
            isLoading = false
            return@LaunchedEffect
        }

        connectAndInteractWithDevice(
            context = context,
            device = device,
            serviceUuid = serviceUuid,
            characteristicUuid = characteristicUuid,
            onResult = { json: JSONObject ->
                deviceInfo = json
                isLoading = false
            },
            onError = { err: String ->
                errorMessage = err
                isLoading = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Info") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                errorMessage != null -> Text("Error: $errorMessage")
                deviceInfo != null -> {
                    Column {
                        Text("📡 Device Information", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(deviceInfo!!.toString(4)) // imprime JSON formatado
                    }
                }
                else -> Text("No data received yet.")
            }
        }
    }
}

/**
 * Conecta ao dispositivo BLE, escreve dados em uma característica,
 * lê a resposta e retorna o JSON.
 */
@RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
private fun connectAndInteractWithDevice(
    context: Context,
    device: BluetoothDevice,
    serviceUuid: UUID,
    characteristicUuid: UUID,
    onResult: (JSONObject) -> Unit,
    onError: (String) -> Unit
) {
    val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    val adapter = bluetoothManager?.adapter
    if (adapter == null || !adapter.isEnabled) {
        onError("Bluetooth is disabled or unavailable")
        return
    }

    val callback = object : BluetoothGattCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("BLE", "✅ Connected to GATT, discovering services...")
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.e("BLE", "❌ Disconnected from GATT")
                gatt.close()
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                onError("Service discovery failed")
                gatt.close()
                return
            }

            val service = gatt.getService(serviceUuid)
            val characteristic = service?.getCharacteristic(characteristicUuid)

            if (characteristic == null) {
                onError("Characteristic not found")
                gatt.close()
                return
            }

            val payload = JSONObject(mapOf("request" to "device_info"))
            val data = payload.toString().encodeToByteArray()

            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic.value = data

            val success = gatt.writeCharacteristic(characteristic)
            if (!success) {
                onError("Failed to write characteristic")
                gatt.close()
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            handleCharacteristicRead(gatt, characteristic, status, onResult, onError)
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("BLE", "✍️ Characteristic write successful, reading response...")
                gatt.readCharacteristic(characteristic)
                Log.i("BLE", "✍️ PASSEI...")
            } else {
                onError("Characteristic write failed")
                gatt.close()
            }
        }
    }

    device.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE)
}

/**
 * Processa a leitura da característica e extrai JSON da resposta
 */
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
private fun handleCharacteristicRead(
    gatt: BluetoothGatt,
    characteristic: BluetoothGattCharacteristic,
    status: Int,
    onResult: (JSONObject) -> Unit,
    onError: (String) -> Unit
) {
    if (status == BluetoothGatt.GATT_SUCCESS) {
        val data = characteristic.value?.decodeToString()
        Log.i("BLE", "📨 Read data: $data")
        if (!data.isNullOrEmpty()) {
            try {
                val json = JSONObject(data)
                onResult(json)
            } catch (e: Exception) {
                onError("Invalid JSON response: ${e.message}")
            }
        } else {
            onError("Empty response")
        }
    } else {
        onError("Failed to read characteristic")
    }
    gatt.close()
}