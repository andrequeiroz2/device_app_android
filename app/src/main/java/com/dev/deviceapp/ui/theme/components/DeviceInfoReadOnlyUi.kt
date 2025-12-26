package com.dev.deviceapp.ui.theme.components

import androidx.compose.runtime.Composable
import com.dev.deviceapp.model.device.DeviceBleInfoModel

fun String?.orDash(): String {
    return if (this.isNullOrBlank() || this == "null") {
        "----"
    } else {
        this
    }
}

@Composable
fun DeviceInfoReadOnly(info: DeviceBleInfoModel) {

    DetailRow("Device UUID", info.device_uuid.orDash())
    DividerSpace()

    DetailRow("Device Name", info.device_name.orDash())
    DividerSpace()

    DetailRow("MAC Address", info.mac_address.orDash())
    DividerSpace()

    DetailRow("Board Type", info.boarder_type.orDash())
    DividerSpace()

    DetailRow("Device Type", info.device_type.orDash())
    DividerSpace()

    DetailRow("Sensor Type", info.sensor_type.orDash())
    DividerSpace()

    DetailRow("Actuator Type", info.actuator_type.orDash())
    DividerSpace()

    DetailRow("Broker URL", info.broker_url.orDash())
    DividerSpace()

    DetailRow("MQTT Topic", info.topic.orDash())
    DividerSpace()

    val scaleText = if (info.device_scale.isEmpty()) {
        "---"
    } else {
        info.device_scale.joinToString("\n") { row ->
            row.joinToString(" = ").orDash()
        }
    }

    DetailRow("Device Scale", scaleText)
    DividerSpace()

    DetailRow(
        "Adoption Status",
        when (info.adopted_status) {
            0 -> "Unadopted"
            1 -> "Adopted"
            else -> "----"
        }
    )
    DividerSpace()

    DetailRow("User UUID", info.user_uuid.orDash())
}