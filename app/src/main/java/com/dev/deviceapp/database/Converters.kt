package com.dev.deviceapp.database

import androidx.room.TypeConverter
import com.dev.deviceapp.database.device.DeviceMessageEntity
import com.dev.deviceapp.database.device.DeviceMessageReceivedEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromInstant(value: Instant?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toInstant(value: String?): Instant? {
        return value?.let { Instant.parse(it) }
    }

    @TypeConverter
    fun fromStringMap(value: Map<String, DeviceMessageReceivedEntity>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStringMap(value: String?): Map<String, DeviceMessageReceivedEntity>? {
        return value?.let {
            val type = object : TypeToken<Map<String, DeviceMessageReceivedEntity>>() {}.type
            gson.fromJson(it, type)
        }
    }

    @TypeConverter
    fun fromDeviceMessageList(value: List<DeviceMessageEntity>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toDeviceMessageList(value: String?): List<DeviceMessageEntity>? {
        return value?.let {
            val type = object : TypeToken<List<DeviceMessageEntity>>() {}.type
            gson.fromJson(it, type)
        }
    }
}

