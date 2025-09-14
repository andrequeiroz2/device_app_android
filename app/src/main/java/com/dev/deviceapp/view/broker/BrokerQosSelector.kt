package com.dev.deviceapp.view.broker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrokerQosSelector(
    selectedQos: Int,
    onQosSelected: (Int) -> Unit,
    qosError: String?,
    onClearError: () -> Unit
){

    val qosOptions = listOf(0, 1, 2)
    var expanded by remember { mutableStateOf(false) }
    var selectedLabel by remember { mutableStateOf(selectedQos.toString()) }


    Column(modifier = Modifier.fillMaxWidth()){
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ){
            OutlinedTextField(
                value = selectedLabel,
                onValueChange = { },
                readOnly = true,
                label = { Text("Qos", color = Color(0xFFE0F2F1)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                isError = qosError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF00A86B),
                    unfocusedBorderColor = Color(0xFF00693E)
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.exposedDropdownSize()
            ){
                qosOptions.forEach { qos ->
                    DropdownMenuItem(
                        text = { Text(qos.toString()) },
                        onClick = {
                            selectedLabel = qos.toString()
                            onQosSelected(qos)
                            onClearError()
                            expanded = false
                        }
                    )
                }
            }
        }
        if (qosError != null) {
            Text(
                text = qosError,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}