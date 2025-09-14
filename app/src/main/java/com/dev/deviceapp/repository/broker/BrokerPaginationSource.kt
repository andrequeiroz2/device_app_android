package com.dev.deviceapp.repository.broker

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dev.deviceapp.model.broker.BrokerGetFilterRequest
import com.dev.deviceapp.model.broker.BrokerResponse
import com.dev.deviceapp.model.pagination.PaginationModel
import com.dev.deviceapp.model.broker.BrokerGetFilterResponse

class BrokerPaginationSource (
    private val repository: BrokerGetFilterRepository,
    private val host: String?,
    private val port: Int?,
    private val connected: Boolean?
): PagingSource<Int, BrokerResponse.Success>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BrokerResponse.Success> {
        return try{
            val page = params.key ?: 1
            val pageSize = params.loadSize

            val request = BrokerGetFilterRequest(
                host = host,
                port = port,
                connected = connected,
                pagination = PaginationModel(page = page, pageSize = pageSize)
            )

            val response = repository.getBroker(request)

            when (response) {
                is BrokerGetFilterResponse.Success -> {
                    LoadResult.Page(
                        data = response.brokers,
                        prevKey = if (page == 1) null else page - 1,
                        nextKey = if (response.hasNextPage) page + 1 else null
                    )
                }
                is BrokerGetFilterResponse.Error -> {
                    LoadResult.Error(Exception(response.errorMessage))
                }
            }
        }catch (e: Exception){
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, BrokerResponse.Success>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }
}