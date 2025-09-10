package com.dev.deviceapp.model.broker

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName

@Serializable
data class BrokerCreateRequest(
    val host: String,
    val port: Int,
    val client_id: String,
    val version: Int,
    val keep_alive: Int,
    val clean_session: Boolean,
    val last_will_topic: String,
    val last_will_message: String,
    val last_will_qos: Int,
    val last_will_retain: Boolean
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
        val client_id: String,
        val version: Int,
        val version_text: String,
        val keep_alive: Int,
        val clean_session: Boolean,
        val last_will_topic: String,
        val last_will_message: String,
        val last_will_qos: Int,
        val last_will_retain: Boolean,
        val connected: Boolean,
        val created_at: Instant?,
        val updated_at: Instant?,
        val deleted_at: Instant?,
    ) : BrokerResponse()

    @Serializable
    data class Error(
        @SerialName("error_message")
        val errorMessage: String
    ) : BrokerResponse()

}