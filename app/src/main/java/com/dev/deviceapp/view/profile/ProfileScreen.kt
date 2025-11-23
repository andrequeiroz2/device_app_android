package com.dev.deviceapp.view.profile

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.dev.deviceapp.AppDestinations
import com.dev.deviceapp.model.user.UserGetRequest
import com.dev.deviceapp.model.user.UserSuccess
import com.dev.deviceapp.viewmodel.profile.ProfileViewModel
import com.dev.deviceapp.viewmodel.user.UserGetUiState
import com.dev.deviceapp.viewmodel.user.UserGetViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
    userGetViewModel: UserGetViewModel = hiltViewModel(),
    onLogout: () -> Unit = {}
) {

    val tokenInfo = viewModel.tokenInfo
    if (tokenInfo == null) {
        Log.e("ProfileScreen", "Token is null")
        LaunchedEffect(Unit) { onLogout() }
        return
    }

    val context = LocalContext.current
    val uiStateUserGet by userGetViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        userGetViewModel.getUser(
            UserGetRequest(uuid = tokenInfo.uuid)
        )
    }

    LaunchedEffect(uiStateUserGet) {
        when (uiStateUserGet) {
            is UserGetUiState.Error -> {
                val message = (uiStateUserGet as UserGetUiState.Error).message
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF121212)
    ) {
        when(uiStateUserGet){
            is UserGetUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF00A86B))
                }
            }
            is UserGetUiState.Success -> {

                val user = (uiStateUserGet as UserGetUiState.Success).user

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(40.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        "Profile",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF00A86B)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column {
                        Text("👤 Name: ${user.username}", color = Color.White)
                        Text("📧 Email: ${user.email}", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {

                            val user = (uiStateUserGet as UserGetUiState.Success).user

                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("user", UserSuccess(user.uuid, user.username, user.email))

                            navController.navigate(AppDestinations.USER_UPDATE_SCREEN)
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00A86B),
                            contentColor = Color.White
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Edit")
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            navController.popBackStack()
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00A86B),
                            contentColor = Color.White
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Back")
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            navController.navigate(AppDestinations.USER_DELETE_SCREEN)
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD2507D),
                            contentColor = Color.White
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Delete")
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            viewModel.logout()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9AA24B),
                            contentColor = Color.White
                        ),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Text("Logout")
                    }
                }
            }
            is UserGetUiState.Error -> {
                val message = (uiStateUserGet as UserGetUiState.Error).message
                Log.e("ProfileScreen", "Error: $message")
                navController.navigate(AppDestinations.LOGIN_SCREEN)
            }
            else -> {
                Log.e("ProfileScreen", "Unknown state: $uiStateUserGet")
            }
        }
    }
}
