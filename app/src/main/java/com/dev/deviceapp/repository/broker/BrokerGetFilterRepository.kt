package com.dev.deviceapp.repository.broker

import android.content.Context
import android.util.Log
import com.dev.deviceapp.config.ApiRoutes
import com.dev.deviceapp.database.broker.BrokerDao
import com.dev.deviceapp.database.broker.BrokerEntity
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.ExperimentalTime

class BrokerGetFilterRepository @Inject constructor(
    @Named("HttpClientAuthenticated") private val client: HttpClient,
    @ApplicationContext private val context: Context,
    private val tokenRepository: TokenRepository,
    private val brokerDao: BrokerDao
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
            val successResponse = BrokerGetFilterResponse.Success(
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
            
            
            if (params.connected == true && data.brokers.isNotEmpty()) {
                withContext(Dispatchers.IO) {
                    saveBrokerToDatabase(data.brokers.first())
                }
            }
            
            successResponse
        }else{
            val data = response.body<BrokerGetFilterResponse.Error>()
            BrokerGetFilterResponse.Error(
                data.errorMessage
            )
        })
    }
    
    @OptIn(ExperimentalTime::class)
    private suspend fun saveBrokerToDatabase(broker: com.dev.deviceapp.model.broker.BrokerResponse.Success) {
        val brokerEntity = BrokerEntity(
            uuid = broker.uuid,
            host = broker.host,
            port = broker.port,
            clientId = broker.clientId,
            version = broker.version,
            versionText = broker.versionText,
            keepAlive = broker.keepAlive,
            cleanSession = broker.cleanSession,
            lastWillTopic = broker.lastWillTopic,
            lastWillMessage = broker.lastWillMessage,
            lastWillQos = broker.lastWillQos,
            lastWillRetain = broker.lastWillRetain,
            connected = broker.connected,
            createdAt = broker.createdAt,
            updatedAt = broker.updatedAt
        )
        
        brokerDao.insertBroker(brokerEntity)
        Log.i("BrokerGetFilterRepository", "Broker saved to database: ${brokerEntity.uuid} - ${brokerEntity.host}:${brokerEntity.port}")
    }
}