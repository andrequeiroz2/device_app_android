package com.dev.deviceapp.repository.broker

import android.content.Context
import com.dev.deviceapp.config.ApiRoutes
import com.dev.deviceapp.model.broker.BrokerDeleteResponse
import com.dev.deviceapp.model.user.UserDeleteResponse
import com.dev.deviceapp.repository.login.TokenRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.http.HttpStatusCode
import javax.inject.Inject
import jakarta.inject.Named

class BrokerDeleteRepository @Inject constructor(
    @Named("HttpClientAuthenticated") private val client: HttpClient,
    @ApplicationContext private val context: Context,
    private val tokenRepository: TokenRepository
) {

    private val apiRoutes = ApiRoutes(context)
    suspend fun deleteBroker(brokerUuid: String): BrokerDeleteResponse {

        tokenRepository.getTokenInfoRepository() ?:
            throw IllegalStateException("User not Authenticate")

        val url = apiRoutes.getUrl("broker_delete", brokerUuid)

        val response = client.delete(url)

        return if(response.status == HttpStatusCode.NoContent){
            BrokerDeleteResponse.Success(
                message = "Broker deleted successfully"
            )
        }else{
            val data = response.body<UserDeleteResponse.Error>()
            BrokerDeleteResponse.Error(
                data.errorMessage
            )
        }
    }
}