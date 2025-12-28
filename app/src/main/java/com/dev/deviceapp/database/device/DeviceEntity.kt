package com.dev.deviceapp.database.device

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.dev.deviceapp.database.Converters
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Entity(tableName = "devices")
@TypeConverters(Converters::class)
data class DeviceEntity(
    @PrimaryKey
    val uuid: String,
    val name: String,
    val deviceTypeInt: Int,
    val deviceTypeText: String,
    val boardTypeInt: Int,
    val boardTypeText: String,
    val sensorType: String?,
    val actuatorType: String?,
    val deviceConditionInt: Int,
    val deviceConditionText: String,
    val topic: String,
    val macAddress: String,
    val createdAt: Instant?,
    val updatedAt: Instant?,
    val deletedAt: Instant?,
    val messages: List<DeviceMessageEntity>?
)

