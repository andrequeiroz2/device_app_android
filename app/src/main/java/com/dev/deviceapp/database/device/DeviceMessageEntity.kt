package com.dev.deviceapp.database.device

data class DeviceMessageEntity(
    val deviceUuid: String,
    val messages: Map<String, DeviceMessageReceivedEntity>?
)

