package com.dev.deviceapp.view.broker

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.dev.deviceapp.AppDestinations
import com.dev.deviceapp.model.broker.BrokerResponse
import com.dev.deviceapp.viewmodel.broker.BrokerPaginateViewModel
import kotlinx.coroutines.launch
import androidx.paging.*
import com.dev.deviceapp.model.broker.BrokerSuccess
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrokerGetFilterScreen(
    navController: NavController,
    brokerViewModel: BrokerPaginateViewModel = hiltViewModel()
){
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showFilter by remember { mutableStateOf(false) }

    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("") }
    var connected by remember { mutableStateOf<Boolean?>(null) }

    val brokers = brokerViewModel.getBrokers(
        host = host.ifBlank { null },
        port = port.toIntOrNull(),
        connected = connected
    ).collectAsLazyPagingItems()

    val context = LocalContext.current

    LaunchedEffect(brokers) {
        snapshotFlow { brokers.loadState.refresh }
            .collect { loadState ->
                if (loadState is LoadState.Error) {
                    Log.e("BrokerGetFilterScreen", "Error: ${loadState.error.message}")
                    val message = loadState.error.localizedMessage ?: "Application Error"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                    if(message.contains("Unauthorized", ignoreCase = true)) {
                        Log.e("BrokerGetFilterScreen", "Unauthorized")
                        navController.navigate(AppDestinations.LOGIN_SCREEN)
                    }
                }
            }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF1E1E1E)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Menu",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF00A86B)
                )

                HorizontalDivider(Modifier, DividerDefaults.Thickness, color = Color.Gray)

                NavigationDrawerItem(
                    label = { Text("Home", color = Color.White) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(AppDestinations.MAIN_SCREEN)
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("Brokers", color = Color.White)
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showFilter = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF121212)
                    )
                )
            },
            containerColor = Color(0xFF121212)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                BrokerList(brokers, navController)
            }
        }
    }

    // Modal Filters
    if (showFilter) {
        ModalBottomSheet(
            onDismissRequest = { showFilter = false },
            containerColor = Color(0xFF1E1E1E)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Filters", color = Color.White, style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text("Host") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = Color.White)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it },
                    label = { Text("Port") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { connected = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A86B))
                    ) {
                        Text("Connected")
                    }
                    Button(
                        onClick = { connected = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("Disconnected")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        showFilter = false
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A86B))
                ) {
                    Text("Back")
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun BrokerList(
    brokers: LazyPagingItems<BrokerResponse.Success>,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(brokers.itemCount) { index ->
            val broker = brokers[index]
            broker?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable{
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("brokerItem", BrokerSuccess(
                                    it.uuid,
                                    it.host,
                                    it.port,
                                    it.clientId,
                                    it.version,
                                    it.versionText,
                                    it.keepAlive,
                                    it.cleanSession
                                    ,it.lastWillTopic,
                                    it.lastWillMessage,
                                    it.lastWillQos,
                                    it.lastWillRetain,
                                    it.connected,
                                    it.createdAt,
                                    it.updatedAt
                                ))
                            navController.navigate(AppDestinations.BROKER_DETAIL_SCREEN)
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("UUID: ${it.uuid}", color = Color.White)
                        Text("Host: ${it.host}", color = Color.White)
                        Text("Port: ${it.port}", color = Color.White)
                        Text(
                            "Connected: ${it.connected}",
                            color = if (it.connected) Color(0xFF00A86B) else Color.Red
                        )
                    }
                }
            }
        }
    }
}


//@Composable
//fun BrokerList(brokers: LazyPagingItems<BrokerResponse.Success>) {
//    LazyColumn(
//        modifier = Modifier.fillMaxSize(),
//        contentPadding = PaddingValues(16.dp),
//        verticalArrangement = Arrangement.spacedBy(12.dp)
//    ) {
//        items(brokers.itemCount) { index ->
//            val broker = brokers[index]
//            broker?.let {
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
//                ) {
//                    Column(modifier = Modifier.padding(16.dp)) {
//                        Text("UUID: ${it.uuid}", color = Color.White)
//                        Text("Host: ${it.host}", color = Color.White)
//                        Text("Port: ${it.port}", color = Color.White)
//                        Text(
//                            "Connected: ${it.connected}",
//                            color = if (it.connected) Color(0xFF00A86B) else Color.Red
//                        )
//                    }
//                }
//            }
//        }
//    }
//}

