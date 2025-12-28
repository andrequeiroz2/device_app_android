package com.dev.deviceapp.repository.mqtt

import android.content.Context
import android.util.Log
import com.dev.deviceapp.database.broker.BrokerDao
import com.dev.deviceapp.database.broker.BrokerEntity
import com.dev.deviceapp.mqtt.MqttClient
import com.dev.deviceapp.mqtt.MqttConnectionState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MqttRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val brokerDao: BrokerDao,
    private val mqttClient: MqttClient
) {
    
    /**
     * Estado da conexão MQTT
     */
    val connectionState: StateFlow<MqttConnectionState> = mqttClient.connectionState
    
    /**
     * Mensagens recebidas por tópico
     */
    val receivedMessages: StateFlow<Map<String, String>> = mqttClient.receivedMessages
    
    /**
     * Conecta ao broker salvo no banco (primeiro broker conectado)
     * @return true se a conexão foi iniciada com sucesso
     */
    suspend fun connect(): Boolean {
        return try {
            val broker = brokerDao.getConnectedBroker()
            if (broker != null) {
                Log.i("MqttRepository", "Connecting to broker: ${broker.host}:${broker.port}")
                mqttClient.connect(broker)
            } else {
                Log.w("MqttRepository", "No connected broker found in database")
                false
            }
        } catch (e: Exception) {
            Log.e("MqttRepository", "Error connecting: ${e.message}", e)
            false
        }
    }
    
    /**
     * Conecta a um broker específico
     * @param broker Broker para conectar
     * @return true se a conexão foi iniciada com sucesso
     */
    fun connect(broker: BrokerEntity): Boolean {
        return try {
            Log.i("MqttRepository", "Connecting to broker: ${broker.host}:${broker.port}")
            mqttClient.connect(broker)
        } catch (e: Exception) {
            Log.e("MqttRepository", "Error connecting: ${e.message}", e)
            false
        }
    }
    
    /**
     * Desconecta do broker MQTT
     */
    fun disconnect() {
        Log.i("MqttRepository", "Disconnecting from broker")
        mqttClient.disconnect()
    }
    
    /**
     * Inscreve-se em um tópico
     * @param topic Tópico para se inscrever
     * @param qos Nível de qualidade de serviço (0, 1 ou 2)
     * @return true se a inscrição foi iniciada com sucesso
     */
    fun subscribe(topic: String, qos: Int = 1): Boolean {
        Log.i("MqttRepository", "Subscribing to topic: $topic with QoS: $qos")
        return mqttClient.subscribe(topic, qos)
    }
    
    /**
     * Cancela a inscrição de um tópico
     * @param topic Tópico para cancelar inscrição
     * @return true se o cancelamento foi iniciado com sucesso
     */
    fun unsubscribe(topic: String): Boolean {
        Log.i("MqttRepository", "Unsubscribing from topic: $topic")
        return mqttClient.unsubscribe(topic)
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
        Log.i("MqttRepository", "Publishing message to topic: $topic")
        return mqttClient.publish(topic, message, qos, retained)
    }
    
    /**
     * Verifica se está conectado
     */
    fun isConnected(): Boolean {
        return mqttClient.isConnected()
    }
}
