package com.dev.deviceapp.di

import android.content.Context
import com.dev.deviceapp.mqtt.MqttClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MqttModule {
    
    @Provides
    @Singleton
    fun provideMqttClient(@ApplicationContext context: Context): MqttClient {
        return MqttClient(context)
    }
}

