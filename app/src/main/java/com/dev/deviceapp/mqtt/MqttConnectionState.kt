package com.dev.deviceapp.mqtt

sealed class MqttConnectionState {
    object Disconnected : MqttConnectionState()
    object Connecting : MqttConnectionState()
    object Connected : MqttConnectionState()

    data class Error(val message: String) : MqttConnectionState()
}

