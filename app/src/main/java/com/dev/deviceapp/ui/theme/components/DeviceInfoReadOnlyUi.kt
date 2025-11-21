package com.dev.deviceapp.ui.theme.components

import androidx.compose.runtime.Composable
import com.dev.deviceapp.model.device.DeviceBleInfoModel

@Composable
fun DeviceInfoReadOnly(info: DeviceBleInfoModel) {
    DetailRow("Device UUID", info.device_uuid)
    DividerSpace()
    DetailRow("Device Name", info.device_name)
    DividerSpace()
    DetailRow("MAC Address", info.mac_address)
    DividerSpace()
    DetailRow("Board Type", info.boarder_type)
    DividerSpace()
    DetailRow("Device Type", info.device_type)
    DividerSpace()
    DetailRow("Sensor Type", info.sensor_type)
    DividerSpace()
    DetailRow("Actuator Type", info.actuator_type)
    DividerSpace()
    DetailRow("Broker URL", info.broker_url)
    DividerSpace()
    DetailRow("MQTT Topic", info.topic)
    DividerSpace()
    val scaleText = info.device_scale.joinToString("\n") { row ->
        row.joinToString(" = ")
    }
    DetailRow("Device Scale", scaleText)
    DividerSpace()
    DetailRow(
        "Adoption Status",
        if (info.adopted_status == 0) "Unadopted" else "Adopted",
    )
    DividerSpace()
    DetailRow("User UUID", info.user_uuid)
}