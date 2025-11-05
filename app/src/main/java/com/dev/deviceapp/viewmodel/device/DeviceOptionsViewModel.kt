package com.dev.deviceapp.viewmodel.device

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.deviceapp.ble.BleConstants
import com.dev.deviceapp.common.ConfigLoader
import com.dev.deviceapp.model.device.DeviceBleInfoModel
import com.dev.deviceapp.repository.login.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@HiltViewModel
class DeviceOptionsViewModel @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val configLoader: ConfigLoader
) : ViewModel() {

    private val tokenUserId: String? get() = tokenRepository.getTokenInfoRepository()?.uuid

    private val bleCharacteristicInfo: String = configLoader.getBleCharacteristicInfo()
    private val _uiState = MutableStateFlow<DeviceOptionsUiState>(DeviceOptionsUiState.Loading)
    val uiState: StateFlow<DeviceOptionsUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<DeviceNavigationEvent>()

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    fun loadDevice(device: BluetoothDevice, context: Context) {
        viewModelScope.launch {
            _uiState.value = DeviceOptionsUiState.Loading

            try {
                val json = readDeviceInfo(
                    context,
                    device,
                    BleConstants.BLE_SERVICE_UUID,
                    BleConstants.BLE_DEVICE_INFO_UUID
                )

                val parsed = Json.decodeFromString<DeviceBleInfoModel>(json.toString())
                val userId = tokenUserId

                val newState = when {
                    parsed.user_uuid.isNotEmpty() && parsed.user_uuid != userId ->
                        DeviceOptionsUiState.Unauthorized

                    parsed.adopted_status == 0 ->
                        DeviceOptionsUiState.AdoptAvailable(parsed)

                    parsed.user_uuid == userId ->
                        DeviceOptionsUiState.Owner(parsed)

                    else -> DeviceOptionsUiState.Error("Unexpected state")
                }

                _uiState.value = newState

                if (newState is DeviceOptionsUiState.Owner ||
                    newState is DeviceOptionsUiState.AdoptAvailable) {

                    _navigationEvent.emit(DeviceNavigationEvent.NavigateToOptions)
                }

            } catch (e: Exception) {
                _uiState.value = DeviceOptionsUiState.Error(e.message ?: "Unknown BLE error")
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
    fun adoptDevice(device: BluetoothDevice, context: Context, payload: JSONObject) {
        viewModelScope.launch {
            try {
                writeAdoptionData(
                    context,
                    device,
                    BleConstants.BLE_SERVICE_UUID,
                    BleConstants.BLE_ADOPTION_UUID,
                    BleConstants.BLE_RESPONSE_ADOPTION_UUID,
                    payload
                )

                _navigationEvent.emit(DeviceNavigationEvent.NavigateToOptions)

            } catch (e: Exception) {
                _uiState.value = DeviceOptionsUiState.Error(e.message ?: "BLE adoption failed")
                Log.e("DeviceOptionsViewModel", "BLE adoption failed: ${e.message}")
            }
        }
    }


    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
    private suspend fun readDeviceInfo(
        context: Context,
        device: BluetoothDevice?,
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
                                val fallbackJson = JSONObject()
                                fallbackJson.put("raw_data", currentData)
                                fallbackJson.put("partial_data", true)
                                fallbackJson.put("received_chars", currentData.length)
                                cont.resume(fallbackJson)
                            } else {
                                cont.resumeWithException(Exception("No data received from device"))
                            }
                            cleanup()
                        }
                    }
                    mainHandler.postDelayed(chunkTimeoutRunnable, 3000)
                }

                val gattCallback = object : BluetoothGattCallback() {
                    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                    override fun onConnectionStateChange(
                        gattLocal: BluetoothGatt,
                        status: Int,
                        newState: Int
                    ) {
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
                    override fun onServicesDiscovered(
                        gattLocal: BluetoothGatt,
                        status: Int
                    ) {
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
                        val descriptor = characteristic.getDescriptor(BleConstants.BLE_CLIENT_UUID)
                        descriptor?.let {
                            gattLocal.writeDescriptor(it, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                        }

                        mainHandler.postDelayed({
                            val command = bleCharacteristicInfo
                            val statusWrite = gattLocal.writeCharacteristic(
                                characteristic,
                                command.encodeToByteArray(),
                                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                            )
                            if (statusWrite != BluetoothGatt.GATT_SUCCESS) {
                                if (!operationCompleted) {
                                    operationCompleted = true
                                    cont.resumeWithException(Exception("Failed to write characteristic, status: $statusWrite"))
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
                                Log.e("DeviceOptionsViewModel", "Error parsing JSON: ${e.message}")
                                resetChunkTimeout()
                            }
                        }
                    }
                }

                gatt = device?.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)

                cont.invokeOnCancellation {
                    operationCompleted = true
                    cleanup()
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun writeAdoptionData(
        context: Context,
        device: BluetoothDevice?,
        serviceUuid: UUID,
        writeUuid: UUID,
        notifyUuid: UUID,
        jsonPayload: JSONObject,
        timeoutMillis: Long = 25000L
    ) = withContext(Dispatchers.IO) {
        withTimeout(timeoutMillis) {
            suspendCancellableCoroutine { cont ->
                var gatt: BluetoothGatt? = null
                val mainHandler = Handler(Looper.getMainLooper())
                var operationCompleted = false
                val buffer = StringBuilder()

                fun finish(error: Exception? = null) {
                    if (!operationCompleted) {
                        operationCompleted = true
                        gatt?.disconnect()
                        mainHandler.postDelayed({ gatt?.close() }, 300)
                        if (error != null) cont.resumeWithException(error)
                        else cont.resume(Unit)
                    }
                }

                val callback = object : BluetoothGattCallback() {
                    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                    override fun onConnectionStateChange(gattLocal: BluetoothGatt, status: Int, newState: Int) {
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            gattLocal.discoverServices()
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            finish(Exception("Device disconnected"))
                        }
                    }

                    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
                    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                    override fun onServicesDiscovered(gattLocal: BluetoothGatt, status: Int) {
                        val service = gattLocal.getService(serviceUuid)
                        val writeChar = service?.getCharacteristic(writeUuid)
                        val notifyChar = service?.getCharacteristic(notifyUuid)

                        if (writeChar == null || notifyChar == null) {
                            finish(Exception("BLE adoption characteristics not found"))
                            return
                        }

                        gattLocal.setCharacteristicNotification(notifyChar, true)
                        val descriptor = notifyChar.getDescriptor(BleConstants.BLE_CLIENT_UUID)
                        descriptor?.let {
                            gattLocal.writeDescriptor(it, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                        }

                        mainHandler.postDelayed({
                            val data = jsonPayload.toString().encodeToByteArray()
                            val status = gattLocal.writeCharacteristic(writeChar, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)

                            if (status != BluetoothGatt.GATT_SUCCESS) {
                                finish(Exception("Write failed: $status"))
                            }
                        }, 300)
                    }

                    override fun onCharacteristicChanged(
                        gattLocal: BluetoothGatt,
                        characteristic: BluetoothGattCharacteristic,
                        value: ByteArray
                    ) {
                        val msg = value.decodeToString()
                        buffer.append(msg)

                        if (buffer.isNotEmpty()) {
                            finish()
                        }
                    }
                }

                gatt = device?.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE)
                cont.invokeOnCancellation { finish() }
            }
        }
    }

}

sealed class DeviceNavigationEvent {
    object NavigateToOptions : DeviceNavigationEvent()
}
