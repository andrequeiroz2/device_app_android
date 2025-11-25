package com.dev.deviceapp.viewmodel.device

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
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
import com.dev.deviceapp.model.device.DeviceAdoptionBoard
import com.dev.deviceapp.model.device.DeviceAdoptionResponse
import com.dev.deviceapp.model.device.DeviceCreateRequest
import com.dev.deviceapp.model.token.TokenInfo
import com.dev.deviceapp.repository.device.DeviceCreateRepository
import com.dev.deviceapp.repository.login.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@HiltViewModel
class DeviceCreateViewModel @Inject constructor(
    private val repository: DeviceCreateRepository,
    private val tokenRepository: TokenRepository
): ViewModel(){

    private val _state = MutableStateFlow<DeviceCreateUiState>(DeviceCreateUiState.Idle)
    val state: StateFlow<DeviceCreateUiState> = _state

    fun createDevice(device: DeviceCreateRequest) {
        viewModelScope.launch {

            _state.value = DeviceCreateUiState.Loading

            try {
                val result = repository.createDevice(device)

                _state.value = when (result) {
                    is DeviceAdoptionResponse.Success -> DeviceCreateUiState.Success(
                        result
                    )

                    is DeviceAdoptionResponse.Error -> DeviceCreateUiState.Error(
                        result.errorMessage
                    )
                }
            }catch (e: Exception){
                Log.e("DeviceCreateViewModel", "Error: $e")
                _state.value = DeviceCreateUiState.Error(
                    "Application Error"
                )
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
    fun sendAdoptionDataToDevice(
        context: Context,
        device: BluetoothDevice?,
        adoptionBoard: DeviceAdoptionBoard
    ) {
        viewModelScope.launch {
            try {
                _state.value = DeviceCreateUiState.SendingBleData
                
                val jsonPayload = JSONObject().apply {
                    put("device_name", adoptionBoard.device_name)
                    put("user_uuid", adoptionBoard.user_uuid)
                    put("device_uuid", adoptionBoard.device_uuid)
                    put("broker_url", adoptionBoard.broker_url)
                    put("topic", adoptionBoard.topic)
                }

                val responseCode = writeAdoptionData(
                    context,
                    device,
                    BleConstants.BLE_SERVICE_UUID,
                    BleConstants.BLE_ADOPTION_UUID,
                    BleConstants.BLE_RESPONSE_ADOPTION_UUID,
                    jsonPayload
                )
                
                if (responseCode == "200") {
                    _state.value = DeviceCreateUiState.BleAdoptionSuccess
                } else {
                    _state.value = DeviceCreateUiState.Error(
                        "BLE adoption failed with code: $responseCode"
                    )
                }
            } catch (e: Exception) {
                Log.e("DeviceCreateViewModel", "BLE adoption error: ${e.message}")
                _state.value = DeviceCreateUiState.Error(
                    "BLE adoption error: ${e.message}"
                )
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
    ): String = withContext(Dispatchers.IO) {
        withTimeout(timeoutMillis) {
            suspendCancellableCoroutine { cont ->
                var gatt: BluetoothGatt? = null
                val mainHandler = Handler(Looper.getMainLooper())
                var operationCompleted = false
                val buffer = StringBuilder()
                
                val dataBytes = jsonPayload.toString().encodeToByteArray()
                val chunkSize = 20 // BLE typical chunk size
                val chunks = mutableListOf<ByteArray>()
                for (i in dataBytes.indices step chunkSize) {
                    val end = minOf(i + chunkSize, dataBytes.size)
                    chunks.add(dataBytes.sliceArray(i until end))
                }
                var currentChunkIndex = 0
                var writeChar: BluetoothGattCharacteristic? = null

                fun finish(error: Exception? = null, responseCode: String? = null) {
                    if (!operationCompleted) {
                        operationCompleted = true
                        gatt?.disconnect()
                        mainHandler.postDelayed({ gatt?.close() }, 300)
                        when {
                            error != null -> cont.resumeWithException(error)
                            responseCode != null -> cont.resume(responseCode)
                            else -> cont.resumeWithException(Exception("No response received"))
                        }
                    }
                }

                fun sendNextChunk() {
                    if (currentChunkIndex >= chunks.size) {
                        // All chunks sent, wait for response
                        return
                    }
                    
                    val chunk = chunks[currentChunkIndex]
                    currentChunkIndex++
                    
                    writeChar?.let { char ->
                        val status = gatt?.writeCharacteristic(
                            char,
                            chunk,
                            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                        )
                        
                        if (status != BluetoothGatt.GATT_SUCCESS) {
                            finish(Exception("Write failed: $status"))
                        }
                    }
                }

                val callback = object : BluetoothGattCallback() {
                    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                    override fun onConnectionStateChange(
                        gattLocal: BluetoothGatt,
                        status: Int,
                        newState: Int
                    ) {
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            gattLocal.discoverServices()
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            finish(Exception("Device disconnected"))
                        }
                    }

                    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
                    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                    override fun onServicesDiscovered(gattLocal: BluetoothGatt, status: Int) {
                        if (status != BluetoothGatt.GATT_SUCCESS) {
                            finish(Exception("Service discovery failed"))
                            return
                        }

                        val service = gattLocal.getService(serviceUuid)
                        val writeCharLocal = service?.getCharacteristic(writeUuid)
                        val notifyChar = service?.getCharacteristic(notifyUuid)

                        if (writeCharLocal == null || notifyChar == null) {
                            finish(Exception("BLE adoption characteristics not found"))
                            return
                        }

                        writeChar = writeCharLocal

                        gattLocal.setCharacteristicNotification(notifyChar, true)
                        val descriptor = notifyChar.getDescriptor(BleConstants.BLE_CLIENT_UUID)
                        descriptor?.let {
                            gattLocal.writeDescriptor(it, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                        }

                        mainHandler.postDelayed({
                            sendNextChunk()
                        }, 300)
                    }

                    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                    override fun onCharacteristicWrite(
                        gattLocal: BluetoothGatt,
                        characteristic: BluetoothGattCharacteristic,
                        status: Int
                    ) {
                        if (status != BluetoothGatt.GATT_SUCCESS) {
                            finish(Exception("Write failed: $status"))
                            return
                        }
                        
                        // Send next chunk if available
                        if (currentChunkIndex < chunks.size) {
                            mainHandler.postDelayed({
                                sendNextChunk()
                            }, 50)
                        }
                    }

                    override fun onCharacteristicChanged(
                        gattLocal: BluetoothGatt,
                        characteristic: BluetoothGattCharacteristic,
                        value: ByteArray
                    ) {
                        val msg = value.decodeToString()
                        buffer.append(msg)

                        if (buffer.isNotEmpty()) {
                            val responseCode = buffer.toString().trim()
                            finish(responseCode = responseCode)
                        }
                    }
                }

                gatt = device?.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE)
                cont.invokeOnCancellation { finish() }
            }
        }
    }

    fun getTokenInfo(): TokenInfo? = tokenRepository.getTokenInfoRepository()
}

sealed class DeviceCreateUiState {
    object Idle: DeviceCreateUiState()
    object Loading : DeviceCreateUiState()
    object SendingBleData : DeviceCreateUiState()
    data class Success(val device: DeviceAdoptionResponse.Success) : DeviceCreateUiState()
    object BleAdoptionSuccess : DeviceCreateUiState()
    data class Error(val message: String) : DeviceCreateUiState()
}