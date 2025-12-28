@file:OptIn(ExperimentalTime::class)

package com.dev.deviceapp.model.device

import com.dev.deviceapp.model.pagination.PaginationModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.dev.deviceapp.di.InstantSerializer
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class DeviceGetOwnedUserFilterRequest(
    val pagination: PaginationModel? = null
)

@Serializable
data class DeviceAndMessageResponse(
    val uuid: String,
    val name: String,
    @SerialName("device_type_int")
    val deviceTypeInt: Int,
    @SerialName("device_type_text")
    val deviceTypeText: String,
    @SerialName("board_type_int")
    val boardTypeInt: Int,
    @SerialName("board_type_text")
    val boardTypeText: String,
    @SerialName("sensor_type")
    val sensorType: String?,
    @SerialName("actuator_type")
    val actuatorType: String?,
    @SerialName("device_condition_int")
    val deviceConditionInt: Int,
    @SerialName("device_condition_text")
    val deviceConditionText: String,
    val topic: String,
    @SerialName("mac_address")
    val macAddress: String,
    @SerialName("created_at")
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant? = null,
    @SerialName("updated_at")
    @Serializable(with = InstantSerializer::class)
    val updatedAt: Instant? = null,
    @SerialName("deleted_at")
    @Serializable(with = InstantSerializer::class)
    val deletedAt: Instant? = null,
    val message: List<DeviceMessagesOwned>?,
)

@Serializable
data class DeviceMessagesOwned(
    @SerialName("device_uuid")
    val deviceUuid: String,
    val messages: Map<String, DeviceMessageReceived>,
)

@Serializable
data class DeviceMessageReceived(
    val value: String,
    val scale: String,
    @Serializable(with = InstantSerializer::class)
    val timestamp: Instant
)


@Serializable
sealed class DeviceGetOwnedUserResponse{
    @Serializable
    data class Success(
        val devices: List<DeviceAndMessageResponse>,
        val pagination: PaginationModel,
        @SerialName("total_count")
        val totalCount: Int,
        @SerialName("total_pages")
        val totalPages: Int,
        @SerialName("current_page")
        val currentPage: Int,
        @SerialName("next_page")
        val nextPage: Int?,
        @SerialName("previous_page")
        val previousPage: Int?,
        @SerialName("first_page")
        val firstPage: Int,
        @SerialName("last_page")
        val lastPage: Int,
        @SerialName("has_next_page")
        val hasNextPage: Boolean
    ): DeviceGetOwnedUserResponse()

    @Serializable
    data class Error(
        @SerialName("error_message")
        val errorMessage: String
    ): DeviceGetOwnedUserResponse()
}
