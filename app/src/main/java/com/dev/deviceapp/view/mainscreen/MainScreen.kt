package com.dev.deviceapp.view.mainscreen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.dev.deviceapp.model.device.DeviceGetOwnedUserFilterRequest
import com.dev.deviceapp.repository.device.DeviceGetOwnedUserRepository
import com.dev.deviceapp.viewmodel.profile.ProfileViewModel
import com.dev.deviceapp.viewmodel.user.UserGetUiState
import com.dev.deviceapp.viewmodel.user.UserGetViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
    userGetViewModel: UserGetViewModel = hiltViewModel(),
    onLogout: () -> Unit = {}
) {

    val tokenInfo = viewModel.tokenInfo
    if (tokenInfo == null) {
        LaunchedEffect(Unit) { onLogout() }
        return
    }

    val context = LocalContext.current
    
    // Acessar DeviceDao e Repository
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
    
    // Carregar dispositivos apenas se não existirem no banco
    LaunchedEffect(Unit) {
        try {
            // Verificar se já existem dispositivos no banco
            val existingDevices = deviceDao.getAllDevices()
            
            if (existingDevices.isEmpty()) {
                // Banco vazio, fazer chamada à API
                Log.i("MainScreen", "No devices in cache, loading from API...")
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
            } else {
                // Já existem dispositivos no banco, não precisa chamar API
                Log.i("MainScreen", "Devices already in cache (${existingDevices.size} devices), skipping API call")
            }
        } catch (e: Exception) {
            Log.e("MainScreen", "Error checking/loading devices: ${e.message}", e)
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
                    label = { Text("Broker", color = Color.White) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(AppDestinations.BROKER_TREE_SCREEN)
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                NavigationDrawerItem(
                    label = { Text("Device", color = Color.White) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(AppDestinations.DEVICE_TREE_SCREEN)
                    }
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
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { expanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Profile",
                                    tint = Color.White
                                )
                            }
                            DropdownMenu(
                                expanded = expanded,
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Bem-vindo!",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )
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