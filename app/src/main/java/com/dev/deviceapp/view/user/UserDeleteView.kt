package com.dev.deviceapp.view.user

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.navigation.NavController
import com.dev.deviceapp.model.login.LoginRequest
import com.dev.deviceapp.viewmodel.user.UserDeleteUiState
import com.dev.deviceapp.viewmodel.user.UserDeleteViewModel


@Composable
fun UserDeleteView(
    navController: NavController,
    userViewModel: UserDeleteViewModel = hiltViewModel()
){
    val uiState by userViewModel.state.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val params = LoginRequest(email, password)

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Spacer(modifier = Modifier.height(20.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                userViewModel.deleteUser(params)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is UserDeleteUiState.Loading
        ){
            Text(text = "Delete User")
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
            is UserDeleteUiState.Loading -> CircularProgressIndicator()
            is UserDeleteUiState.Success -> Text(
                (
                        uiState as UserDeleteUiState.Success).message, color = MaterialTheme.colorScheme.primary)
            is UserDeleteUiState.Error -> Text(
                (
                        uiState as UserDeleteUiState.Error).message, color = MaterialTheme.colorScheme.error)
            else -> {}
        }
    }
}