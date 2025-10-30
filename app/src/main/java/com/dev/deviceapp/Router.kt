package com.dev.deviceapp

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dev.deviceapp.repository.login.TokenRepository
import com.dev.deviceapp.view.broker.BrokerCreateScreen
import com.dev.deviceapp.view.broker.BrokerDetailScreen
import com.dev.deviceapp.view.broker.BrokerGetFilterScreen
import com.dev.deviceapp.view.broker.BrokerTreeScreen
import com.dev.deviceapp.view.broker.BrokerUpdateScreen
import com.dev.deviceapp.view.device.DeviceCharacteristicReadInfoScreen
import com.dev.deviceapp.view.device.DeviceTreeScreen
import com.dev.deviceapp.view.device.DeviveBleScanListScreen
import com.dev.deviceapp.view.login.LoginScreen
import com.dev.deviceapp.view.mainscreen.MainScreen
import com.dev.deviceapp.view.profile.ProfileScreen
import com.dev.deviceapp.view.user.UserCreateScreen
import com.dev.deviceapp.view.user.UserDeleteScreen
import com.dev.deviceapp.view.user.UserUpdateScreen
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent



object AppDestinations {
    const val MAIN_SCREEN = "mainScreen"
    const val LOGIN_SCREEN = "loginScreen"
    const val USER_CREATE_SCREEN = "userCreateScreen"
    const val PROFILE_SCREEN = "registerScreen"
    const val USER_UPDATE_SCREEN = "userUpdateScreen"
    const val USER_DELETE_SCREEN = "userDeleteScreen"
    const val BROKER_TREE_SCREEN = "brokerTreeScreen"
    const val BROKER_CREATE_SCREEN = "brokerCreateScreen"
    const val BROKER_GET_FILTER_SCREEN = "brokerGetFilterScreen"
    const val BROKER_DETAIL_SCREEN = "brokerDetailScreen"
    const val BROKER_UPDATE_SCREEN = "brokerUpdateScreen"
    const val DEVICE_TREE_SCREEN = "deviceTreeScreen"
    const val DEVICE_BLE_SCAN_LIST_SCREEN = "deviceBleScanListScreen"
    const val DEVICE_OPTION_TREE_SCREEN = "deviceCharacteristicReadInfoScreen"
}


@EntryPoint
@InstallIn(SingletonComponent::class)
interface TokenRepositoryEntryPoint {
    fun tokenRepository(): TokenRepository
}

@RequiresApi(Build.VERSION_CODES.S)
@androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val tokenRepository = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            TokenRepositoryEntryPoint::class.java
        ).tokenRepository()
    }

    val startDestination = if (tokenRepository.getToken() != null) {
        Log.i("AppNavigation", "token: ${tokenRepository.getToken()}")
        AppDestinations.MAIN_SCREEN
    } else {
        Log.i("AppNavigation", "token: ${tokenRepository.getToken()}")
        AppDestinations.LOGIN_SCREEN
    }

    val onLogout: () -> Unit = {
        tokenRepository.clearToken()
        navController.navigate(AppDestinations.LOGIN_SCREEN) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = AppDestinations.MAIN_SCREEN) {
            MainScreen(navController = navController, onLogout = onLogout)
        }

        composable(route = AppDestinations.LOGIN_SCREEN) {
            LoginScreen(navController = navController)
        }

        composable(route = AppDestinations.PROFILE_SCREEN) {
            ProfileScreen(navController = navController, onLogout = onLogout)
        }

        composable(route = AppDestinations.USER_CREATE_SCREEN) {
            UserCreateScreen(navController = navController)
        }

        composable(route = AppDestinations.USER_UPDATE_SCREEN) {
            UserUpdateScreen(navController = navController)
        }

        composable(route = AppDestinations.USER_DELETE_SCREEN) {
            UserDeleteScreen(navController = navController)
        }

        composable(route = AppDestinations.BROKER_TREE_SCREEN) {
            BrokerTreeScreen(navController = navController)
        }

        composable(route = AppDestinations.BROKER_CREATE_SCREEN) {
            BrokerCreateScreen(navController = navController)
        }

        composable(route = AppDestinations.BROKER_GET_FILTER_SCREEN) {
            BrokerGetFilterScreen(navController = navController)
        }

        composable(route = AppDestinations.BROKER_DETAIL_SCREEN) {
            BrokerDetailScreen(navController = navController)
        }

        composable(route = AppDestinations.BROKER_UPDATE_SCREEN) {
            BrokerUpdateScreen(navController = navController)
        }

        composable(route = AppDestinations.DEVICE_TREE_SCREEN) {
            DeviceTreeScreen(navController = navController)
        }

        composable(route = AppDestinations.DEVICE_BLE_SCAN_LIST_SCREEN) {
            DeviveBleScanListScreen(
                navController = navController,
                onBack = { navController.popBackStack() },
                onDeviceClick = { bleDevice ->
                    navController.navigate(
                        "${AppDestinations.DEVICE_OPTION_TREE_SCREEN}/${bleDevice.address}"
                    )
                },
                onLogout = onLogout
            )
        }

        composable(
            route = "${AppDestinations.DEVICE_OPTION_TREE_SCREEN}/{deviceAddress}"
        ) { backStackEntry ->
            val deviceAddress = backStackEntry.arguments?.getString("deviceAddress")
            val context = LocalContext.current

            DeviceCharacteristicReadInfoScreen(
                navController = navController,
                deviceAddress = deviceAddress,
                context = context
            )
        }
    }
}
