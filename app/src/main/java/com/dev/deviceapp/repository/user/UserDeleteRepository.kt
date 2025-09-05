package com.dev.deviceapp.repository.user


import com.dev.deviceapp.model.login.LoginRequest
import com.dev.deviceapp.model.user.UserCreateResponse
import com.dev.deviceapp.model.user.UserDeleteResponse
import com.dev.deviceapp.repository.login.TokenRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import jakarta.inject.Inject
import jakarta.inject.Named
import io.ktor.http.*


class UserDeleteRepository @Inject constructor(
    @Named("HttpClientAuthenticated") private val client: HttpClient,
    private val tokenRepository: TokenRepository
){

    suspend fun deleteUser(param: LoginRequest): UserDeleteResponse {
        val userInfo = tokenRepository.getTokenInfoRepository() ?:
            throw IllegalStateException("User not Authenticate")

        val response = client.delete("http://10.0.2.2:8081/user/${userInfo.uuid}") {
            contentType(ContentType.Application.Json)
            setBody(param)
        }

        return if(response.status == HttpStatusCode.NoContent){
            UserDeleteResponse.Success(
                message = "User deleted successfully"
            )
        }else{
            val data = response.body<UserDeleteResponse.Error>()
            UserDeleteResponse.Error(
                data.errorMessage
            )
        }
    }
}