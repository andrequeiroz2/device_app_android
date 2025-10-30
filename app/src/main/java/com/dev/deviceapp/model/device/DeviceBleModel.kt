package com.dev.deviceapp.model.device

import kotlinx.serialization.Serializable

data class DeviceBleModel(
    val name: String,
    val address: String,
    val rssi: Int,
    val uuids: List<String> = emptyList(),
    val deviceType: Int = 0,
    val manufacturerData: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DeviceBleModel) return false

        return name == other.name &&
                address == other.address &&
                rssi == other.rssi &&
                uuids == other.uuids &&
                deviceType == other.deviceType &&
                manufacturerData?.contentEquals(other.manufacturerData) ?: (other.manufacturerData == null)
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + rssi
        result = 31 * result + uuids.hashCode()
        result = 31 * result + deviceType
        result = 31 * result + (manufacturerData?.contentHashCode() ?: 0)
        return result
    }
}

@Serializable
data class DeviceBleInfoModel(
    val boarder_type: String = "",
    val mac_address: String = "",
    val device_type: String = "",
    val sensor_type: String = "",
    val actuator_type: String = "",
    val adopted_status: Int = 0,
    val adopted_status_desc: String = "",
    val broker_url: String = "",
    val topic: String = "",
    val device_scale: List<List<String>> = emptyList(),
    val user_uuid: String = "",
    val device_uuid: String = "",
    val device_name: String = ""
)