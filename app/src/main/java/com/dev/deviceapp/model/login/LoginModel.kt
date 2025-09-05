package com.dev.deviceapp.model.login

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest (
    val email: String,
    val password: String
)

sealed class LoginResponse{

    @Serializable
    data class Success(
        val token: String
    ): LoginResponse()

    @Serializable
    data class Error(
        @SerialName("error_message")
        val errorMessage: String
    ): LoginResponse()
}