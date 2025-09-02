package com.dev.deviceapp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dev.deviceapp.view.user.CreateUserView

object AppDestinations{
    const val MAIN_SCREEN = "mainScreen"
    const val USER_CREATE_SCREEN = "userCreateScreen"
}

@Composable
fun AppNavigation(){
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.MAIN_SCREEN
    ){
        composable(route = AppDestinations.MAIN_SCREEN){
            MainScreen(navController = navController)
        }

        composable(route = AppDestinations.USER_CREATE_SCREEN){
            CreateUserView(navController = navController)
        }
    }
}