package com.dev.deviceapp.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@RequiresApi(Build.VERSION_CODES.S)
private val ALL_BLE_PERMISSIONS = arrayOf(
    Manifest.permission.BLUETOOTH_CONNECT,
    Manifest.permission.BLUETOOTH_SCAN
)

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun PermissionsRequiredScreen(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted.values.all { it }) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bluetooth Permissions Required",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.padding(vertical = 16.dp),
            text = "This app needs Bluetooth permissions to scan for and connect to BLE devices. Please grant the necessary permissions to continue.",
            textAlign = TextAlign.Center
        )
        Button(onClick = { launcher.launch(ALL_BLE_PERMISSIONS) }) {
            Text("Grant Permissions")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
fun haveAllPermissions(context: Context): Boolean {
    return ALL_BLE_PERMISSIONS.all { permission ->
        context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }
}
