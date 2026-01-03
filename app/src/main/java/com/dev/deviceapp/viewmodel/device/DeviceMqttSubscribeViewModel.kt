package com.dev.deviceapp.viewmodel.device

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.deviceapp.database.device.DeviceDao
import com.dev.deviceapp.database.device.DeviceEntity
import com.dev.deviceapp.database.device.DeviceMessageEntity
import com.dev.deviceapp.database.device.DeviceMessageReceivedEntity
import com.dev.deviceapp.model.mqtt.MqttMessagePayload
import com.dev.deviceapp.mqtt.MqttConnectionState
import com.dev.deviceapp.repository.login.TokenRepository
import com.dev.deviceapp.repository.mqtt.MqttRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@HiltViewModel
class DeviceMqttSubscribeViewModel @Inject constructor(
    private val deviceDao: DeviceDao,
    private val mqttRepository: MqttRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val userUuid: String?
        get() = tokenRepository.getTokenInfoRepository()?.uuid

    private val subscribedTopics = mutableSetOf<String>()
    private var isObservingMessages = false

    private val _deviceUpdatedEvent = MutableSharedFlow<Unit>(replay = 0)
    val deviceUpdatedEvent: SharedFlow<Unit> = _deviceUpdatedEvent.asSharedFlow()

    init {
        // Iniciar observer de mensagens uma única vez
        observeReceivedMessages()
    }

    /**
     * Inicia o processo de subscribe para devices tipo Sensor
     * Deve ser chamado após a lista de devices estar carregada no banco
     */
    fun startSubscribing(devices: List<DeviceEntity>) {
        viewModelScope.launch {
            // Verificar se já está conectado
            if (mqttRepository.isConnected()) {
                subscribeToSensorDevices(devices)
            } else {
                // Observar conexão MQTT e fazer subscribe quando conectar (apenas uma vez)
                var subscribed = false
                mqttRepository.connectionState.collect { state ->
                    if (state is MqttConnectionState.Connected && !subscribed) {
                        subscribeToSensorDevices(devices)
                        subscribed = true
                    }
                }
            }
        }
    }

    /**
     * Faz subscribe nos topics dos devices tipo Sensor
     */
    private fun subscribeToSensorDevices(devices: List<DeviceEntity>) {
        val currentUserUuid = userUuid
        if (currentUserUuid == null) {
            Log.w("DeviceMqttSubscribeViewModel", "User UUID is null, cannot subscribe")
            return
        }

        devices.forEach { device ->
            if (device.deviceTypeText == "Sensor" && device.topic.isNotBlank()) {
                // Verificar se já está inscrito
                if (!subscribedTopics.contains(device.topic)) {
                    val success = mqttRepository.subscribe(device.topic, qos = 1)
                    if (success) {
                        subscribedTopics.add(device.topic)
                        Log.i("DeviceMqttSubscribeViewModel", "Subscribed to topic: ${device.topic}")
                    } else {
                        Log.w("DeviceMqttSubscribeViewModel", "Failed to subscribe to topic: ${device.topic}")
                    }
                }
            }
        }
    }

    /**
     * Observa mensagens recebidas e atualiza os devices no banco
     */
    private fun observeReceivedMessages() {
        if (isObservingMessages) return
        isObservingMessages = true

        viewModelScope.launch {
            mqttRepository.receivedMessages.collect { messagesMap ->
                messagesMap.forEach { (topic, payloadJson) ->
                    processMessage(topic, payloadJson)
                }
            }
        }
    }

    /**
     * Processa uma mensagem MQTT recebida
     */
    private fun processMessage(
        topic: String,
        payloadJson: String
    ) {
        viewModelScope.launch {
            try {
                // Parse do JSON
                val payload = json.decodeFromString<MqttMessagePayload>(payloadJson)

                val currentUserUuid = userUuid
                if (currentUserUuid == null) {
                    Log.w("DeviceMqttSubscribeViewModel", "User UUID is null, cannot process message")
                    return@launch
                }

                // Extrair userUuid e deviceUuid do topic
                // Formato: userUuid/deviceUuid/deviceName
                val topicParts = topic.split("/")
                if (topicParts.size < 2) {
                    Log.w("DeviceMqttSubscribeViewModel", "Invalid topic format: $topic")
                    return@launch
                }

                val topicUserUuid = topicParts[0]
                val topicDeviceUuid = topicParts[1]

                // Validar userUuid
                if (topicUserUuid != currentUserUuid) {
                    Log.d("DeviceMqttSubscribeViewModel", "Message from different user, ignoring: $topic")
                    return@launch
                }

                // Buscar device do banco
                val device = deviceDao.getDeviceByUuid(topicDeviceUuid)
                if (device == null) {
                    Log.w("DeviceMqttSubscribeViewModel", "Device not found for UUID: $topicDeviceUuid")
                    return@launch
                }

                // Atualizar device no banco
                updateDeviceMessage(device, payload)

            } catch (e: Exception) {
                Log.e("DeviceMqttSubscribeViewModel", "Error processing message: ${e.message}", e)
            }
        }
    }

    /**
     * Atualiza a mensagem do device no banco
     */
    private suspend fun updateDeviceMessage(
        device: DeviceEntity,
        payload: MqttMessagePayload
    ) {
        try {
            // Criar DeviceMessageReceivedEntity
            val messageReceived = DeviceMessageReceivedEntity(
                value = payload.payload,
                scale = payload.scale,
                timestamp = payload.timestamp
            )

            // Atualizar ou criar DeviceMessageEntity
            val currentMessages = device.messages?.toMutableList() ?: mutableListOf()
            val messageEntityIndex = currentMessages.indexOfFirst { it.deviceUuid == device.uuid }

            val updatedMessagesMap = if (messageEntityIndex >= 0) {
                // Atualizar Map existente
                val existingEntity = currentMessages[messageEntityIndex]
                val updatedMap = (existingEntity.messages?.toMutableMap() ?: mutableMapOf()).apply {
                    put(payload.metric, messageReceived)
                }
                updatedMap
            } else {
                // Criar novo Map
                mutableMapOf(payload.metric to messageReceived)
            }

            val messageEntity = DeviceMessageEntity(
                deviceUuid = device.uuid,
                messages = updatedMessagesMap
            )

            // Atualizar lista de messages
            if (messageEntityIndex >= 0) {
                currentMessages[messageEntityIndex] = messageEntity
            } else {
                currentMessages.add(messageEntity)
            }

            // Criar DeviceEntity atualizado
            val updatedDevice = device.copy(messages = currentMessages)

            // Salvar no banco
            deviceDao.updateDevice(updatedDevice)

            // Emitir evento para notificar que um device foi atualizado
            _deviceUpdatedEvent.emit(Unit)

            Log.d("DeviceMqttSubscribeViewModel", "Updated device ${device.uuid} with metric ${payload.metric} = ${payload.payload}${payload.scale}")

        } catch (e: Exception) {
            Log.e("DeviceMqttSubscribeViewModel", "Error updating device message: ${e.message}", e)
        }
    }

    /**
     * Faz unsubscribe quando device é removido
     */
    fun unsubscribeDevice(topic: String) {
        if (subscribedTopics.contains(topic)) {
            mqttRepository.unsubscribe(topic)
            subscribedTopics.remove(topic)
            Log.i("DeviceMqttSubscribeViewModel", "Unsubscribed from topic: $topic")
        }
    }

    /**
     * Atualiza a lista de devices (chamado quando devices são adicionados/removidos)
     */
    fun updateDevicesList(devices: List<DeviceEntity>) {
        viewModelScope.launch {
            // Verificar se está conectado antes de fazer subscribe
            if (mqttRepository.isConnected()) {
                subscribeToSensorDevices(devices)
                
                // Unsubscribe de topics que não estão mais na lista
                val currentTopics = devices
                    .filter { it.deviceTypeText == "Sensor" && it.topic.isNotBlank() }
                    .map { it.topic }
                    .toSet()
                
                subscribedTopics.toList().forEach { topic ->
                    if (!currentTopics.contains(topic)) {
                        unsubscribeDevice(topic)
                    }
                }
            }
        }
    }
}

