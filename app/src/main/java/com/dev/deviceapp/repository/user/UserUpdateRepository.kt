package com.dev.deviceapp.repository.user

import com.dev.deviceapp.model.user.UserUpdateRequest
import com.dev.deviceapp.model.user.UserUpdateResponse
import com.dev.deviceapp.repository.login.TokenRepository
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
    private val tokenRepository: TokenRepository
){

    suspend fun updateUser(param: UserUpdateRequest): UserUpdateResponse{
        val userInfo = tokenRepository.getTokenInfoRepository() ?:
        throw IllegalStateException("User not Authenticate")

        val response = client.put("http://10.0.2.2:8081/user/${userInfo.uuid}") {
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