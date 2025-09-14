package com.dev.deviceapp.repository.broker

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dev.deviceapp.model.broker.BrokerResponse
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class BrokerPaginationRepository @Inject constructor(
    private val brokerGetFilterRepository: BrokerGetFilterRepository
){
    fun pager(
        host: String?,
        port: Int?,
        connected: Boolean?,
        pageSize: Int = 10
    ): Flow<PagingData<BrokerResponse.Success>>{
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                BrokerPaginationSource(
                    brokerGetFilterRepository,
                    host,
                    port,
                    connected
                )
            }
        ).flow
    }
}