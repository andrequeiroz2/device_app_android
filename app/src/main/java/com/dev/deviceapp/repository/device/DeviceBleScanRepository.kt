package com.dev.deviceapp.repository.device


import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dev.deviceapp.model.device.DeviceBleModel
import androidx.core.util.size

@SuppressLint("MissingPermission")
class BluetoothScanner(val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val scanner = bluetoothAdapter?.bluetoothLeScanner

    private val _devices = MutableLiveData<List<DeviceBleModel>>(emptyList())
    val devices: LiveData<List<DeviceBleModel>> = _devices

    private val validPrefixes = listOf("ESP32_", "RASPBERRY_", "GENERIC_")

    private var activeCallback: ScanCallback? = null

    fun startScan(onDeviceFound: (DeviceBleModel) -> Unit) {
        bluetoothAdapter?.takeIf { it.isEnabled }?.let {
            val callback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    processResult(result, onDeviceFound)
                }

                override fun onBatchScanResults(results: MutableList<ScanResult>) {
                    results.forEach { processResult(it, onDeviceFound) }
                }

                override fun onScanFailed(errorCode: Int) {
                    // Log ou tratamento de erro
                }
            }

            activeCallback = callback
            scanner?.startScan(callback)
        }
    }

    fun stopScan() {
        activeCallback?.let {
            scanner?.stopScan(it)
            activeCallback = null
        }
    }

    private fun processResult(result: ScanResult, onDeviceFound: (DeviceBleModel) -> Unit) {
        val device = result.device
        val deviceName = device.name ?: return
        val currentList = _devices.value ?: emptyList()

        if (validPrefixes.any { deviceName.startsWith(it) } &&
            currentList.none { it.address == device.address }) {

            val bleDevice = DeviceBleModel(
                name = deviceName,
                address = device.address ?: "N/A",
                rssi = result.rssi,
                uuids = result.scanRecord?.serviceUuids?.map { it.toString() } ?: emptyList(),
                deviceType = device.type,
                manufacturerData = result.scanRecord?.manufacturerSpecificData?.let { msd ->
                    val size = msd.size
                    val arr = ByteArray(size)
                    for (i in 0 until size) {
                        arr[i] = msd.valueAt(i)[0] // pega o primeiro byte de cada entry
                    }
                    arr
                }
            )

            _devices.postValue(currentList + bleDevice)
            onDeviceFound(bleDevice)
        }
    }
}