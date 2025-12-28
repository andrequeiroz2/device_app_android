package com.dev.deviceapp.database.broker

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.dev.deviceapp.database.Converters
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Entity(tableName = "brokers")
@TypeConverters(Converters::class)
data class BrokerEntity(
    @PrimaryKey
    val uuid: String,
    val host: String,
    val port: Int,
    val clientId: String,
    val version: Int,
    val versionText: String,
    val keepAlive: Int,
    val cleanSession: Boolean,
    val lastWillTopic: String,
    val lastWillMessage: String,
    val lastWillQos: Int,
    val lastWillRetain: Boolean,
    val connected: Boolean,
    val createdAt: Instant?,
    val updatedAt: Instant?
)

