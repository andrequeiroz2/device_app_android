@file:OptIn(ExperimentalTime::class)

package com.dev.deviceapp.model.broker

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlin.time.ExperimentalTime
import com.dev.deviceapp.di.InstantSerializer
import kotlin.time.Instant

@Serializable
data class BrokerCreateRequest(
    val host: String,
    val port: Int,
    @SerialName("client_id")
    val clientId: String,
    val version: Int,
    @SerialName("keep_alive")
    val keepAlive: Int,
    @SerialName("clean_session")
    val cleanSession: Boolean,
    @SerialName("last_will_topic")
    val lastWillTopic: String,
    @SerialName("last_will_message")
    val lastWillMessage: String,
    @SerialName("last_will_qos")
    val lastWillQos: Int,
    @SerialName("last_will_retain")
    val lastWillRetain: Boolean
)

@Serializable
data class BrokerGetRequest(
    val uuid: String,
    val host: String,
    val port: Int,
    val connected: Boolean
)

sealed class BrokerResponse {
    @Serializable
    data class Success(
        val uuid: String,
        val host: String,
        val port: Int,
        @SerialName("client_id")
        val clientId: String,
        val version: Int,
        @SerialName("version_text")
        val versionText: String,
        @SerialName("keep_alive")
        val keepAlive: Int,
        @SerialName("clean_session")
        val cleanSession: Boolean,
        @SerialName("last_will_topic")
        val lastWillTopic: String,
        @SerialName("last_will_message")
        val lastWillMessage: String,
        @SerialName("last_will_qos")
        val lastWillQos: Int,
        @SerialName("last_will_retain")
        val lastWillRetain: Boolean,
        val connected: Boolean,
        @SerialName("created_at")
        @Serializable(with = InstantSerializer::class)
        val createdAt: Instant? = null,
        @SerialName("updated_at")
        @Serializable(with = InstantSerializer::class)
        val updatedAt: Instant? = null,
    ) : BrokerResponse()

    @Serializable
    data class Error(
        @SerialName("error_message")
        val errorMessage: String
    ) : BrokerResponse()

}