package com.dev.deviceapp.di

import android.util.Log
import com.dev.deviceapp.repository.login.TokenRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger as KtorLogger
import io.ktor.serialization.kotlinx.json.json
import jakarta.inject.Named
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @Named("HttpClientUnauthenticated")
    fun provideKtorUnauthenticateHttpClient(): HttpClient {
        return HttpClient(Android) {

            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }

            install(Logging) {
                logger = KtorLogger.DEFAULT
                level = LogLevel.ALL
            }

            engine {
                connectTimeout = 50_000 // ms
                socketTimeout = 50_000 // ms
            }
        }
    }

    @Provides
    @Singleton
    @Named("HttpClientAuthenticated")
    fun provideAuthenticatedHttpClient(
        tokenRepository: TokenRepository
    ): HttpClient {
        return HttpClient(Android) {

            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }

            install(Logging) {
                logger = KtorLogger.DEFAULT
                level = LogLevel.ALL
            }

            install(DefaultRequest) {
                val token = runBlocking { tokenRepository.getToken() }
                if (!token.isNullOrBlank()) {
                    headers.append("Authorization", "Bearer $token")
                    Log.i("NetworkModule", "Token added to header $token")
                }
            }

            engine {
                connectTimeout = 50_000
                socketTimeout = 50_000
            }
        }
    }

}
