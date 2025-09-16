package com.dev.deviceapp.model.broker

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BrokerUpdateRequest(
    val uuid: String,
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
    val lastWillRetain: Boolean,
    val connected: Boolean
)

sealed class BrokerUpdateResponse{
    @Serializable
    data class Success(
        val broker: BrokerResponse.Success
    ): BrokerUpdateResponse()

    @Serializable
    data class Error(
        @SerialName("error_message")
        val errorMessage: String
    ): BrokerUpdateResponse()
}