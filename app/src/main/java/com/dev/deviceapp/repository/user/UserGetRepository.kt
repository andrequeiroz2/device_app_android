package com.dev.deviceapp.repository.user

import android.content.Context
import com.dev.deviceapp.config.ApiRoutes
import com.dev.deviceapp.model.user.UserGetRequest
import com.dev.deviceapp.model.user.UserGetResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import jakarta.inject.Inject
import jakarta.inject.Named

class UserGetRepository @Inject constructor(
    @Named("HttpClientAuthenticated") private val client: HttpClient,
    @ApplicationContext private val context: Context
){

    private val apiRoutes = ApiRoutes(context)
    suspend fun getUser(param: UserGetRequest): UserGetResponse{

        val url = apiRoutes.getUrl("user_get")

        val response = client.get(url){

            if(param.uuid?.isNotBlank() == true){
                parameter("uuid", param.uuid)
            }

            if(param.email?.isNotBlank() == true){
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