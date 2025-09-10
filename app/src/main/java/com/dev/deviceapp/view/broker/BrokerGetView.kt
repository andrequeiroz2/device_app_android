package com.dev.deviceapp.view.broker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import com.dev.deviceapp.model.broker.BrokerGetRequest
import com.dev.deviceapp.viewmodel.broker.BrokerGetUiState
import com.dev.deviceapp.viewmodel.broker.BrokerGetViewModel

@Composable
fun BrokerGetView(
    navController: androidx.navigation.NavController,
    brokerViewModel: BrokerGetViewModel = hiltViewModel()
){
    val uiState by brokerViewModel.state.collectAsState()

    var uuid by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf(0) }
    var connected by remember { mutableStateOf(true) }

    val params = BrokerGetRequest(
        uuid,
        host,
        port,
        connected
    )

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)){

        Spacer(modifier = Modifier.height(20.dp))

        TextField(
            value = uuid,
            onValueChange = { uuid = it },
            label = { Text("UUID") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

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
            value = connected.toString(),
            onValueChange = { connected = it.toBoolean() },
            label = { Text("Connected") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                brokerViewModel.getBroker(params)
            },
            modifier = Modifier.fillMaxWidth(),
        ){
            Text(text = "Get Broker")
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
            is BrokerGetUiState.Loading -> CircularProgressIndicator()
            is BrokerGetUiState.Success -> Text(
                (
                        uiState as BrokerGetUiState.Success).message)
            is BrokerGetUiState.Error -> Text(
                (
                        uiState as BrokerGetUiState.Error).message)
            else -> {}
        }
    }
}