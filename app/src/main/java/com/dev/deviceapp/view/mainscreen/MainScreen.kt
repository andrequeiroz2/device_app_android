package com.dev.deviceapp.view.mainscreen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import android.util.Log
import com.dev.deviceapp.AppDestinations
import com.dev.deviceapp.database.device.DeviceDao
import com.dev.deviceapp.model.broker.BrokerGetFilterRequest
import com.dev.deviceapp.model.device.DeviceGetOwnedUserFilterRequest
import com.dev.deviceapp.mqtt.MqttConnectionState
import com.dev.deviceapp.repository.broker.BrokerGetFilterRepository
import com.dev.deviceapp.repository.device.DeviceGetOwnedUserRepository
import com.dev.deviceapp.repository.mqtt.MqttRepository
import com.dev.deviceapp.database.device.DeviceEntity
import com.dev.deviceapp.viewmodel.device.DeviceOwnedViewModel
import com.dev.deviceapp.viewmodel.profile.ProfileViewModel
import com.dev.deviceapp.viewmodel.user.UserGetUiState
import com.dev.deviceapp.viewmodel.user.UserGetViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

@Composable
fun DeviceCard(
    device: DeviceEntity,
    onClick: () -> Unit
) {
    // Extrair últimas mensagens
    val latestMessages = remember(device.messages) {
        device.messages?.flatMap { messageEntity ->
            messageEntity.messages?.map { (key, received) ->
                "$key: ${received.value}${received.scale}"
            } ?: emptyList()
        } ?: emptyList()
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = device.name,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            
            if (latestMessages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                latestMessages.take(3).forEach { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF00A86B),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DeviceGetOwnedUserRepositoryEntryPoint {
    fun deviceGetOwnedUserRepository(): DeviceGetOwnedUserRepository
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DeviceDaoEntryPoint {
    fun deviceDao(): DeviceDao
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BrokerGetFilterRepositoryEntryPoint {
    fun brokerGetFilterRepository(): BrokerGetFilterRepository
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MqttRepositoryEntryPoint {
    fun mqttRepository(): MqttRepository
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
    userGetViewModel: UserGetViewModel = hiltViewModel(),
    deviceOwnedViewModel: DeviceOwnedViewModel = hiltViewModel(),
    onLogout: () -> Unit = {}
) {

    val tokenInfo = viewModel.tokenInfo
    if (tokenInfo == null) {
        LaunchedEffect(Unit) { onLogout() }
        return
    }

    val context = LocalContext.current
    
    // State loading controller spinner
    var isLoadingDevices by remember { mutableStateOf(false) }
    
    // Access DeviceDao and Repository
    val deviceDao = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            DeviceDaoEntryPoint::class.java
        ).deviceDao()
    }
    
    val deviceRepository = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            DeviceGetOwnedUserRepositoryEntryPoint::class.java
        ).deviceGetOwnedUserRepository()
    }
    
    val brokerRepository = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            BrokerGetFilterRepositoryEntryPoint::class.java
        ).brokerGetFilterRepository()
    }
    
    val mqttRepository = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            MqttRepositoryEntryPoint::class.java
        ).mqttRepository()
    }
    
    // Observar estado da conexão MQTT e logar mudanças
    val mqttConnectionState by mqttRepository.connectionState.collectAsState()
    
    LaunchedEffect(mqttConnectionState) {
        when (mqttConnectionState) {
            is MqttConnectionState.Disconnected -> {
                Log.i("MainScreen", "MQTT State: DISCONNECTED")
            }
            is MqttConnectionState.Connecting -> {
                Log.i("MainScreen", "MQTT State: CONNECTING...")
            }
            is MqttConnectionState.Connected -> {
                Log.i("MainScreen", "MQTT State: CONNECTED ✓")
            }
            is MqttConnectionState.Error -> {
                Log.e("MainScreen", "MQTT State: ERROR - ${(mqttConnectionState as MqttConnectionState.Error).message}")
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            // Verify exists devices on DataBase
            val existingDevices = deviceDao.getAllDevices()
            
            if (existingDevices.isEmpty()) {
                // get device owned user
                Log.i("MainScreen", "No devices in cache, loading from API...")
                isLoadingDevices = true
                
                val response = deviceRepository.getDeviceOwnedUser(
                    DeviceGetOwnedUserFilterRequest()
                )
                
                when (response) {
                    is com.dev.deviceapp.model.device.DeviceGetOwnedUserResponse.Success -> {
                        Log.i("MainScreen", "Devices loaded successfully: ${response.devices.size} devices")
                    }
                    is com.dev.deviceapp.model.device.DeviceGetOwnedUserResponse.Error -> {
                        Log.w("MainScreen", "Error loading devices: ${response.errorMessage}")
                    }
                }
                
                isLoadingDevices = false
            } else {
                // Devices already exist in the database; no need to call the API.
                Log.i("MainScreen", "Devices already in cache (${existingDevices.size} devices), skipping API call")
            }
            
            // Buscar e salvar broker conectado
            try {
                Log.i("MainScreen", "=== Loading connected broker ===")
                val brokerResponse = brokerRepository.getBroker(
                    BrokerGetFilterRequest(connected = true)
                )
                when (brokerResponse) {
                    is com.dev.deviceapp.model.broker.BrokerGetFilterResponse.Success -> {
                        if (brokerResponse.brokers.isNotEmpty()) {
                            val broker = brokerResponse.brokers.first()
                            Log.i("MainScreen", "Connected broker found and saved:")
                            Log.i("MainScreen", "  - UUID: ${broker.uuid}")
                            Log.i("MainScreen", "  - Host: ${broker.host}")
                            Log.i("MainScreen", "  - Port: ${broker.port}")
                            Log.i("MainScreen", "  - Client ID: ${broker.clientId}")
                            Log.i("MainScreen", "  - Clean Session: ${broker.cleanSession}")
                            Log.i("MainScreen", "  - Keep Alive: ${broker.keepAlive}")
                            
                            // Conectar ao broker MQTT usando o broker recebido da API
                            // (o broker já foi salvo no banco pelo repository)
                            Log.i("MainScreen", "=== Attempting MQTT connection ===")
                            Log.i("MainScreen", "Server URI: tcp://${broker.host}:${broker.port}")
                            Log.i("MainScreen", "Client ID: ${broker.clientId}")
                            
                            // Converter o broker da API para BrokerEntity para conexão
                            val brokerEntity = com.dev.deviceapp.database.broker.BrokerEntity(
                                uuid = broker.uuid,
                                host = broker.host,
                                port = broker.port,
                                clientId = broker.clientId,
                                version = broker.version,
                                versionText = broker.versionText,
                                keepAlive = broker.keepAlive,
                                cleanSession = broker.cleanSession,
                                lastWillTopic = broker.lastWillTopic,
                                lastWillMessage = broker.lastWillMessage,
                                lastWillQos = broker.lastWillQos,
                                lastWillRetain = broker.lastWillRetain,
                                connected = broker.connected,
                                createdAt = broker.createdAt,
                                updatedAt = broker.updatedAt
                            )
                            
                            val connectionStarted = mqttRepository.connect(brokerEntity)
                            if (connectionStarted) {
                                Log.i("MainScreen", "✓ MQTT connection attempt started successfully")
                            } else {
                                Log.w("MainScreen", "✗ Failed to start MQTT connection")
                            }
                        } else {
                            Log.i("MainScreen", "No connected broker found")
                        }
                    }
                    is com.dev.deviceapp.model.broker.BrokerGetFilterResponse.Error -> {
                        Log.w("MainScreen", "Error loading broker: ${brokerResponse.errorMessage}")
                    }
                }
            } catch (e: Exception) {
                Log.e("MainScreen", "Error loading broker: ${e.message}", e)
            }
        } catch (e: Exception) {
            Log.e("MainScreen", "Error checking/loading devices: ${e.message}", e)
            isLoadingDevices = false
        }
    }

    val uiStateUserGet by userGetViewModel.state.collectAsState()
    LaunchedEffect(uiStateUserGet) {
        when (uiStateUserGet) {
            is UserGetUiState.Error -> {
                Toast.makeText(
                    context,
                    (uiStateUserGet as UserGetUiState.Error).message,
                    Toast.LENGTH_SHORT
                ).show()
            }
            is UserGetUiState.Success -> {
                navController.navigate(AppDestinations.PROFILE_SCREEN)
            }
            else -> {}
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Observar dispositivos do banco
    val devices by deviceOwnedViewModel.devices.collectAsState()
    
    // Recarregar dispositivos quando terminar o loading
    LaunchedEffect(isLoadingDevices) {
        if (!isLoadingDevices) {
            deviceOwnedViewModel.loadDevices()
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
                    label = { 
                        Text(
                            "Broker", 
                            color = if (isLoadingDevices) Color.Gray else Color.White
                        ) 
                    },
                    selected = false,
                    onClick = {
                        if (!isLoadingDevices) {
                            scope.launch { drawerState.close() }
                            navController.navigate(AppDestinations.BROKER_TREE_SCREEN)
                        }
                    },
                    modifier = Modifier.alpha(if (isLoadingDevices) 0.5f else 1f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                NavigationDrawerItem(
                    label = { 
                        Text(
                            "Device", 
                            color = if (isLoadingDevices) Color.Gray else Color.White
                        ) 
                    },
                    selected = false,
                    onClick = {
                        if (!isLoadingDevices) {
                            scope.launch { drawerState.close() }
                            navController.navigate(AppDestinations.DEVICE_TREE_SCREEN)
                        }
                    },
                    modifier = Modifier.alpha(if (isLoadingDevices) 0.5f else 1f)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("Device App", color = Color.White)
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch { drawerState.open() }
                            },
                            enabled = !isLoadingDevices
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(
                                onClick = { expanded = true },
                                enabled = !isLoadingDevices
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Profile",
                                    tint = Color.White
                                )
                            }
                            DropdownMenu(
                                expanded = expanded && !isLoadingDevices,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Profile") },
                                    onClick = {
                                        expanded = false
                                        navController.navigate(AppDestinations.PROFILE_SCREEN)
                                    }

                                )
                                DropdownMenuItem(
                                    text = { Text("Logout") },
                                    onClick = {
                                        expanded = false
                                        onLogout()
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF121212)
                    )
                )
            },
            containerColor = Color(0xFF121212)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .then(
                        if (isLoadingDevices) {
                            Modifier.alpha(0.5f)
                        } else {
                            Modifier
                        }
                    )
            ) {
                if (devices.isEmpty() && !isLoadingDevices) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum dispositivo encontrado",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(devices, key = { it.uuid }) { device ->
                            DeviceCard(
                                device = device,
                                onClick = {
                                    navController.currentBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("deviceUuid", device.uuid)
                                    navController.navigate(AppDestinations.DEVICE_OWNED_DETAIL_SCREEN)
                                }
                            )
                        }
                    }
                }
            }
        }

        if (isLoadingDevices) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF00A86B))
            }
        }

        if (uiStateUserGet is UserGetUiState.Loading){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF00A86B))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
    val navController = rememberNavController()
    MaterialTheme {
        MainScreen(navController = navController)
    }
}