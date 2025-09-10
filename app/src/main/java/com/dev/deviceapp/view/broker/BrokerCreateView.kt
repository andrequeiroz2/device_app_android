package com.dev.deviceapp.view.broker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dev.deviceapp.model.broker.BrokerCreateRequest
import com.dev.deviceapp.viewmodel.broker.BrokerCreateUiState
import com.dev.deviceapp.viewmodel.broker.BrokerCreateViewModel

@Composable
fun BrokerCreateView(
    navController: androidx.navigation.NavController,
    brokerViewModel: BrokerCreateViewModel = hiltViewModel()
){

    val uiState by brokerViewModel.state.collectAsState()

    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf(0) }
    var client_id by remember { mutableStateOf("") }
    var version by remember { mutableStateOf(0) }
    var keep_alive by remember { mutableStateOf(0) }
    var clean_session by remember { mutableStateOf(true) }
    var last_will_topic by remember { mutableStateOf("") }
    var last_will_message by remember { mutableStateOf("") }
    var last_will_qos by remember { mutableStateOf(0) }
    var last_will_retain by remember { mutableStateOf(true) }

    val broker = BrokerCreateRequest(
        host,
        port,
        client_id,
        version,
        keep_alive,
        clean_session,
        last_will_topic,
        last_will_message,
        last_will_qos,
        last_will_retain
    )

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)){

        Spacer(modifier = Modifier.height(20.dp))

        TextField(
            value = host,
            onValueChange = { host = it },
            label = { Text("Host") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = port.toString(),
            onValueChange = { port = it.toInt() },
            label = { Text("Port") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = client_id,
            onValueChange = { client_id = it },
            label = { Text("Client ID") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = version.toString(),
            onValueChange = { version = it.toInt() },
            label = { Text("Version") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = keep_alive.toString(),
            onValueChange = { keep_alive = it.toInt() },
            label = { Text("Keep Alive") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = clean_session.toString(),
            onValueChange = { clean_session = it.toBoolean() },
            label = { Text("Clean Session") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = last_will_topic,
            onValueChange = { last_will_topic = it },
            label = { Text("Last Will Topic") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = last_will_message,
            onValueChange = { last_will_message = it },
            label = { Text("Last Will Message") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = last_will_qos.toString(),
            onValueChange = { last_will_qos = it.toInt() },
            label = { Text("Last Will Qos") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = last_will_retain.toString(),
            onValueChange = { last_will_retain = it.toBoolean() },
            label = { Text("Last Will Retain") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                brokerViewModel.createBroker(broker)
            },
            modifier = Modifier.fillMaxWidth()
        ){
            Text("Create Broker")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ){
            Text("Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when(uiState){
            is BrokerCreateUiState.Loading -> CircularProgressIndicator()
            is BrokerCreateUiState.Success -> Text(
                (
                        uiState as BrokerCreateUiState.Success).message, color = MaterialTheme.colorScheme.primary
            )
            is BrokerCreateUiState.Error -> Text(
                (
                        uiState as BrokerCreateUiState.Error).message, color = MaterialTheme.colorScheme.error
            )
            else -> {}
        }
    }
}