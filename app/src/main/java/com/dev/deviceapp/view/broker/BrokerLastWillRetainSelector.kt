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
fun BrokerLastWillRetainSelector(
    selectedOption: Boolean,
    onOptionSelected: (Boolean) -> Unit,
    lastWillRetainError: String?,
    onClearError: () -> Unit

){
    val lastWillRetainOptions = listOf(true, false)
    var expanded by remember { mutableStateOf(false) }
    var selectedLabel by remember { mutableStateOf(selectedOption) }

    Column(modifier = Modifier.fillMaxWidth()){
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedLabel.toString(),
                onValueChange = { },
                readOnly = true,
                label = { Text("Last Will Retain", color = Color(0xFFE0F2F1)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                isError = lastWillRetainError != null,
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
            ) {
                lastWillRetainOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.toString()) },
                        onClick = {
                            selectedLabel = option
                            onOptionSelected(option)
                            onClearError()
                            expanded = false
                        }
                    )
                }
            }
        }
        if (lastWillRetainError != null) {
            Text(
                text = lastWillRetainError,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}