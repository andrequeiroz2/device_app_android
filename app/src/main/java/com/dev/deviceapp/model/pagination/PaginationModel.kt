package com.dev.deviceapp.model.pagination

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaginationModel(
    val page: Int = 1,
    @SerialName("page_size")
    val pageSize: Int = 10
)