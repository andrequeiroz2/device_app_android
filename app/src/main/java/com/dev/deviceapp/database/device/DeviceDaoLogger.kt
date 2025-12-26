package com.dev.deviceapp.database.device

import android.util.Log

/**
 * Helper object para logar o estado do banco de dados após operações CRUD
 * 
 * Exemplos de uso:
 * 
 * // Após INSERT
 * deviceDao.insertDevice(device)
 * DeviceDaoLogger.logDatabaseState(deviceDao, "INSERT")
 * 
 * // Após UPDATE
 * deviceDao.updateDevice(device)
 * DeviceDaoLogger.logDatabaseState(deviceDao, "UPDATE")
 * 
 * // Após DELETE
 * deviceDao.deleteDevice(device)
 * DeviceDaoLogger.logDatabaseState(deviceDao, "DELETE")
 * 
 * // Após buscar um dispositivo específico
 * val device = deviceDao.getDeviceByUuid(uuid)
 * DeviceDaoLogger.logDevice(device, "GET_BY_UUID")
 */
object DeviceDaoLogger {
    private const val TAG = "DeviceDaoLogger"
    
    /**
     * Loga o estado atual do banco de dados após uma operação CRUD
     * Mostra exatamente o que retorna do banco
     * 
     * @param deviceDao O DAO para acessar o banco
     * @param operation Nome da operação realizada (ex: "INSERT", "UPDATE", "DELETE")
     */
    suspend fun logDatabaseState(deviceDao: DeviceDao, operation: String) {
        try {
            val allDevices = deviceDao.getAllDevices()
            
            Log.i(TAG, "=== Database State after $operation ===")
            Log.i(TAG, "Total devices in database: ${allDevices.size}")
            
            if (allDevices.isEmpty()) {
                Log.i(TAG, "Database is empty")
            } else {
                allDevices.forEachIndexed { index, device ->
                    Log.i(TAG, 
                        "Device[$index]: " +
                        "uuid=${device.uuid}, " +
                        "name=${device.name}, " +
                        "macAddress=${device.macAddress}, " +
                        "deviceType=${device.deviceTypeText}, " +
                        "boardType=${device.boardTypeText}, " +
                        "sensorType=${device.sensorType ?: "N/A"}, " +
                        "actuatorType=${device.actuatorType ?: "N/A"}, " +
                        "condition=${device.deviceConditionText}, " +
                        "messages=${device.messages?.size ?: 0}"
                    )
                }
            }
            
            Log.i(TAG, "=== End Database State ===")
        } catch (e: Exception) {
            Log.e(TAG, "Error logging database state: ${e.message}", e)
        }
    }
    
    /**
     * Loga um dispositivo específico retornado do banco
     * 
     * @param device O dispositivo retornado (pode ser null)
     * @param operation Nome da operação realizada
     */
    fun logDevice(device: DeviceEntity?, operation: String) {
        if (device == null) {
            Log.i(TAG, "=== $operation: Device not found ===")
        } else {
            Log.i(TAG, "=== $operation: Device found ===")
            Log.i(TAG, 
                "Device: " +
                "uuid=${device.uuid}, " +
                "name=${device.name}, " +
                "macAddress=${device.macAddress}, " +
                "deviceType=${device.deviceTypeText}, " +
                "boardType=${device.boardTypeText}, " +
                "sensorType=${device.sensorType ?: "N/A"}, " +
                "actuatorType=${device.actuatorType ?: "N/A"}, " +
                "condition=${device.deviceConditionText}, " +
                "messages=${device.messages?.size ?: 0}"
            )
        }
    }
}

