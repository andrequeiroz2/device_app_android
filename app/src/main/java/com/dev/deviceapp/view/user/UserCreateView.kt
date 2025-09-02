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
import com.dev.deviceapp.viewmodel.user.UserCreateViewModel
import com.dev.deviceapp.model.user.UserCreateRequest
import com.dev.deviceapp.viewmodel.user.UserUiState
import androidx.hilt.navigation.compose.hiltViewModel



@Composable
fun CreateUserView(
    navController: androidx.navigation.NavController,
    userViewModel: UserCreateViewModel = hiltViewModel()
) {
    val uiState by userViewModel.state.collectAsState()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm_password by remember { mutableStateOf("") }

    val user = UserCreateRequest(username, email, password, confirm_password)

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)){

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

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

        TextField(
            value = confirm_password,
            onValueChange = { confirm_password = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                userViewModel.createUser(user)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is UserUiState.Loading
        ){
            Text(text = "Create User")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when(uiState){
            is UserUiState.Loading -> CircularProgressIndicator()
            is UserUiState.Success -> Text(
                (
                        uiState as UserUiState.Success).message, color = MaterialTheme.colorScheme.primary)
            is UserUiState.Error -> Text(
                (
                        uiState as UserUiState.Error).message, color = MaterialTheme.colorScheme.error)
            else -> {}
        }
    }
}