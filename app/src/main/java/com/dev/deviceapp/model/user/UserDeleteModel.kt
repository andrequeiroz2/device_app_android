package com.dev.deviceapp.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


sealed class UserDeleteResponse {

    @Serializable
    data class Success(
        val message: String
    ) : UserDeleteResponse()

    @Serializable
    data class Error(
        @SerialName("error_message")
        val errorMessage: String
    ) : UserDeleteResponse()
}