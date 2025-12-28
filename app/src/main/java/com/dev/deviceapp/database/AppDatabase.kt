package com.dev.deviceapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dev.deviceapp.database.broker.BrokerDao
import com.dev.deviceapp.database.broker.BrokerEntity
import com.dev.deviceapp.database.device.DeviceDao
import com.dev.deviceapp.database.device.DeviceEntity

@Database(
    entities = [DeviceEntity::class, BrokerEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun brokerDao(): BrokerDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.inMemoryDatabaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

