package com.dev.deviceapp.config

import android.content.Context
import android.util.Log
import com.dev.deviceapp.mqtt.MqttConfigurationException
import java.util.*

class ApiRoutes(
    private val context: Context
) {

    private val props: Properties by lazy {
        Properties().apply {
            context.assets.open("config.properties").use { load(it) }
        }
    }

    private val ngrokEnabled = props.getProperty("NGROK_ENABLED")?.toBoolean() ?: false
    private val ngrokUrl = props.getProperty("NGROK_URL")?.trimEnd('/') ?: ""
    private val localUrl = props.getProperty("URL_LOCAL")?.trimEnd('/') ?: ""

    private val mqttTunnelEnabled = props.getProperty("MQTT_TUNNEL")?.toBoolean() ?: false

    private val mqttUrl = props.getProperty("MQTT_URL")?: ""


    companion object {
        const val ROUTE_LOGIN = "/login"

        const val ROUTE_USER_POST = "/user/create"
        const val ROUTE_USER_GET = "/user"
        const val ROUTE_USER_PUT = "/user/%s"
        const val ROUTE_USER_DELETE = "/user/%s"

        const val ROUTE_BROKER_POST = "/broker"
        const val ROUTE_BROKER_GET = "/broker"
        const val ROUTE_BROKER_PUT = "/broker/%s"
        const val ROUTE_BROKER_DELETE = "/broker/%s"

        const val ROUTER_DEVICE_POST = "/device"

        const val ROUTE_DEVICE_GET_OWNED_USER = "/device/owned"
    }

    // Map com as rotas do seu backend
    private val routes = mapOf(
        "login" to ROUTE_LOGIN,

        "broker_post" to ROUTE_BROKER_POST,
        "broker_get" to ROUTE_BROKER_GET,
        "broker_put" to ROUTE_BROKER_PUT,
        "broker_delete" to ROUTE_BROKER_DELETE,

        "user_post" to ROUTE_USER_POST,
        "user_get" to ROUTE_USER_GET,
        "user_put" to ROUTE_USER_PUT,
        "user_delete" to ROUTE_USER_DELETE,

        "device_post" to ROUTER_DEVICE_POST,
        "device_get_owned_user" to ROUTE_DEVICE_GET_OWNED_USER
    )

    fun getUrl(key: String, vararg args: String): String {
        val template = routes[key] ?: throw IllegalArgumentException("Route: '$key' undefined route")
        val path = if (args.isNotEmpty()) String.format(template, *args) else template
        val base = if (ngrokEnabled) ngrokUrl else localUrl
        return "$base$path"
    }

    fun getMqttUrl(port: Int? = null, host: String? = null): String {
        if (mqttTunnelEnabled) {
            if (mqttUrl.isBlank()) {
                Log.e("ApiRoutes", "=========================================")
                Log.e("ApiRoutes", "MQTT Configuration Error")
                Log.e("ApiRoutes", "=========================================")
                Log.e("ApiRoutes", "MQTT_TUNNEL: mqttTunnelEnabled (enabled)")
                Log.e("ApiRoutes", "MQTT_URL: '$mqttUrl' (isBlank: ${mqttUrl.isBlank()})")
                Log.e("ApiRoutes", "=========================================")
                Log.e("ApiRoutes", "The application will crash because MQTT tunnel is enabled")
                Log.e("ApiRoutes", "but MQTT_URL is not configured in config.properties")
                Log.e("ApiRoutes", "=========================================")
                
                val errorMsg = "FATAL: MQTT tunnel is enabled (MQTT_TUNNEL=true) but MQTT_URL is not configured in config.properties. " +
                        "Current MQTT_URL value: '$mqttUrl'. " +
                        "Please configure MQTT_URL in config.properties or set MQTT_TUNNEL=false."

                throw RuntimeException(errorMsg)
            }
            Log.i("ApiRoutes", "Using MQTT tunnel URL: $mqttUrl")
            return mqttUrl
        }

        if (port == null || host == null) {
            Log.e("ApiRoutes", "=========================================")
            Log.e("ApiRoutes", "MQTT Configuration Error")
            Log.e("ApiRoutes", "=========================================")
            Log.e("ApiRoutes", "MQTT_TUNNEL: mqttTunnelEnabled (disabled)")
            Log.e("ApiRoutes", "Parameters received:")
            Log.e("ApiRoutes", "  - port: $port ${if (port == null) "(NULL)" else ""}")
            Log.e("ApiRoutes", "  - host: $host ${if (host == null) "(NULL)" else ""}")
            Log.e("ApiRoutes", "=========================================")
            Log.e("ApiRoutes", "MQTT tunnel is disabled but required parameters are missing.")
            Log.e("ApiRoutes", "Provide valid host and port parameters.")
            Log.e("ApiRoutes", "=========================================")
            
            val errorMsg = "MQTT URL not configured: port=$port, host=$host. " +
                    "Provide valid host and port parameters."
            
            throw MqttConfigurationException(errorMsg)
        }

        return "$host:$port"
    }
}