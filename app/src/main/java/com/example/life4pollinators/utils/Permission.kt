package com.example.life4pollinators.utils

import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

enum class PermissionStatus {
    Unknown,
    Granted,
    Denied,
    PermanentlyDenied;

    val isGranted get() = this == Granted
}

interface MultiplePermissionHandler {
    val statuses: Map<String, PermissionStatus>
    fun launchPermissionRequest()
}

@Composable
fun rememberMultiplePermissions(
    permissions: List<String>,
    onResult: (Map<String, PermissionStatus>) -> Unit
): MultiplePermissionHandler {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
        ?: throw IllegalStateException("Context must be a ComponentActivity")

    var statuses by remember {
        mutableStateOf(
            permissions.associateWith { permission ->
                if (
                    ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
                ) PermissionStatus.Granted else PermissionStatus.Unknown
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { newPermissions ->
        statuses = newPermissions.mapValues { (permission, isGranted) ->
            when {
                isGranted -> PermissionStatus.Granted
                activity.shouldShowRequestPermissionRationale(permission) -> PermissionStatus.Denied
                else -> PermissionStatus.PermanentlyDenied
            }
        }
        onResult(statuses)
    }

    val permissionHandler = remember(permissionLauncher) {
        object : MultiplePermissionHandler {
            override val statuses get() = statuses
            override fun launchPermissionRequest() {
                permissionLauncher.launch(permissions.toTypedArray())
            }
        }
    }
    return permissionHandler
}