package com.dev.deviceapp.repository.user

import com.dev.deviceapp.model.user.UserCreateRequest
import com.dev.deviceapp.model.user.UserCreateResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject
import io.ktor.http.*

class UserCreateRepository @Inject constructor(
    private val client: HttpClient
){
    suspend fun createUser(user: UserCreateRequest): UserCreateResponse {
        val response = client.post("http://10.0.2.2:8081/user/create") {
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