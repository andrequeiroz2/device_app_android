package com.dev.deviceapp.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserCreateRequest(
    val username: String,
    val email: String,
    val password: String,
    val confirm_password: String
)

sealed class UserCreateResponse {
    @Serializable
    data class Success(
        val uuid: String,
        val username: String,
        val email: String
    ) : UserCreateResponse()

    @Serializable
    data class Error(
        @SerialName("error_message")
        val errorMessage: String
    ) : UserCreateResponse()
}
