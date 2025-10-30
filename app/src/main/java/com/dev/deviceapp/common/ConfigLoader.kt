package com.dev.deviceapp.common

import android.content.Context
import android.util.Log
import java.util.Properties

class ConfigLoader(private val context: Context) {

    private val properties = Properties()

    init {
        try {
            context.assets.open("config.properties").use { stream ->
                properties.load(stream)
            }
        } catch (e: Exception) {
            Log.e("ConfigLoader", "Error loading config.properties", e)
            e.printStackTrace()
        }
    }

    fun getBleDevicePrefixes(): List<String> {
        return properties.getProperty("BLE_DEVICE_PREFIXES", "").split(",").filter { it.isNotEmpty() }
    }
}
