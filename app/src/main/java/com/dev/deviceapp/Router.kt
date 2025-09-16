package com.dev.deviceapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dev.deviceapp.repository.login.TokenRepository
import com.dev.deviceapp.view.broker.BrokerCreateScreen
import com.dev.deviceapp.view.broker.BrokerDetailScreen
import com.dev.deviceapp.view.broker.BrokerGetFilterScreen
import com.dev.deviceapp.view.broker.BrokerTreeScreen
import com.dev.deviceapp.view.broker.BrokerUpdateScreen
import com.dev.deviceapp.view.login.LoginScreen
import com.dev.deviceapp.view.mainscreen.MainScreen
import com.dev.deviceapp.view.profile.ProfileScreen
import com.dev.deviceapp.view.user.UserCreateScreen
import com.dev.deviceapp.view.user.UserDeleteScreen
import com.dev.deviceapp.view.user.UserUpdateScreen
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.EntryPointAccessors


object AppDestinations{
    const val MAIN_SCREEN = "mainScreen"

    //Login
    const val LOGIN_SCREEN = "loginScreen"

    //User Crud
    const val USER_CREATE_SCREEN = "userCreateScreen"
    const val PROFILE_SCREEN = "registerScreen"
    const val USER_UPDATE_SCREEN = "userUpdateScreen"
    const val USER_DELETE_SCREEN = "userDeleteScreen"

    //Broker Crud
    const val BROKER_TREE_SCREEN = "brokerTreeScreen"
    const val BROKER_CREATE_SCREEN = "brokerCreateScreen"
    const val BROKER_GET_FILTER_SCREEN = "brokerGetFilterScreen"
    const val BROKER_DETAIL_SCREEN = "brokerDetailScreen"
    const val BROKER_UPDATE_SCREEN = "brokerUpdateScreen"

}


@EntryPoint
@InstallIn(SingletonComponent::class)
interface TokenRepositoryEntryPoint {
    fun tokenRepository(): TokenRepository
}

@Composable
fun AppNavigation(){

    val context = LocalContext.current

    val tokenRepository = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            TokenRepositoryEntryPoint::class.java
        ).tokenRepository()
    }

    val navController = rememberNavController()

    val startDestination = if (tokenRepository.getToken() != null) {
        AppDestinations.MAIN_SCREEN
    } else {
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
    ){
        composable(route = AppDestinations.MAIN_SCREEN){
            MainScreen(navController = navController, onLogout = onLogout)
        }

        composable(route = AppDestinations.LOGIN_SCREEN){
            LoginScreen(navController = navController)
        }

        composable(route = AppDestinations.PROFILE_SCREEN){
            ProfileScreen(navController = navController, onLogout = onLogout)
        }

        composable(route = AppDestinations.USER_CREATE_SCREEN){
            UserCreateScreen(navController = navController)
        }

        composable(route = AppDestinations.USER_UPDATE_SCREEN){
            UserUpdateScreen(navController = navController)
        }

        composable(route = AppDestinations.USER_DELETE_SCREEN){
            UserDeleteScreen(navController = navController)
        }

        composable(route = AppDestinations.BROKER_TREE_SCREEN){
            BrokerTreeScreen(navController = navController)
        }

        composable(route = AppDestinations.BROKER_CREATE_SCREEN){
            BrokerCreateScreen(navController = navController)
        }

        composable(route = AppDestinations.BROKER_GET_FILTER_SCREEN){
            BrokerGetFilterScreen(navController = navController)
        }

        composable(route = AppDestinations.BROKER_DETAIL_SCREEN){
            BrokerDetailScreen(navController = navController)
        }

        composable(route = AppDestinations.BROKER_UPDATE_SCREEN){
            BrokerUpdateScreen(navController = navController)
        }
    }
}
