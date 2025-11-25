package com.dev.deviceapp.model.device

import kotlinx.serialization.Serializable

@Serializable
data class DeviceAdoptionBoard(
    val device_name: String,
    val user_uuid: String,
    val device_uuid: String,
    val broker_url: String,
    val topic: String
)
