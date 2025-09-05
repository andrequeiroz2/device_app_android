package com.dev.deviceapp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dev.deviceapp.view.login.LoginView
import com.dev.deviceapp.view.user.CreateUserView
import com.dev.deviceapp.view.user.UserDeleteView
import com.dev.deviceapp.view.user.UserGetView
import com.dev.deviceapp.view.user.UserTreeView
import com.dev.deviceapp.view.user.UserUpdateView

object AppDestinations{
    const val MAIN_SCREEN = "mainScreen"
    const val USER_TREE_SCREEN = "userTreeScreen"
    const val USER_CREATE_SCREEN = "userCreateScreen"
    const val USER_GET_SCREEN = "userGetScreen"
    const val USER_DELETE_SCREEN = "userDeleteScreen"
    const val USER_UPDATE_SCREEN = "userUpdateScreen"
    const val LOGIN_SCREEN = "loginScreen"
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

        composable(route = AppDestinations.LOGIN_SCREEN){
            LoginView(navController = navController)
        }

        composable(route = AppDestinations.USER_TREE_SCREEN){
            UserTreeView(navController = navController)
        }

        composable(route = AppDestinations.USER_CREATE_SCREEN){
            CreateUserView(navController = navController)
        }

        composable(route = AppDestinations.USER_GET_SCREEN){
            UserGetView(navController = navController)
        }

        composable(route = AppDestinations.USER_DELETE_SCREEN){
            UserDeleteView(navController = navController)
        }

        composable(route = AppDestinations.USER_UPDATE_SCREEN){
            UserUpdateView(navController = navController)
        }
    }
}