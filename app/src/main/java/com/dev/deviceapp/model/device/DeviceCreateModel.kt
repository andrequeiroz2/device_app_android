package com.dev.deviceapp.model.device

import com.dev.deviceapp.di.InstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class DeviceCreateRequest(
    val name: String,
    val device_type_str: String,
    val board_type_str: String,
    val sensor_type: String? = null,
    val actuator_type: String? = null,
    val adopted_status: String,
    val mac_address: String,
    val scale: List<List<String>>? = null,
    val message: DeviceMessageCreateRequest
)

@Serializable
data class DeviceMessageCreateRequest(
    val qos: Int,
    val retained: Boolean,
    val publisher: Boolean? = null,
    val subscriber: Boolean? = null,
    val command_start: Int? = 1,
    val command_end: Int? = 0,
    val command_last: Int? = null
)

@OptIn(ExperimentalTime::class)
@Serializable
data class DeviceMessageResponse(
    val uuid: String,
    @SerialName("device_uuid")
    val deviceUuid: String,
    val topic: String,
    val qos: Int,
    val retained: Boolean,
    val publisher: Boolean? = null,
    val subscriber: Boolean? = null,
    @SerialName("command_start")
    val commandStart: Int? = 1,
    @SerialName("command_end")
    val commandEnd: Int? = 0,
    @SerialName("command_last")
    val commandLast: Int? = null,
    @SerialName("command_last_time")
    @Serializable(with = InstantSerializer::class)
    val commandLastTime: Instant? = null,
    @SerialName("created_at")
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant? = null,
    @SerialName("updated_at")
    @Serializable(with = InstantSerializer::class)
    val updatedAt: Instant? = null,
    @SerialName("deleted_at")
    @Serializable(with = InstantSerializer::class)
    val deletedAt: Instant? = null
)

@OptIn(ExperimentalTime::class)
@Serializable
data class DeviceScaleResponse(
    val uuid: String,
    @SerialName("device_id")
    val deviceId: String,
    val metric: String,
    val unit: String,
    @SerialName("created_at")
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant? = null,
    @SerialName("updated_at")
    @Serializable(with = InstantSerializer::class)
    val updatedAt: Instant? = null,
    @SerialName("deleted_at")
    @Serializable(with = InstantSerializer::class)
    val deletedAt: Instant? = null
)

@OptIn(ExperimentalTime::class)
sealed class DeviceAdoptionResponse {

    @Serializable
    data class Success(
        val uuid: String,
        @SerialName("user_uuid")
        val userUuid: String,
        val name: String,
        @SerialName("device_type_int")
        val deviceTypeInt: Int,
        @SerialName("device_type_text")
        val deviceTypeText: String,
        @SerialName("board_type_int")
        val boardTypeInt: Int,
        @SerialName("board_type_text")
        val boardTypeText: String,
        @SerialName("mac_address")
        val macAddress: String,
        @SerialName("device_condition_int")
        val deviceConditionInt: Int,
        @SerialName("device_condition_text")
        val deviceConditionText: String,
        @SerialName("created_at")
        @Serializable(with = InstantSerializer::class)
        val createdAt: Instant? = null,
        @SerialName("updated_at")
        @Serializable(with = InstantSerializer::class)
        val updatedAt: Instant? = null,
        @SerialName("deleted_at")
        @Serializable(with = InstantSerializer::class)
        val deletedAt: Instant? = null,
        val message: DeviceMessageResponse,
        val scale: List<DeviceScaleResponse>,
        @SerialName("broker_url")
        val brokerUrl: String
    ) : DeviceAdoptionResponse()

    @Serializable
    data class Error(
        @SerialName("error_message")
        val errorMessage: String
    ) : DeviceAdoptionResponse()
}