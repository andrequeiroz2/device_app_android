package com.dev.deviceapp.view.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dev.deviceapp.AppDestinations
import com.dev.deviceapp.model.login.LoginRequest
import com.dev.deviceapp.viewmodel.login.LoginUiState
import com.dev.deviceapp.viewmodel.login.LoginViewModel
import com.dev.deviceapp.viewmodel.user.UserUiState

@Composable
fun LoginView(
    navController: androidx.navigation.NavController,
    loginViewModel: LoginViewModel = hiltViewModel()
){

    val uiState by loginViewModel.state.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val login = LoginRequest(email, password)

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(20.dp)
    ){

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
                loginViewModel.login(login)
            },
            modifier = Modifier.fillMaxWidth()
        ){
            Text("Login")
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
            is LoginUiState.Loading -> CircularProgressIndicator()
            is LoginUiState.Success -> Text(
                (
                        uiState as LoginUiState.Success).message, color = MaterialTheme.colorScheme.primary)
            is LoginUiState.Error -> Text(
                (
                        uiState as LoginUiState.Error).message, color = MaterialTheme.colorScheme.error)
            else -> {}
        }
    }
}