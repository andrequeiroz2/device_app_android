@file:OptIn(ExperimentalTime::class)

package com.dev.deviceapp.model.device

import com.dev.deviceapp.model.broker.BrokerResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

sealed class DeviveBleScanResponse{
    @Serializable
    data class Success(
        val name: String,
        val address: String,
    ): DeviveBleScanResponse()

    @Serializable
    data class Error(
        @SerialName("error_message")
        val errorMessage: String
    ) : DeviveBleScanResponse()
}