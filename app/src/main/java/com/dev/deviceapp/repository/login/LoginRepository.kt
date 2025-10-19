package com.dev.deviceapp.repository.login

import android.content.Context
import com.dev.deviceapp.model.login.LoginRequest
import com.dev.deviceapp.model.login.LoginResponse
import com.dev.deviceapp.config.ApiRoutes
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import jakarta.inject.Inject
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.*
import jakarta.inject.Named
import dagger.hilt.android.qualifiers.ApplicationContext


class LoginRepository @Inject constructor(
    @Named("HttpClientUnauthenticated") private val client: HttpClient,
    @ApplicationContext private val context: Context
) {

    private val apiRoutes = ApiRoutes(context)

    suspend fun login(login: LoginRequest): LoginResponse {

        val url = apiRoutes.getUrl("login")

        val response = client.post(url) {
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