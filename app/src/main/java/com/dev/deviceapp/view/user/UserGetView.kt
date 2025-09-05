package com.dev.deviceapp.view.user

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dev.deviceapp.model.user.UserGetRequest
import com.dev.deviceapp.viewmodel.user.UserGetViewModel
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.navigation.NavController
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import com.dev.deviceapp.viewmodel.user.UserGetUiState


@Composable
fun UserGetView(
    navController: NavController,
    userViewModel: UserGetViewModel = hiltViewModel()
){

    val uiState by userViewModel.state.collectAsState()

    var uuid by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    val params = UserGetRequest(uuid, email)

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Spacer(modifier = Modifier.height(20.dp))

        TextField(
            value = uuid,
            onValueChange = { uuid = it },
            label = { Text("UUID") },
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
                userViewModel.getUser(params)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is UserGetUiState.Loading
        ){
            Text(text = "Get User")
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
            is UserGetUiState.Loading -> CircularProgressIndicator()
            is UserGetUiState.Success -> Text(
                (
                        uiState as UserGetUiState.Success).message, color = MaterialTheme.colorScheme.primary)
            is UserGetUiState.Error -> Text(
                (
                        uiState as UserGetUiState.Error).message, color = MaterialTheme.colorScheme.error)
            else -> {}
        }
    }

}
