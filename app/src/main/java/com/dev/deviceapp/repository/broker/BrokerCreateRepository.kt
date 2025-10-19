package com.dev.deviceapp.repository.broker

import android.content.Context
import com.dev.deviceapp.config.ApiRoutes
import com.dev.deviceapp.model.broker.BrokerCreateRequest
import com.dev.deviceapp.model.broker.BrokerResponse
import com.dev.deviceapp.repository.login.TokenRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import jakarta.inject.Inject
import jakarta.inject.Named
import kotlin.time.ExperimentalTime

class BrokerCreateRepository @Inject constructor(
    @Named("HttpClientAuthenticated") private val client: HttpClient,
    @ApplicationContext private val context: Context,
    private val tokenRepository: TokenRepository
){

    private val apiRoutes = ApiRoutes(context)

    @OptIn(ExperimentalTime::class)
    suspend fun createBroker(broker: BrokerCreateRequest): BrokerResponse {

        tokenRepository.getTokenInfoRepository() ?:
        throw IllegalStateException("User not Authenticate")

        val url = apiRoutes.getUrl("broker_post")

        val response = client.post(url) {
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