package com.dev.deviceapp.view.user

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.dev.deviceapp.viewmodel.user.UserCreateViewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import com.dev.deviceapp.AppDestinations

@Composable
fun UserTreeView(
    navController: androidx.navigation.NavController,
    userViewModel: UserCreateViewModel = hiltViewModel()
){
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(20.dp)
    ){

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                navController.navigate(AppDestinations.USER_CREATE_SCREEN)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Create User")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                navController.navigate(AppDestinations.USER_GET_SCREEN)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Get User")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                navController.navigate(AppDestinations.USER_DELETE_SCREEN)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Delete User")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                navController.navigate(AppDestinations.USER_UPDATE_SCREEN)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Update User")
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
    }
}
