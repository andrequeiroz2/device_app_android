package com.dev.deviceapp.repository.broker

import com.dev.deviceapp.model.broker.BrokerResponse
import com.dev.deviceapp.model.broker.BrokerUpdateRequest
import com.dev.deviceapp.model.broker.BrokerUpdateResponse
import com.dev.deviceapp.repository.login.TokenRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import jakarta.inject.Inject
import jakarta.inject.Named
import kotlin.time.ExperimentalTime

class BrokerUpdateRepository @Inject constructor(
    @Named("HttpClientAuthenticated") private val client: HttpClient,
    private val tokenRepository: TokenRepository

){

    @OptIn(ExperimentalTime::class)
    suspend fun updateBroker(broker: BrokerUpdateRequest): BrokerUpdateResponse {
        val userInfo = tokenRepository.getTokenInfoRepository() ?:
            throw IllegalStateException("User not Authenticate")

        val response = client.put("http://10.0.2.2:8081/broker/${broker.uuid}"){
            contentType(ContentType.Application.Json)
            setBody(broker)
        }

        return if(response.status == HttpStatusCode.OK){
            val data = response.body<BrokerResponse.Success>()
            BrokerUpdateResponse.Success(
                data
            )
        }else{
            val data = response.body<BrokerUpdateResponse.Error>()
            BrokerUpdateResponse.Error(
                data.errorMessage
            )
        }
    }
}