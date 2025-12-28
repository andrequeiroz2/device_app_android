package com.dev.deviceapp.mqtt

import android.content.Context
import android.util.Log
import com.dev.deviceapp.database.broker.BrokerEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class MqttClient(
    private val context: Context
) {
    private var mqttClient: MqttAndroidClient? = null
    
    private val _connectionState = MutableStateFlow<MqttConnectionState>(MqttConnectionState.Disconnected)
    val connectionState: StateFlow<MqttConnectionState> = _connectionState.asStateFlow()
    
    private val _receivedMessages = MutableStateFlow<Map<String, String>>(emptyMap())
    val receivedMessages: StateFlow<Map<String, String>> = _receivedMessages.asStateFlow()
    
    private val connectionCallback = object : MqttCallback {
        override fun connectionLost(cause: Throwable?) {
            Log.e("MqttClient", "Connection lost: ${cause?.message}")
            _connectionState.value = MqttConnectionState.Error(cause?.message ?: "Connection lost")
        }
        
        override fun messageArrived(topic: String, message: MqttMessage) {
            val payload = String(message.payload, Charsets.UTF_8)
            Log.i("MqttClient", "Message arrived - Topic: $topic, Payload: $payload")
            
            _receivedMessages.value = _receivedMessages.value + (topic to payload)
        }
        
        override fun deliveryComplete(token: IMqttDeliveryToken) {
            Log.i("MqttClient", "Message delivered: ${token.messageId}")
        }
    }
    
    /**
     * Conecta ao broker MQTT
     * @param broker Informações do broker salvo no banco
     * @return true se a conexão foi iniciada com sucesso
     */
    fun connect(broker: BrokerEntity): Boolean {
        return try {
            _connectionState.value = MqttConnectionState.Connecting
            
            // Limpar o host removendo prefixos como mqtt://, tcp://, etc.
            val cleanHost = broker.host
                .removePrefix("mqtt://")
                .removePrefix("tcp://")
                .removePrefix("ssl://")
                .removePrefix("ws://")
                .removePrefix("wss://")
                .trim()
            
            val serverUri = "tcp://$cleanHost:${broker.port}"
            Log.i("MqttClient", "Cleaned host: '$cleanHost' (original: '${broker.host}')")
            mqttClient = MqttAndroidClient(context, serverUri, broker.clientId)
            mqttClient?.setCallback(connectionCallback)
            
            val options = MqttConnectOptions().apply {
                isCleanSession = broker.cleanSession
                keepAliveInterval = broker.keepAlive
                isAutomaticReconnect = true
                
                // Last Will and Testament
                if (broker.lastWillTopic.isNotEmpty() && broker.lastWillMessage.isNotEmpty()) {
                    setWill(
                        broker.lastWillTopic,
                        broker.lastWillMessage.toByteArray(),
                        broker.lastWillQos,
                        broker.lastWillRetain
                    )
                }
            }
            
            mqttClient?.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.i("MqttClient", "Connected to broker: $serverUri")
                    _connectionState.value = MqttConnectionState.Connected
                }
                
                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Log.e("MqttClient", "Connection failed: ${exception.message}")
                    _connectionState.value = MqttConnectionState.Error(exception.message ?: "Connection failed")
                }
            })
            
            true
        } catch (e: Exception) {
            Log.e("MqttClient", "Error connecting: ${e.message}", e)
            _connectionState.value = MqttConnectionState.Error(e.message ?: "Unknown error")
            false
        }
    }
    
    /**
     * Desconecta do broker MQTT
     */
    fun disconnect() {
        try {
            mqttClient?.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.i("MqttClient", "Disconnected from broker")
                    _connectionState.value = MqttConnectionState.Disconnected
                }
                
                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Log.e("MqttClient", "Disconnect failed: ${exception.message}")
                    _connectionState.value = MqttConnectionState.Error(exception.message ?: "Disconnect failed")
                }
            })
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
            if (mqttClient?.isConnected == true) {
                mqttClient?.subscribe(topic, qos, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        Log.i("MqttClient", "Subscribed to topic: $topic with QoS: $qos")
                    }
                    
                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        Log.e("MqttClient", "Subscribe failed for topic $topic: ${exception.message}")
                    }
                })
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
    
    /**
     * Cancela a inscrição de um tópico
     * @param topic Tópico para cancelar inscrição
     * @return true se o cancelamento foi iniciado com sucesso
     */
    fun unsubscribe(topic: String): Boolean {
        return try {
            if (mqttClient?.isConnected == true) {
                mqttClient?.unsubscribe(topic, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        Log.i("MqttClient", "Unsubscribed from topic: $topic")
                        _receivedMessages.value = _receivedMessages.value - topic
                    }
                    
                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        Log.e("MqttClient", "Unsubscribe failed for topic $topic: ${exception.message}")
                    }
                })
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
    
    /**
     * Publica uma mensagem em um tópico
     * @param topic Tópico para publicar
     * @param message Mensagem a ser publicada
     * @param qos Nível de qualidade de serviço (0, 1 ou 2)
     * @param retained Se a mensagem deve ser retida pelo broker
     * @return true se a publicação foi iniciada com sucesso
     */
    fun publish(topic: String, message: String, qos: Int = 1, retained: Boolean = false): Boolean {
        return try {
            if (mqttClient?.isConnected == true) {
                val mqttMessage = MqttMessage(message.toByteArray(Charsets.UTF_8)).apply {
                    this.qos = qos
                    isRetained = retained
                }
                
                mqttClient?.publish(topic, mqttMessage, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        Log.i("MqttClient", "Published message to topic: $topic")
                    }
                    
                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        Log.e("MqttClient", "Publish failed for topic $topic: ${exception.message}")
                    }
                })
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
    
    /**
     * Verifica se está conectado
     */
    fun isConnected(): Boolean {
        return mqttClient?.isConnected == true
    }
}

