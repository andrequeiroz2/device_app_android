package com.dev.deviceapp

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dev.deviceapp.permission.PermissionsRequiredScreen
import com.dev.deviceapp.permission.haveAllPermissions
import com.dev.deviceapp.ui.theme.DeviceAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var haveBlePermissions by mutableStateOf(false)

    @RequiresApi(Build.VERSION_CODES.S)
    @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DeviceAppTheme {
                MaterialTheme  {
                    if (haveBlePermissions) {
                        AppNavigation()
                    } else {
                        PermissionsRequiredScreen(
                            onPermissionGranted = { haveBlePermissions = true },
                            onPermissionDenied = {
                                Toast.makeText(
                                    this,
                                    "Bluetooth permissions are required. The application will be closed",
                                    Toast.LENGTH_LONG
                                ).show()
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onResume() {
        super.onResume()
        if (haveAllPermissions(this)) {
            haveBlePermissions = true
        }
    }
}
