package com.dev.deviceapp.repository.login

import com.dev.deviceapp.model.login.LoginRequest
import com.dev.deviceapp.model.login.LoginResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import jakarta.inject.Inject
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.*
import jakarta.inject.Named


class LoginRepository @Inject constructor(
    @Named("HttpClientUnauthenticated") private val client: HttpClient
) {
    suspend fun login(login: LoginRequest): LoginResponse {
        val response = client.post("http://10.0.2.2:8081/login") {
            contentType(ContentType.Application.Json)
            setBody(login)
        }

        return if(response.status == HttpStatusCode.OK){
            val data = response.body<LoginResponse.Success>()
            LoginResponse.Success(
                data.token
            )
        }else{

            val data = response.body<LoginResponse.Error>()
            LoginResponse.Error(
                data.errorMessage
            )
        }
    }
}