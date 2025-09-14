package com.dev.deviceapp.viewmodel.broker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dev.deviceapp.model.broker.BrokerResponse
import com.dev.deviceapp.repository.broker.BrokerPaginationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

@HiltViewModel
class BrokerPaginateViewModel @Inject constructor(
    private val brokerPaginationRepository: BrokerPaginationRepository
): ViewModel(){
    fun getBrokers(
        host: String?,
        port: Int?,
        connected: Boolean?
    ): Flow<PagingData<BrokerResponse.Success>> {
        return brokerPaginationRepository.pager(host, port, connected)
            .cachedIn(viewModelScope)
    }
}