package com.dev.deviceapp.repository.device

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.bluetooth.BluetoothGattCallback
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.util.isNotEmpty
import com.dev.deviceapp.common.ConfigLoader
import com.dev.deviceapp.model.device.DeviceBleModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import org.json.JSONObject
import java.util.UUID

@SuppressLint("MissingPermission")
class DeviceBleScanRepository(
    context: Context,
    configLoader: ConfigLoader
) {

    private val bluetoothAdapter: BluetoothAdapter? = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    private val scanner = bluetoothAdapter?.bluetoothLeScanner

    private val _scannedDevices = MutableStateFlow<List<DeviceBleModel>>(emptyList())
    val scannedDevices: StateFlow<List<DeviceBleModel>> = _scannedDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val validPrefixes: List<String> = configLoader.getBleDevicePrefixes()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            processResult(result)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            results.forEach { processResult(it) }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("DeviceBleScanRepository", "Scan failed with error: $errorCode")
            _isScanning.value = false
        }
    }

    fun startScan() {
        if (_isScanning.value || bluetoothAdapter?.isEnabled != true) {
            Log.w("DeviceBleScanRepository", "Scan could not be started. isScanning: ${_isScanning.value}, BT enabled: ${bluetoothAdapter?.isEnabled}")
            return
        }

        _scannedDevices.value = emptyList()

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner?.startScan(null, scanSettings, scanCallback)
        _isScanning.value = true
        Log.i("DeviceBleScanRepository", "BLE scan started.")
    }

    fun stopScan() {
        if (!_isScanning.value) return
        scanner?.stopScan(scanCallback)
        _isScanning.value = false
        Log.i("DeviceBleScanRepository", "BLE scan stopped.")
    }

    private fun processResult(result: ScanResult) {
        val deviceName = result.device.name ?: return

        if (validPrefixes.any { deviceName.startsWith(it) }) {
            val bleDevice = result.toDeviceBleModel()
            _scannedDevices.update { currentList ->
                val existingDevice = currentList.find { it.address == bleDevice.address }
                if (existingDevice == null) {
                    currentList + bleDevice
                } else {
                    // Update existing device info (e.g., RSSI)
                    currentList.map { if (it.address == bleDevice.address) bleDevice else it }
                }
            }
        }
    }

    private fun ScanResult.toDeviceBleModel(): DeviceBleModel {
        return DeviceBleModel(
            name = device.name ?: "Unnamed Device",
            address = device.address ?: "N/A",
            rssi = rssi,
            uuids = scanRecord?.serviceUuids?.map { it.toString() } ?: emptyList(),
            deviceType = device.type,
            manufacturerData = scanRecord?.manufacturerSpecificData?.let { msd ->
                if (msd.isNotEmpty()) {
                    msd.valueAt(0)
                } else {
                    null
                }
            }
        )
    }
}

@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
fun connectAndInteractWithDeviceSafe(
    context: Context,
    device: BluetoothDevice,
    serviceUuid: UUID,
    characteristicUuid: UUID,
    timeoutMillis: Long = 5000L,
    onResult: (JSONObject) -> Unit,
    onError: (String) -> Unit
) {
    val mainScope = CoroutineScope(Dispatchers.Main)

    try {
        val gatt = device.connectGatt(context, false, object : BluetoothGattCallback() {


            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    mainScope.launch { onError("Connection failed: $status") }
                    gatt.close()
                    return
                }

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i("BLE", "Connected, discovering services...")
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.e("BLE", "Disconnected from GATT")
                    gatt.close()
                    mainScope.launch { onError("Device disconnected") }
                }
            }

            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    mainScope.launch { onError("Service discovery failed") }
                    gatt.close()
                    return
                }

                val service = gatt.getService(serviceUuid)
                val characteristic = service?.getCharacteristic(characteristicUuid)

                if (characteristic == null) {
                    mainScope.launch { onError("Characteristic not found") }
                    gatt.close()
                    return
                }

                val payload = JSONObject(mapOf("request" to "device_info"))
                val data = payload.toString().encodeToByteArray()
                characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                characteristic.value = data

                val success = gatt.writeCharacteristic(characteristic)
                if (!success) {
                    mainScope.launch { onError("Failed to write characteristic") }
                    gatt.close()
                }
            }

            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onCharacteristicWrite(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i("BLE", "Write successful, reading response...")
                    gatt.readCharacteristic(characteristic)
                } else {
                    mainScope.launch { onError("Characteristic write failed") }
                    gatt.close()
                }
            }

            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val data = characteristic.value?.decodeToString()
                    Log.i("BLE", "Read data: $data")
                    if (!data.isNullOrEmpty()) {
                        try {
                            val json = JSONObject(data)
                            mainScope.launch { onResult(json) }
                        } catch (e: Exception) {
                            mainScope.launch { onError("Invalid JSON: ${e.message}") }
                        }
                    } else {
                        mainScope.launch { onError("Empty response") }
                    }
                } else {
                    mainScope.launch { onError("Failed to read characteristic") }
                }
                gatt.close()
            }
        }, BluetoothDevice.TRANSPORT_LE)

        // Timeout: cancela se BLE não responder
        mainScope.launch {
            delay(timeoutMillis)
            mainScope.launch { onError("BLE operation timed out") }
            gatt.close()
        }

    } catch (e: Exception) {
        mainScope.launch { onError("BLE exception: ${e.message}") }
    }
}

