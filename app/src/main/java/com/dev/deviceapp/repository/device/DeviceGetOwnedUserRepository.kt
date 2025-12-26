package com.dev.deviceapp.repository.device

import android.content.Context
import com.dev.deviceapp.config.ApiRoutes
import com.dev.deviceapp.database.device.DeviceDao
import com.dev.deviceapp.database.device.DeviceDaoLogger
import com.dev.deviceapp.database.device.DeviceEntity
import com.dev.deviceapp.database.device.DeviceMessageEntity
import com.dev.deviceapp.database.device.DeviceMessageReceivedEntity
import com.dev.deviceapp.model.device.DeviceAndMessageResponse
import com.dev.deviceapp.model.device.DeviceGetOwnedUserFilterRequest
import com.dev.deviceapp.model.device.DeviceGetOwnedUserResponse
import com.dev.deviceapp.repository.login.TokenRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import jakarta.inject.Inject
import jakarta.inject.Named
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.ExperimentalTime

class DeviceGetOwnedUserRepository @Inject constructor(
    @Named("HttpClientAuthenticated") private val client: HttpClient,
    @ApplicationContext private val context: Context,
    private val tokenRepository: TokenRepository,
    private val deviceDao: DeviceDao
){
    private val apiRoutes = ApiRoutes(context)

    suspend fun getDeviceOwnedUser(params: DeviceGetOwnedUserFilterRequest): DeviceGetOwnedUserResponse {

        tokenRepository.getTokenInfoRepository() ?:
        throw IllegalStateException("User not Authenticate")

        val url = apiRoutes.getUrl("device_get_owned_user")

        val response = client.get(url){
            contentType(ContentType.Application.Json)

            params.pagination?.let {
                parameter("page", it.page)
                parameter("page_size", it.pageSize)
            }
        }

        return (if(response.status == HttpStatusCode.OK){
            val data = response.body<DeviceGetOwnedUserResponse.Success>()
            val successResponse = DeviceGetOwnedUserResponse.Success(
                data.devices,
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
            
            // Save DataBase Devices
            withContext(Dispatchers.IO) {
                saveDevicesToDatabase(data.devices)
            }
            
            successResponse
        }else{
            val data = response.body<DeviceGetOwnedUserResponse.Error>()
            DeviceGetOwnedUserResponse.Error(
                data.errorMessage
            )
        })
    }
    
    @OptIn(ExperimentalTime::class)
    private suspend fun saveDevicesToDatabase(devices: List<DeviceAndMessageResponse>) {
        val deviceEntities = devices.map { device ->
            val messageEntities = device.message?.map { message ->
                DeviceMessageEntity(
                    deviceUuid = message.deviceUuid,
                    messages = message.messages.mapValues { (_, received) ->
                        DeviceMessageReceivedEntity(
                            value = received.value,
                            scale = received.scale,
                            timestamp = received.timestamp
                        )
                    }
                )
            }
            
            DeviceEntity(
                uuid = device.uuid,
                name = device.name,
                deviceTypeInt = device.deviceTypeInt,
                deviceTypeText = device.deviceTypeText,
                boardTypeInt = device.boardTypeInt,
                boardTypeText = device.boardTypeText,
                sensorType = device.sensorType,
                actuatorType = device.actuatorType,
                deviceConditionInt = device.deviceConditionInt,
                deviceConditionText = device.deviceConditionText,
                macAddress = device.macAddress,
                createdAt = device.createdAt,
                updatedAt = device.updatedAt,
                deletedAt = device.deletedAt,
                messages = messageEntities
            )
        }
        
        deviceDao.replaceAllDevices(deviceEntities)
        
        // Log Database device REPLACE_ALL
        DeviceDaoLogger.logDatabaseState(deviceDao, "REPLACE_ALL")
    }
}