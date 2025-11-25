package com.dev.deviceapp.repository.device

import android.content.Context
import android.util.Log
import com.dev.deviceapp.config.ApiRoutes
import com.dev.deviceapp.model.device.DeviceAdoptionResponse
import com.dev.deviceapp.model.device.DeviceCreateRequest
import com.dev.deviceapp.repository.login.TokenRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import jakarta.inject.Named
import javax.inject.Inject
import kotlin.time.ExperimentalTime

class DeviceCreateRepository @Inject constructor(
    @Named("HttpClientAuthenticated") private val client: HttpClient,
    @ApplicationContext private val context: Context,
    private val tokenRepository: TokenRepository
){

    private val apiRoutes = ApiRoutes(context)

    @OptIn(ExperimentalTime::class)
    suspend fun createDevice(device: DeviceCreateRequest): DeviceAdoptionResponse {
        tokenRepository.getTokenInfoRepository() ?:
        throw IllegalStateException("User not Authenticate")

        val url = apiRoutes.getUrl("device_post")

        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(device)
        }

        return if (response.status == HttpStatusCode.OK) {
            val data = response.body<DeviceAdoptionResponse.Success>()
            DeviceAdoptionResponse.Success(
                data.uuid,
                data.userUuid,
                data.name,
                data.deviceTypeInt,
                data.deviceTypeText,
                data.boardTypeInt,
                data.boardTypeText,
                data.macAddress,
                data.deviceConditionInt,
                data.deviceConditionText,
                data.createdAt,
                data.updatedAt,
                data.deletedAt,
                data.message,
                data.scale,
                data.brokerUrl
            )
        } else {
            val errorText = try {
                response.body<String>()
            } catch (e: Exception) {
                Log.e("DeviceCreateRepository", "Error reading error body", e)
                "Unknown error"
            }

            DeviceAdoptionResponse.Error(
                errorMessage = errorText
            )
        }
    }
}