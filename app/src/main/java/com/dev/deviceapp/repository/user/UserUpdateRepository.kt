package com.dev.deviceapp.repository.user

import android.content.Context
import com.dev.deviceapp.config.ApiRoutes
import com.dev.deviceapp.model.user.UserUpdateRequest
import com.dev.deviceapp.model.user.UserUpdateResponse
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

class UserUpdateRepository @Inject constructor(
    @Named("HttpClientAuthenticated") private val client: HttpClient,
    @ApplicationContext private val context: Context,
    private val tokenRepository: TokenRepository
){

    private val apiRoutes = ApiRoutes(context)

    suspend fun updateUser(param: UserUpdateRequest): UserUpdateResponse{

        val userInfo = tokenRepository.getTokenInfoRepository() ?:
        throw IllegalStateException("User not Authenticate")

        val url = apiRoutes.getUrl("user_put", userInfo.uuid)

        val response = client.put(url) {
            contentType(ContentType.Application.Json)
            setBody(param)
        }

        return if(response.status == HttpStatusCode.OK){

            val data = response.body<UserUpdateResponse.Success>()
            UserUpdateResponse.Success(
                data.uuid,
                data.username,
                data.email
            )
        }else{
            val data = response.body<UserUpdateResponse.Error>()
            UserUpdateResponse.Error(
                data.errorMessage
            )
        }
    }
}