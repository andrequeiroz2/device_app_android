package com.dev.deviceapp.model.broker

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import com.dev.deviceapp.model.pagination.PaginationModel

@Serializable
data class BrokerGetFilterRequest(
    val uuid: String? = null,
    val host: String? = null,
    val port: Int? = null,
    val connected: Boolean? = null,
    val pagination: PaginationModel? = null
)

@Serializable
sealed class BrokerGetFilterResponse{
    @Serializable
    data class Success(
        val brokers: List<BrokerResponse.Success>,
        val pagination: PaginationModel,
        @SerialName("total_count")
        val totalCount: Int,
        @SerialName("total_pages")
        val totalPages: Int,
        @SerialName("current_page")
        val currentPage: Int,
        @SerialName("next_page")
        val nextPage: Int?,
        @SerialName("previous_page")
        val previousPage: Int?,
        @SerialName("first_page")
        val firstPage: Int,
        @SerialName("last_page")
        val lastPage: Int,
        @SerialName("has_next_page")
        val hasNextPage: Boolean
    ): BrokerGetFilterResponse()

    @Serializable
    data class Error(
        @SerialName("error_message")
        val errorMessage: String
    ): BrokerGetFilterResponse()
}