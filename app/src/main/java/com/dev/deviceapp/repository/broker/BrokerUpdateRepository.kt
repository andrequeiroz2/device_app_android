package com.dev.deviceapp.repository.broker

import com.dev.deviceapp.model.broker.BrokerCreateRequest
import com.dev.deviceapp.model.broker.BrokerResponse
import com.dev.deviceapp.repository.login.TokenRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.put
import jakarta.inject.Inject
import jakarta.inject.Named

class BrokerUpdateRepository @Inject constructor(
    @Named("HttpClientAuthenticated") private val client: HttpClient,
    private val tokenRepository: TokenRepository

){

//    suspend fun updateBroker(broker: BrokerCreateRequest): BrokerResponse {
//        val userInfo = tokenRepository.getTokenInfoRepository() ?:
//            throw IllegalStateException("User not Authenticate")
//
//        val response = client.put("http://10.0.2.2:8081/broker/${userInfo.uuid}"){
//    }

}