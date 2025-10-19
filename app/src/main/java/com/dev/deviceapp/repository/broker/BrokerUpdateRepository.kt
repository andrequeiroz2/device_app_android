package com.dev.deviceapp.repository.broker

import android.content.Context
import com.dev.deviceapp.config.ApiRoutes
import com.dev.deviceapp.model.broker.BrokerResponse
import com.dev.deviceapp.model.broker.BrokerUpdateRequest
import com.dev.deviceapp.model.broker.BrokerUpdateResponse
import com.dev.deviceapp.repository.login.TokenRepository
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context,
    private val tokenRepository: TokenRepository
){

    private val apiRoutes = ApiRoutes(context)
    @OptIn(ExperimentalTime::class)
    suspend fun updateBroker(broker: BrokerUpdateRequest): BrokerUpdateResponse {

        tokenRepository.getTokenInfoRepository() ?:
            throw IllegalStateException("User not Authenticate")

        val url = apiRoutes.getUrl("broker_put", broker.uuid)

        val response = client.put(url){
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