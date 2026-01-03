package com.dev.deviceapp.mqtt

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.dev.deviceapp.config.ApiRoutes
import com.dev.deviceapp.database.broker.BrokerEntity
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttClientState
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class MqttClient(
    private val context: Context
) {
    private var mqttClient: Mqtt3AsyncClient? = null
    private val clientScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _connectionState = MutableStateFlow<MqttConnectionState>(MqttConnectionState.Disconnected)
    val connectionState: StateFlow<MqttConnectionState> = _connectionState.asStateFlow()
    
    private val _receivedMessages = MutableStateFlow<Map<String, String>>(emptyMap())
    val receivedMessages: StateFlow<Map<String, String>> = _receivedMessages.asStateFlow()
    
    /**
     * Conecta ao broker MQTT
     * @param broker Informações do broker salvo no banco
     * @return true se a conexão foi iniciada com sucesso
     */
    fun connect(broker: BrokerEntity): Boolean {
        return try {
            _connectionState.value = MqttConnectionState.Connecting
            
            val apiRoutes = ApiRoutes(context)
            val serverUri = try {
                apiRoutes.getMqttUrl(port = broker.port, host = broker.host)
            } catch (e: MqttConfigurationException) {
                val errorMessage = e.message ?: "MQTT configuration error"
                Toast.makeText(
                    context,
                    "Error config MQTT: $errorMessage",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("MqttClient", "MQTT configuration error: $errorMessage", e)
                _connectionState.value = MqttConnectionState.Error(errorMessage)
                return false
            }
            
            // Parse server URI - handle different protocols (mqtt://, tcp://, ssl://, ws://, wss://)
            val isWebSocket = serverUri.startsWith("ws://") || serverUri.startsWith("wss://")
            val cleanUri = serverUri
                .removePrefix("mqtt://")
                .removePrefix("tcp://")
                .removePrefix("ssl://")
                .removePrefix("ws://")
                .removePrefix("wss://")
                .trim()
            
            // Extract path if present (e.g., /mqtt)
            val pathParts = cleanUri.split("/", limit = 2)
            val hostAndPortStr = pathParts[0]
            val webSocketPath = if (pathParts.size > 1) "/${pathParts[1]}" else "/mqtt"
            
            // Determine default port based on protocol
            val defaultPort = when {
                serverUri.startsWith("wss://") || serverUri.startsWith("ssl://") -> 443
                serverUri.startsWith("ws://") -> 80
                else -> 1883 // tcp:// default
            }
            
            // Extract host and port
            val hostAndPort: Pair<String, Int> = if (hostAndPortStr.contains(":")) {
                val parts = hostAndPortStr.split(":")
                val hostPart = parts[0]
                val portPart = parts[1]
                Pair(hostPart, portPart.toIntOrNull() ?: defaultPort)
            } else {
                Pair(hostAndPortStr, defaultPort)
            }
            val host = hostAndPort.first
            val port = hostAndPort.second
            
            Log.i("MqttClient", "Connecting to MQTT server: $host:$port (from URI: $serverUri)")
            if (isWebSocket) {
                Log.i("MqttClient", "WebSocket path: $webSocketPath")
            }
            
            // Check if SSL/TLS is needed
            val useSsl = serverUri.startsWith("wss://") || serverUri.startsWith("ssl://")
            Log.i("MqttClient", "Using SSL/TLS: $useSsl, Using WebSocket: $isWebSocket")
            
            // Create HiveMQ client with automatic reconnect
            val clientBuilder = MqttClient.builder()
                .useMqttVersion3()
                .serverHost(host)
                .serverPort(port)
                //.identifier(broker.clientId.ifEmpty { UUID.randomUUID().toString() })
                .identifier(UUID.randomUUID().toString())
                .automaticReconnectWithDefaultConfig()
            
            // Configure WebSocket if needed
            if (isWebSocket) {
                clientBuilder.webSocketConfig()
                    .serverPath(webSocketPath)
                    .subprotocol("mqtt")
                    .applyWebSocketConfig()
                Log.i("MqttClient", "WebSocket configured with path: $webSocketPath")
            }
            
            // Configure SSL/TLS if needed
            if (useSsl) {
                clientBuilder.sslWithDefaultConfig()
                Log.i("MqttClient", "SSL/TLS configured")
            }
            
            mqttClient = clientBuilder.buildAsync()
            
            // Build connect options
            val connectBuilder = mqttClient!!.connectWith()
                .cleanSession(broker.cleanSession)
                .keepAlive(broker.keepAlive)
                .apply {
                    // Last Will and Testament
                    if (broker.lastWillTopic.isNotEmpty() && broker.lastWillMessage.isNotEmpty()) {
                        val willQos = MqttQos.fromCode(broker.lastWillQos) ?: MqttQos.AT_LEAST_ONCE
                        willPublish()
                            .topic(broker.lastWillTopic)
                            .payload(broker.lastWillMessage.toByteArray())
                            .qos(willQos)
                            .retain(broker.lastWillRetain)
                            .applyWillPublish()
                    }
                }
            
            // Connect asynchronously with timeout
            clientScope.launch {
                try {
                    Log.i("MqttClient", "Sending connection request...")
                    
                    // Add timeout to connection (10 seconds)
                    val connAck: Mqtt3ConnAck = try {
                        connectBuilder.send().get()
                    } catch (e: java.util.concurrent.TimeoutException) {
                        Log.e("MqttClient", "Connection timeout after 10 seconds")
                        _connectionState.value = MqttConnectionState.Error("Connection timeout - The server may not support this connection type (WebSocket wss:// requires special support)")
                        return@launch
                    } catch (e: java.util.concurrent.ExecutionException) {
                        val cause = e.cause ?: e
                        Log.e("MqttClient", "Connection execution error: ${cause.javaClass.simpleName} - ${cause.message}", cause)
                        throw cause
                    }
                    
                    Log.i("MqttClient", "Connection response received: ${connAck.returnCode}")
                    
                    if (connAck.returnCode.isError) {
                        val errorMsg = "Connection failed: ${connAck.returnCode}"
                        Log.e("MqttClient", errorMsg)
                        _connectionState.value = MqttConnectionState.Error(errorMsg)
                    } else {
                        Log.i("MqttClient", "✓ Connected successfully to broker: $host:$port")
                        _connectionState.value = MqttConnectionState.Connected
                    }
                } catch (e: Exception) {
                    Log.e("MqttClient", "Connection exception: ${e.javaClass.simpleName} - ${e.message}", e)
                    val errorMsg = if (serverUri.startsWith("wss://") || serverUri.startsWith("ws://")) {
                        "WebSocket (wss://) is not supported by HiveMQ MQTT Client. Use TCP (tcp://) or SSL (ssl://) instead."
                    } else {
                        e.message ?: "Connection failed"
                    }
                    _connectionState.value = MqttConnectionState.Error(errorMsg)
                }
            }
            
            true
        } catch (e: Exception) {
            Log.e("MqttClient", "Error connecting: ${e.message}", e)
            _connectionState.value = MqttConnectionState.Error(e.message ?: "Unknown error")
            false
        }
    }
    
    // Message callback is set up in subscribe() method using callback parameter
    
    /**
     * Desconecta do broker MQTT
     */
    fun disconnect() {
        try {
            clientScope.launch {
                try {
                    mqttClient?.disconnect()?.get()
                    Log.i("MqttClient", "Disconnected from broker")
                    _connectionState.value = MqttConnectionState.Disconnected
                } catch (e: Exception) {
                    Log.e("MqttClient", "Disconnect failed: ${e.message}", e)
                    _connectionState.value = MqttConnectionState.Error(e.message ?: "Disconnect failed")
                }
            }
        } catch (e: Exception) {
            Log.e("MqttClient", "Error disconnecting: ${e.message}", e)
        }
    }
    
    /**
     * Inscreve-se em um tópico
     * @param topic Tópico para se inscrever
     * @param qos Nível de qualidade de serviço (0, 1 ou 2)
     * @return true se a inscrição foi iniciada com sucesso
     */
    fun subscribe(topic: String, qos: Int = 1): Boolean {
        return try {
            if (mqttClient != null && isConnected()) {
                clientScope.launch {
                    try {
                        val subscribeQos = MqttQos.fromCode(qos) ?: MqttQos.AT_LEAST_ONCE
                        mqttClient!!.subscribeWith()
                            .topicFilter(topic)
                            .qos(subscribeQos)
                            .callback { publish ->
                                try {
                                    val topicStr = publish.topic.toString()
                                    val payload = String(publish.payloadAsBytes, Charsets.UTF_8)
                                    Log.i("MqttClient", "Message arrived - Topic: $topicStr, Payload: $payload")
                                    
                                    _receivedMessages.value = _receivedMessages.value.toMutableMap().apply {
                                        put(topicStr, payload)
                                    }
                                } catch (e: Exception) {
                                    Log.e("MqttClient", "Error processing message: ${e.message}", e)
                                }
                            }
                            .send()
                            .get()
                        Log.i("MqttClient", "Subscribed to topic: $topic with QoS: $qos")
                    } catch (e: Exception) {
                        Log.e("MqttClient", "Subscribe failed for topic $topic: ${e.message}", e)
                    }
                }
                true
            } else {
                Log.w("MqttClient", "Cannot subscribe: not connected")
                false
            }
        } catch (e: Exception) {
            Log.e("MqttClient", "Error subscribing to $topic: ${e.message}", e)
            false
        }
    }

    fun unsubscribe(topic: String): Boolean {
        return try {
            if (mqttClient != null && isConnected()) {
                clientScope.launch {
                    try {
                        mqttClient!!.unsubscribeWith()
                            .topicFilter(topic)
                            .send()
                            .get()
                        Log.i("MqttClient", "Unsubscribed from topic: $topic")
                        _receivedMessages.value = _receivedMessages.value.toMutableMap().apply {
                            remove(topic)
                        }
                    } catch (e: Exception) {
                        Log.e("MqttClient", "Unsubscribe failed for topic $topic: ${e.message}", e)
                    }
                }
                true
            } else {
                Log.w("MqttClient", "Cannot unsubscribe: not connected")
                false
            }
        } catch (e: Exception) {
            Log.e("MqttClient", "Error unsubscribing from $topic: ${e.message}", e)
            false
        }
    }

    fun publish(topic: String, message: String, qos: Int = 1, retained: Boolean = false): Boolean {
        return try {
            if (mqttClient != null && isConnected()) {
                clientScope.launch {
                    try {
                        val publishQos = MqttQos.fromCode(qos) ?: MqttQos.AT_LEAST_ONCE
                        mqttClient!!.publishWith()
                            .topic(topic)
                            .payload(message.toByteArray(Charsets.UTF_8))
                            .qos(publishQos)
                            .retain(retained)
                            .send()
                            .get()
                        Log.i("MqttClient", "Published message to topic: $topic")
                    } catch (e: Exception) {
                        Log.e("MqttClient", "Publish failed for topic $topic: ${e.message}", e)
                    }
                }
                true
            } else {
                Log.w("MqttClient", "Cannot publish: not connected")
                false
            }
        } catch (e: Exception) {
            Log.e("MqttClient", "Error publishing to $topic: ${e.message}", e)
            false
        }
    }

    fun isConnected(): Boolean {
        return try {
            mqttClient?.state == MqttClientState.CONNECTED
        } catch (e: Exception) {
            false
        }
    }
}
