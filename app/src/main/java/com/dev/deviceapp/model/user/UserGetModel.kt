package com.dev.deviceapp.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class UserGetRequest(
    val uuid: String? = null,
    val email: String? = null,
)

@Serializable
sealed class UserGetResponse {

    @Serializable
    data class Success(
        val uuid: String,
        val username: String,
        val email: String,
    ) : UserGetResponse()

    @Serializable
    data class Error(
        @SerialName("error_message")
        val errorMessage: String
    ) : UserGetResponse()
}