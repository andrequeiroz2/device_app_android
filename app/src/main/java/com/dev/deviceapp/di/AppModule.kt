package com.dev.deviceapp.di

import android.content.Context
import com.dev.deviceapp.common.ConfigLoader
import com.dev.deviceapp.repository.device.DeviceBleScanRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideConfigLoader(@ApplicationContext context: Context): ConfigLoader {
        return ConfigLoader(context)
    }

    @Provides
    @Singleton
    fun provideDeviceBleScanRepository(
        @ApplicationContext context: Context,
        configLoader: ConfigLoader
    ): DeviceBleScanRepository {
        return DeviceBleScanRepository(context, configLoader)
    }
}
