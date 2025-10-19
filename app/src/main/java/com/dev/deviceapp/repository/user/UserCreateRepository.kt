package com.dev.deviceapp.repository.user

import android.content.Context
import com.dev.deviceapp.config.ApiRoutes
import com.dev.deviceapp.model.user.UserCreateRequest
import com.dev.deviceapp.model.user.UserCreateResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject
import io.ktor.http.*
import jakarta.inject.Named
import dagger.hilt.android.qualifiers.ApplicationContext

class UserCreateRepository @Inject constructor(
    @Named("HttpClientUnauthenticated") private val client: HttpClient,
    @ApplicationContext private val context: Context
){

    private val apiRoutes = ApiRoutes(context)
    suspend fun createUser(user: UserCreateRequest): UserCreateResponse {

        val url = apiRoutes.getUrl("user_post")

        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(user)
        }

        return if(response.status == HttpStatusCode.OK){
            val data = response.body<UserCreateResponse.Success>()
            UserCreateResponse.Success(
                data.uuid,
                data.username,
                data.email
            )
        }else{
            val data = response.body<UserCreateResponse.Error>()
            UserCreateResponse.Error(
                data.errorMessage
            )
        }
    }
}