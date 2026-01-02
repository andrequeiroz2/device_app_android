package com.dev.deviceapp.view.broker

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.dev.deviceapp.AppDestinations
import com.dev.deviceapp.model.broker.BrokerSuccess
import com.dev.deviceapp.viewmodel.broker.BrokerDeleteUiState
import com.dev.deviceapp.viewmodel.broker.BrokerDeleteViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrokerDetailScreen(
    navController: NavController,
    brokerDeleteViewModel: BrokerDeleteViewModel = hiltViewModel()
) {

    val brokerDeleteUiState by brokerDeleteViewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(brokerDeleteUiState) {
        when (brokerDeleteUiState) {
            is BrokerDeleteUiState.Error -> {
                Toast.makeText(
                    context,
                    (brokerDeleteUiState as BrokerDeleteUiState.Error).message,
                    Toast.LENGTH_LONG
                ).show()
            }
            is BrokerDeleteUiState.Success -> {
                Toast.makeText(
                    context,
                    (brokerDeleteUiState as BrokerDeleteUiState.Success).message,
                    Toast.LENGTH_LONG
                ).show()
                navController.navigate(AppDestinations.MAIN_SCREEN)
            }
            else -> {}
        }
    }

    val broker = remember {
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.get<BrokerSuccess>("brokerItem")
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF121212)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Broker Detail",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFF00A86B)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            broker?.let { item ->

                val version = when (item.version) {
                    0 -> "Default"
                    3 -> "v3_1"
                    4 -> "v3_1_1"
                    5 -> "v5"
                    else -> "Default"
                }


                DetailRow(label = "UUID", value = item.uuid)
                Spacer(modifier = Modifier.height(16.dp))
                DetailRow(label = "Host", value = item.host)
                Spacer(modifier = Modifier.height(16.dp))
                DetailRow(label = "Port", value = item.port.toString())
                Spacer(modifier = Modifier.height(16.dp))
                DetailRow(label = "Client ID", value = item.clientId)
                Spacer(modifier = Modifier.height(16.dp))
                DetailRow(label = "Version", value = version)
                Spacer(modifier = Modifier.height(16.dp))
                DetailRow(label = "Keep Alive", value = item.keepAlive.toString())
                Spacer(modifier = Modifier.height(16.dp))
                DetailRow(label = "Clean Session", value = item.cleanSession.toString())
                Spacer(modifier = Modifier.height(16.dp))
                DetailRow(label = "Last Will Topic", value = item.lastWillTopic)
                Spacer(modifier = Modifier.height(16.dp))
                DetailRow(label = "Last Will Message", value = item.lastWillMessage)
                Spacer(modifier = Modifier.height(16.dp))
                DetailRow(label = "Last Will Qos", value = item.lastWillQos.toString())
                Spacer(modifier = Modifier.height(16.dp))
                DetailRow(label = "Last Will Retain", value = item.lastWillRetain.toString())
                Spacer(modifier = Modifier.height(16.dp))
                DetailRow(
                    label = "Connected",
                    value = item.connected.toString(),
                    valueColor = if (item.connected) Color(0xFF00A86B) else Color.Red
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        broker.let {
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("brokerItem", it)

                            navController.navigate(AppDestinations.BROKER_UPDATE_SCREEN)
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
                    Text("Edit")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        broker.let {
                            brokerDeleteViewModel.deleteBroker(broker.uuid)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD2507D),
                        contentColor = Color.White
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Delete")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { navController.popBackStack() },
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
            } ?: Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Broker not found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Red
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, valueColor: Color = Color.White) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF00A86B)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor
        )
    }
}