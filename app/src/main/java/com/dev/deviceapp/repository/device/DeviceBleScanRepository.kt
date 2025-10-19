package com.dev.deviceapp.repository.device

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

@SuppressLint("MissingPermission")
class BluetoothScanner(val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val scanner = bluetoothAdapter?.bluetoothLeScanner

    private val _devices = MutableLiveData<List<BluetoothDevice>>(emptyList())
    val devices: LiveData<List<BluetoothDevice>> = _devices

    private val validPrefixes = listOf("ESP32_", "RASPBERRY_", "GENERIC_")

    // Armazena o callback ativo para poder parar o scan
    private var activeCallback: ScanCallback? = null

    /**
     * Inicia o scan BLE.
     * Apenas dispositivos cujo nome começa com os prefixos válidos serão repassados no callback
     * e adicionados à LiveData.
     */
    @SuppressLint("MissingPermission")
    fun startScan(onDeviceFound: (ScanResult) -> Unit) {
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

    /**
     * Interrompe o scan ativo, se houver.
     */
    fun stopScan() {
        activeCallback?.let {
            scanner?.stopScan(it)
            activeCallback = null
        }
    }

    /**
     * Processa cada resultado do scan.
     * Atualiza LiveData e repassa ao callback apenas dispositivos válidos.
     */
    private fun processResult(result: ScanResult, onDeviceFound: (ScanResult) -> Unit) {
        val device = result.device
        val deviceName = device.name ?: return
        val currentList = _devices.value ?: emptyList()

        if (validPrefixes.any { deviceName.startsWith(it) } &&
            currentList.none { it.address == device.address }) {

            // Atualiza a lista de dispositivos
            _devices.postValue(currentList + device)

            // Repassa apenas dispositivos válidos
            onDeviceFound(result)
        }
    }
}