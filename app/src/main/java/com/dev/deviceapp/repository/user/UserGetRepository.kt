package com.dev.deviceapp.repository.user

import com.dev.deviceapp.model.user.UserGetRequest
import com.dev.deviceapp.model.user.UserGetResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import jakarta.inject.Inject
import jakarta.inject.Named

class UserGetRepository @Inject constructor(
    @Named("HttpClientAuthenticated") private val client: HttpClient

){

    suspend fun getUser(param: UserGetRequest): UserGetResponse{

        val response = client.get("http://10.0.2.2:8081/user"){

            if(param.uuid.isNotBlank()){
                parameter("uuid", param.uuid)
            }

            if(param.email.isNotBlank()){
                parameter("email", param.email)
            }

        }

        return if(response.status == HttpStatusCode.OK){
            val data = response.body<UserGetResponse.Success>()
            UserGetResponse.Success(
                data.uuid,
                data.username,
                data.email
            )
        }else{
            val data = response.body<UserGetResponse.Error>()
            UserGetResponse.Error(
                data.errorMessage
            )
        }
    }
}