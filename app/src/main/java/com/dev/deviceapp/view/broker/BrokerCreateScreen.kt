package com.dev.deviceapp.view.broker

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dev.deviceapp.AppDestinations
import com.dev.deviceapp.model.broker.BrokerCreateRequest
import com.dev.deviceapp.viewmodel.broker.BrokerCreateUiState
import com.dev.deviceapp.viewmodel.broker.BrokerCreateViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrokerCreateScreen(
    navController: NavController,
    brokerViewModel: BrokerCreateViewModel = hiltViewModel()
){

    val uiState by brokerViewModel.state.collectAsState()
    val context = LocalContext.current

    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("") }
    var clientId by remember { mutableStateOf("") }
    var version by remember { mutableStateOf(0) }
    var keepAlive by remember { mutableStateOf(60) }
    var cleanSession by remember { mutableStateOf(true) }
    var lastWillTopic by remember { mutableStateOf("") }
    var lastWillMessage by remember { mutableStateOf("") }
    var lastWillQos by remember { mutableStateOf(0) }
    var lastWillRetain by remember { mutableStateOf(true) }

    var hostError by remember { mutableStateOf<String?>(null) }
    var portError by remember { mutableStateOf<String?>(null) }
    var clientIdError by remember { mutableStateOf<String?>(null) }
    var versionError by remember { mutableStateOf<String?>(null) }
    var keepAliveError by remember { mutableStateOf<String?>(null) }
    var cleanSessionError by remember { mutableStateOf<String?>(null) }
    var lastWillTopicError by remember { mutableStateOf<String?>(null) }
    var lastWillMessageError by remember { mutableStateOf<String?>(null) }
    var lastWillQosError by remember { mutableStateOf<String?>(null) }
    var lastWillRetainError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is BrokerCreateUiState.Error -> {
                Toast.makeText(
                    context,
                    (uiState as BrokerCreateUiState.Error).message,
                    Toast.LENGTH_LONG
                ).show()
            }
            is BrokerCreateUiState.Success -> {
                Toast.makeText(
                    context,
                    "Broker created successfully",
                    Toast.LENGTH_LONG
                ).show()
                navController.navigate(AppDestinations.MAIN_SCREEN)
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()){
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF121212)
        ){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(
                    text = "Create Broker",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFF00A86B)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = host,
                    onValueChange = {
                        host = it
                        if (it.isNotBlank()) hostError = null
                    },
                    label = { Text("Host", color = Color(0xFFE0F2F1)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    isError = hostError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00A86B),
                        unfocusedBorderColor = Color(0xFF00693E)
                    )
                )
                if (hostError != null) {
                    Text(
                        text = hostError ?: "",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                            .padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = port,
                    onValueChange = {
                        if(it.all {char -> char.isDigit()}){
                            port = it
                            if (it.isNotBlank()) portError = null
                        }
                    },
                    label = { Text("Port", color = Color(0xFFE0F2F1)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = portError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00A86B),
                        unfocusedBorderColor = Color(0xFF00693E)
                    )
                )
                if (portError != null) {
                    Text(
                        text = portError ?: "",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                            .padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = clientId,
                    onValueChange = {
                        clientId = it
                        if (it.isNotBlank()) clientIdError = null
                    },
                    label = { Text("Client ID", color = Color(0xFFE0F2F1)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    isError = clientIdError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00A86B),
                        unfocusedBorderColor = Color(0xFF00693E)
                    )
                )
                if (clientIdError != null) {
                    Text(
                        text = clientIdError ?: "",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                            .padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                BrokerVersionSelector(
                    selectedVersion = version,
                    onVersionSelected = { version = it },
                    versionError = versionError,
                    onClearError = { versionError = null }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = keepAlive.toString(),
                    onValueChange = {
                        keepAlive = it.toIntOrNull() ?: 0
                        if (it.isNotBlank()) keepAliveError = null
                    },
                    label = { Text("Keep Alive", color = Color(0xFFE0F2F1))},
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = keepAliveError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00A86B),
                        unfocusedBorderColor = Color(0xFF00693E)
                    )
                )
                if (keepAliveError != null) {
                    Text(
                        text = keepAliveError ?: "",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                            .padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                BrokerCleanSessionSelector(
                    selectedOption = cleanSession,
                    onOptionSelected = { cleanSession = it },
                    cleanSessionError = cleanSessionError,
                    onClearError = { cleanSessionError = null }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = lastWillTopic,
                    onValueChange = {
                        lastWillTopic = it
                        if (it.isNotBlank()) lastWillTopicError = null
                    },
                    label = { Text("Last Will Topic", color = Color(0xFFE0F2F1))},
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    isError = lastWillTopicError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00A86B),
                        unfocusedBorderColor = Color(0xFF00693E)
                    )
                )
                if (lastWillTopicError != null) {
                    Text(
                        text = lastWillTopicError ?: "",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                            .padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = lastWillMessage,
                    onValueChange = {
                        lastWillMessage = it
                        if (it.isNotBlank()) lastWillMessageError = null
                    },
                    label = { Text("Last Will Message", color = Color(0xFFE0F2F1))},
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    isError = lastWillMessageError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00A86B),
                        unfocusedBorderColor = Color(0xFF00693E)
                    )
                )
                if (lastWillMessageError != null) {
                    Text(
                        text = lastWillMessageError ?: "",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                            .padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                BrokerQosSelector(
                    selectedQos = lastWillQos,
                    onQosSelected = { lastWillQos = it },
                    qosError = lastWillQosError,
                    onClearError = { lastWillQosError = null }
                )

                Spacer(modifier = Modifier.height(16.dp))

                BrokerLastWillRetainSelector(
                    selectedOption = lastWillRetain,
                    onOptionSelected = { lastWillRetain = it },
                    lastWillRetainError = lastWillRetainError,
                    onClearError = { lastWillRetainError = null }
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        var hasError = false

                        if (host.isBlank()) {
                            hostError = "Host is required"
                            hasError = true
                        }
                        if (port.isBlank()) {
                            portError = "Port is required"
                            hasError = true
                        }
                        if (clientId.isBlank()) {
                            clientIdError = "Client ID is required"
                            hasError = true
                        }
                        if (lastWillTopic.isBlank()) {
                            lastWillTopicError = "Last Will Topic is required"
                            hasError = true
                        }
                        if (lastWillMessage.isBlank()) {
                            lastWillMessageError = "Last Will Message is required"
                            hasError = true
                        }

                        if (!hasError) {
                            brokerViewModel.createBroker(
                                BrokerCreateRequest(
                                    host,
                                    port.toInt(),
                                    clientId,
                                    version,
                                    keepAlive,
                                    cleanSession,
                                    lastWillTopic,
                                    lastWillMessage,
                                    lastWillQos,
                                    lastWillRetain
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00A86B),
                        contentColor = Color.White
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Create")
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        navController.popBackStack()
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00A86B),
                        contentColor = Color.White
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Back")
                }
            }
        }
        if (uiState is BrokerCreateUiState.Loading){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ){
                CircularProgressIndicator(color = Color(0xFF00A86B))
            }
        }
    }
}