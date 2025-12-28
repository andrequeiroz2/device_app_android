package com.dev.deviceapp.database.broker

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface BrokerDao {
    
    // INSERT
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBroker(broker: BrokerEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrokers(brokers: List<BrokerEntity>)
    
    // READ
    @Query("SELECT * FROM brokers")
    suspend fun getAllBrokers(): List<BrokerEntity>
    
    @Query("SELECT * FROM brokers WHERE uuid = :uuid")
    suspend fun getBrokerByUuid(uuid: String): BrokerEntity?
    
    @Query("SELECT * FROM brokers WHERE connected = 1 LIMIT 1")
    suspend fun getConnectedBroker(): BrokerEntity?
    
    // UPDATE
    @Update
    suspend fun updateBroker(broker: BrokerEntity)
    
    @Update
    suspend fun updateBrokers(brokers: List<BrokerEntity>)
    
    // DELETE
    @Delete
    suspend fun deleteBroker(broker: BrokerEntity)
    
    @Delete
    suspend fun deleteBrokers(brokers: List<BrokerEntity>)
    
    @Query("DELETE FROM brokers WHERE uuid = :uuid")
    suspend fun deleteBrokerByUuid(uuid: String)
    
    @Query("DELETE FROM brokers")
    suspend fun deleteAllBrokers()
}

