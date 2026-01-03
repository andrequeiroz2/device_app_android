package com.dev.deviceapp.model.mqtt

import com.dev.deviceapp.di.InstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Serializable
data class MqttMessagePayload(
    val topic: String,
    val metric: String,
    val scale: String,
    val payload: String,
    @Serializable(with = InstantSerializer::class)
    val timestamp: Instant
)

