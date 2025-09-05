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
import com.dev.deviceapp.model.user.UserGetRequest
import com.dev.deviceapp.model.user.UserUpdateRequest
import com.dev.deviceapp.viewmodel.user.UserGetUiState
import com.dev.deviceapp.viewmodel.user.UserUpdateUiState
import com.dev.deviceapp.viewmodel.user.UserUpdateViewModel

@Composable
fun UserUpdateView(
    navController: NavController,
    userViewModel: UserUpdateViewModel = hiltViewModel()
){
    val uiState by userViewModel.state.collectAsState()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    val params = UserUpdateRequest(username, email)

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Spacer(modifier = Modifier.height(20.dp))

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

        Button(
            onClick = {
                userViewModel.updateUser(params)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is UserUpdateUiState.Loading
        ){
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

        Spacer(modifier = Modifier.height(16.dp))

        when(uiState){
            is UserUpdateUiState.Loading -> CircularProgressIndicator()
            is UserUpdateUiState.Success -> Text(
                (
                        uiState as UserUpdateUiState.Success).message, color = MaterialTheme.colorScheme.primary)
            is UserUpdateUiState.Error -> Text(
                (
                        uiState as UserUpdateUiState.Error).message, color = MaterialTheme.colorScheme.error)
            else -> {}
        }

    }
}