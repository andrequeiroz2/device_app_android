package com.dev.deviceapp.repository.broker

import com.dev.deviceapp.model.broker.BrokerCreateRequest
import com.dev.deviceapp.model.broker.BrokerResponse
import com.dev.deviceapp.repository.login.TokenRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import jakarta.inject.Inject
import jakarta.inject.Named

class BrokerCreateRepository @Inject constructor(
    @Named("HttpClientAuthenticated") private val client: HttpClient,
    private val tokenRepository: TokenRepository
){

    suspend fun createBroker(broker: BrokerCreateRequest): BrokerResponse {
        val userInfo = tokenRepository.getTokenInfoRepository() ?:
        throw IllegalStateException("User not Authenticate")

        val response = client.post("http://10.0.2.2:8081/broker") {
            contentType(ContentType.Application.Json)
            setBody(broker)
        }

        return if(response.status == HttpStatusCode.OK){
            val data = response.body<BrokerResponse.Success>()
            BrokerResponse.Success(
                data.uuid,
                data.host,
                data.port,
                data.clientId,
                data.version,
                data.versionText,
                data.keepAlive,
                data.cleanSession,
                data.lastWillTopic,
                data.lastWillMessage,
                data.lastWillQos,
                data.lastWillRetain,
                data.connected,
                data.createdAt,
                data.updatedAt,
            )
        }else{
            val data = response.body<BrokerResponse.Error>()
            BrokerResponse.Error(
                data.errorMessage
            )
        }
    }
}