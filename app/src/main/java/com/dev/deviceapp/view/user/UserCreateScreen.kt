package com.dev.deviceapp.view.user

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.dev.deviceapp.AppDestinations
import com.dev.deviceapp.model.user.UserCreateRequest
import com.dev.deviceapp.viewmodel.user.UserCreateUiState
import com.dev.deviceapp.viewmodel.user.UserCreateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCreateScreen(
    navController: NavController,
    userViewModel: UserCreateViewModel = hiltViewModel()
){
    val uiState by userViewModel.state.collectAsState()
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is UserCreateUiState.Error -> {
                Toast.makeText(
                    context,
                    (uiState as UserCreateUiState.Error).message,
                    Toast.LENGTH_LONG
                ).show()
            }
            is UserCreateUiState.Success -> {
                Toast.makeText(
                    context,
                    "User created successfully",
                    Toast.LENGTH_LONG
                ).show()
                navController.navigate(AppDestinations.LOGIN_SCREEN)
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()){
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF121212)
        ){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(
                    text = "Create User",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFF00A86B)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        if (it.isNotBlank()) usernameError = null
                    },
                    label = { Text("Username", color = Color(0xFFE0F2F1)) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.AccountCircle,
                            contentDescription = null,
                            tint = Color(0xFFE0F2F1)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    isError = usernameError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00A86B),
                        unfocusedBorderColor = Color(0xFF00693E)
                    )
                )
                if (usernameError != null) {
                    Text(
                        text = usernameError ?: "",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                            .padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (it.isNotBlank()) emailError = null
                    },
                    label = { Text("Email", color = Color(0xFFE0F2F1)) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Email,
                            contentDescription = null,
                            tint = Color(0xFFE0F2F1)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = emailError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00A86B),
                        unfocusedBorderColor = Color(0xFF00693E)
                    )
                )
                if (emailError != null) {
                    Text(
                        text = emailError ?: "",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                            .padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (it.isNotBlank()) passwordError = null
                    },
                    label = { Text("Password", color = Color(0xFFE0F2F1)) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            tint = Color(0xFFE0F2F1)
                        )
                    },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = image,
                                contentDescription = null,
                                tint = Color(0xFFE0F2F1)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    isError = passwordError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00A86B),
                        unfocusedBorderColor = Color(0xFF00693E)
                    )
                )
                if (passwordError != null) {
                    Text(
                        text = passwordError ?: "",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                            .padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        if (it.isNotBlank()) confirmPasswordError = null
                    },
                    label = { Text("Password", color = Color(0xFFE0F2F1)) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.ConfirmationNumber,
                            contentDescription = null,
                            tint = Color(0xFFE0F2F1)
                        )
                    },
                    trailingIcon = {
                        val image = if (confirmPasswordVisible) Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = image,
                                contentDescription = null,
                                tint = Color(0xFFE0F2F1)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    isError = confirmPasswordError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00A86B),
                        unfocusedBorderColor = Color(0xFF00693E)
                    )
                )
                if (confirmPasswordError != null) {
                    Text(
                        text = confirmPasswordError ?: "",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                            .padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        var hasError = false

                        if (username.isBlank()) {
                            usernameError = "Username is required"
                            hasError = true
                        }

                        if (email.isBlank()) {
                            emailError = "Email is required"
                            hasError = true
                        }

                        if (password.isBlank()) {
                            passwordError = "Password is required"
                            hasError = true
                        }

                        if (password.isBlank()) {
                            confirmPasswordError = "Confirm password is required"
                            hasError = true
                        }

                        if (password != confirmPassword) {
                            confirmPasswordError = "Passwords do not match"
                            hasError = true
                        }

                        if (!hasError) {
                            userViewModel.createUser(
                                UserCreateRequest(username, email, password, confirmPassword))
                        }
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
                    Text("Create")
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
            }
        }
        if (uiState is UserCreateUiState.Loading){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF00A86B))
            }
        }
    }
}