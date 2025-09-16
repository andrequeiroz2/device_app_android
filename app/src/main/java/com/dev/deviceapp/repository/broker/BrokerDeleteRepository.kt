package com.dev.deviceapp.repository.broker

import com.dev.deviceapp.model.broker.BrokerDeleteResponse
import com.dev.deviceapp.model.user.UserDeleteResponse
import com.dev.deviceapp.repository.login.TokenRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.http.HttpStatusCode
import javax.inject.Inject
import jakarta.inject.Named

class BrokerDeleteRepository @Inject constructor(
    @Named("HttpClientAuthenticated") private val client: HttpClient,
    private val tokenRepository: TokenRepository
) {

    suspend fun deleteBroker(brokerUuid: String): BrokerDeleteResponse {
        val userInfo = tokenRepository.getTokenInfoRepository() ?:
            throw IllegalStateException("User not Authenticate")

        val response = client.delete("http://10.0.2.2:8081/broker/${brokerUuid}")

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