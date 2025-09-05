package com.dev.deviceapp.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserUpdateRequest(
    val username: String,
    val email: String,
)

sealed class UserUpdateResponse{
    @Serializable
    data class Success(
        val uuid: String,
        val username: String,
        val email: String,
    ) : UserUpdateResponse()

    @Serializable
    data class Error(
        @SerialName("error_message")
        val errorMessage: String,
    ) : UserUpdateResponse()
}