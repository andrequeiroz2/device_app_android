package com.dev.deviceapp.database.device

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class DeviceMessageReceivedEntity(
    val value: String,
    val scale: String,
    val timestamp: Instant
)

