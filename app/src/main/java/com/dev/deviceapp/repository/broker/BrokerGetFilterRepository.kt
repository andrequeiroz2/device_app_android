package com.dev.deviceapp.repository.broker

import com.dev.deviceapp.model.broker.BrokerGetFilterRequest
import com.dev.deviceapp.model.broker.BrokerGetFilterResponse
import com.dev.deviceapp.repository.login.TokenRepository
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
    private val tokenRepository: TokenRepository
){

    suspend fun getBroker(params: BrokerGetFilterRequest): BrokerGetFilterResponse {
        val userInfo = tokenRepository.getTokenInfoRepository() ?:
            throw IllegalStateException("User not Authenticate")

        val response = client.get("http://10.0.2.2:8081/broker"){
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