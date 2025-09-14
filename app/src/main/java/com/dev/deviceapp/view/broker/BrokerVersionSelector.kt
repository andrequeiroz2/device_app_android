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
fun BrokerVersionSelector(
    selectedVersion: Int,
    onVersionSelected: (Int) -> Unit,
    versionError: String?,
    onClearError: () -> Unit
) {
    val versions = mapOf(
        "default" to 0,
        "v3_1" to 3,
        "v3_1_1" to 4,
        "v5" to 5
    )

    var expanded by remember { mutableStateOf(false) }
    var selectedLabel by remember {
        mutableStateOf(
            versions.entries.find { it.value == selectedVersion }?.key ?: "default"
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedLabel,
                onValueChange = { },
                readOnly = true,
                label = { Text("Version", color = Color(0xFFE0F2F1)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(), // ✅ ainda funciona em versões estáveis
                isError = versionError != null,
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
                modifier = Modifier.exposedDropdownSize() // ✅ substitui menuAnchor no Dropdown
            ) {
                versions.forEach { (label, value) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            selectedLabel = label
                            onVersionSelected(value) // agora Int: 0, 3, 4, 5
                            onClearError()
                            expanded = false
                        }
                    )
                }
            }
        }

        if (versionError != null) {
            Text(
                text = versionError,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}