package com.dev.deviceapp.view.broker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dev.deviceapp.AppDestinations
import com.dev.deviceapp.viewmodel.broker.BrokerCreateViewModel
@Composable
fun BrokerTreeView(
    navController: androidx.navigation.NavController,
    brokerViewModel: BrokerCreateViewModel = hiltViewModel()

){
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(20.dp)){

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                navController.navigate(AppDestinations.BROKER_CREATE_SCREEN)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Create Broker")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                navController.navigate(AppDestinations.BROKER_GET_SCREEN)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Get Broker")
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