package com.dev.deviceapp.repository.broker

import android.content.Context
import com.dev.deviceapp.config.ApiRoutes
import com.dev.deviceapp.model.broker.BrokerGetFilterRequest
import com.dev.deviceapp.model.broker.BrokerGetFilterResponse
import com.dev.deviceapp.repository.login.TokenRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import jakarta.inject.Inject
import jakarta.inject.Named

class BrokerGetFilterRepository @Inject constructor(
    @Named("HttpClientAuthenticated") private val client: HttpClient,
    @ApplicationContext private val context: Context,
    private val tokenRepository: TokenRepository
){

    private val apiRoutes = ApiRoutes(context)
    suspend fun getBroker(params: BrokerGetFilterRequest): BrokerGetFilterResponse {

        tokenRepository.getTokenInfoRepository() ?:
            throw IllegalStateException("User not Authenticate")

        val url = apiRoutes.getUrl("broker_get")

        val response = client.get(url){
            contentType(ContentType.Application.Json)

            params.uuid?.let { parameter("uuid", it) }
            params.host?.let { parameter("host", it) }
            params.port?.let { parameter("port", it) }
            params.connected?.let { parameter("connected", it) }

            params.pagination?.let {
                parameter("page", it.page)
                parameter("page_size", it.pageSize)
            }
        }

        return (if(response.status == HttpStatusCode.OK){
            val data = response.body<BrokerGetFilterResponse.Success>()
            BrokerGetFilterResponse.Success(
                data.brokers,
                data.pagination,
                data.totalCount,
                data.totalPages,
                data.currentPage,
                data.nextPage,
                data.previousPage,
                data.firstPage,
                data.lastPage,
                data.hasNextPage
            )
        }else{
            val data = response.body<BrokerGetFilterResponse.Error>()
            BrokerGetFilterResponse.Error(
                data.errorMessage
            )
        })
    }
}