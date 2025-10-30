package com.dev.deviceapp.ble

import java.util.UUID

object BleConstants {
    val BLE_SERVICE_UUID: UUID = UUID.fromString("19b10000-e8f2-537e-4f6c-d104768a1214")
    val BLE_ADOPTION_UUID: UUID = UUID.fromString("19b10001-e8f2-537e-4f6c-d104768a1214")
    val BLE_RESPONSE_ADOPTION_UUID: UUID = UUID.fromString("19b10002-e8f2-537e-4f6c-d104768a1214")
    val BLE_DEVICE_INFO_UUID: UUID = UUID.fromString("19b10003-e8f2-537e-4f6c-d104768a1214")

    val BLE_CLIENT_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
}
