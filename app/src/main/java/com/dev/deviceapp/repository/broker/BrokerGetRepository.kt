package com.dev.deviceapp.repository.broker

import com.dev.deviceapp.model.broker.BrokerResponse
import com.dev.deviceapp.model.broker.BrokerGetRequest
import com.dev.deviceapp.repository.login.TokenRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import jakarta.inject.Inject
import jakarta.inject.Named

class BrokerGetRepository @Inject constructor(
    @Named("HttpClientAuthenticated") private val client: HttpClient,
    private val tokenRepository: TokenRepository
){

    suspend fun getBroker(params: BrokerGetRequest): BrokerResponse{
        val userInfo = tokenRepository.getTokenInfoRepository() ?:
            throw IllegalStateException("User not Authenticate")

        val response = client.get("http://10.0.2.2:8081/broker/${userInfo.uuid}"){
            contentType(ContentType.Application.Json)
            setBody(params)
        }

        return if(response.status == HttpStatusCode.OK){
            val data = response.body<BrokerResponse.Success>()
            BrokerResponse.Success(
                data.uuid,
                data.host,
                data.port,
                data.client_id,
                data.version,
                data.version_text,
                data.keep_alive,
                data.clean_session,
                data.last_will_topic,
                data.last_will_message,
                data.last_will_qos,
                data.last_will_retain,
                data.connected,
                data.created_at,
                data.updated_at,
                data.deleted_at
            )
        }else{
            val data = response.body<BrokerResponse.Error>()
            BrokerResponse.Error(
                data.errorMessage
            )
        }
    }
}