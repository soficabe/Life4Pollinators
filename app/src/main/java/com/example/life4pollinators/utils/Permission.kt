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

/**
 * Enum che rappresenta lo stato di un permesso runtime.
 */
enum class PermissionStatus {
    Unknown, //Permesso non ancora richiesto
    Granted, // Permesso concesso dall'utente
    Denied, // Permesso negato ma può essere richiesto nuovamente
    PermanentlyDenied; // Permesso negato permanentemente ("Don't ask again" selezionato)

    // Helper property per verificare se il permesso è concesso
    val isGranted get() = this == Granted
}

/**
 * Interfaccia per gestire richieste di permessi multipli.
 *
 * @property statuses Map che associa ogni permesso al suo stato corrente
 */
interface MultiplePermissionHandler {
    val statuses: Map<String, PermissionStatus>
    fun launchPermissionRequest() // Lancia la richiesta di permessi al sistema
}

/**
 * Composable che gestisce la richiesta di permessi runtime multipli.
 *
 * Caratteristiche:
 * - Gestisce lo stato di ogni permesso (Unknown, Granted, Denied, PermanentlyDenied)
 * - Distingue tra permesso negato temporaneamente e permanentemente
 *
 * @param permissions Lista di permessi Android da richiedere
 * @param onResult Callback invocato quando l'utente risponde alla richiesta di permessi
 * @return Handler per gestire lo stato e la richiesta dei permessi
 * @throws IllegalStateException Se il Context non è una ComponentActivity
 */
@Composable
fun rememberMultiplePermissions(
    permissions: List<String>,
    onResult: (Map<String, PermissionStatus>) -> Unit
): MultiplePermissionHandler {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
        ?: throw IllegalStateException("Context must be a ComponentActivity")

    // Stato iniziale: verifica quali permessi sono già concessi
    var statuses by remember {
        mutableStateOf(
            permissions.associateWith { permission ->
                if (
                    ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
                ) PermissionStatus.Granted else PermissionStatus.Unknown
            }
        )
    }

    // Launcher per la richiesta di permessi
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { newPermissions ->
        statuses = newPermissions.mapValues { (permission, isGranted) ->
            when {
                isGranted -> PermissionStatus.Granted
                // shouldShowRequestPermissionRationale = true → può richiedere ancora
                activity.shouldShowRequestPermissionRationale(permission) -> PermissionStatus.Denied
                // shouldShowRequestPermissionRationale = false → "Don't ask again" selezionato
                else -> PermissionStatus.PermanentlyDenied
            }
        }
        onResult(statuses)
    }

    // Crea l'handler che espone statuses e launch
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