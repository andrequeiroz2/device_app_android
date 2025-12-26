package com.dev.deviceapp.database.device

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction

@Dao
interface DeviceDao {
    
    // INSERT
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: DeviceEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevices(devices: List<DeviceEntity>)
    
    // READ
    @Query("SELECT * FROM devices")
    suspend fun getAllDevices(): List<DeviceEntity>
    
    @Query("SELECT * FROM devices WHERE uuid = :uuid")
    suspend fun getDeviceByUuid(uuid: String): DeviceEntity?
    
    // UPDATE
    @Update
    suspend fun updateDevice(device: DeviceEntity)
    
    @Update
    suspend fun updateDevices(devices: List<DeviceEntity>)
    
    // DELETE
    @Delete
    suspend fun deleteDevice(device: DeviceEntity)
    
    @Delete
    suspend fun deleteDevices(devices: List<DeviceEntity>)
    
    @Query("DELETE FROM devices WHERE uuid = :uuid")
    suspend fun deleteDeviceByUuid(uuid: String)
    
    @Query("DELETE FROM devices")
    suspend fun deleteAllDevices()
    
    // TRANSACTION
    @Transaction
    suspend fun replaceAllDevices(devices: List<DeviceEntity>) {
        deleteAllDevices()
        insertDevices(devices)
    }
}

