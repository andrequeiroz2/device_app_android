package com.dev.deviceapp.model.broker

import kotlinx.serialization.Serializable

sealed class BrokerDeleteResponse{
    @Serializable
    data class Success(
        val message: String
    ) : BrokerDeleteResponse()

    @Serializable
    data class Error(
        val errorMessage: String
    ) : BrokerDeleteResponse()

}