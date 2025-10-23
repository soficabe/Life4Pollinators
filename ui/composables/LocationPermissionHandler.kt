package com.example.life4pollinators.ui.composables

import android.Manifest
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.life4pollinators.R
import com.example.life4pollinators.utils.*
import kotlinx.coroutines.launch

/**
 * Composable che gestisce la richiesta di permessi di localizzazione e i relativi dialog di warning.
 *
 * Gestisce automaticamente:
 * - Richiesta permessi location (ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
 * - Dialog per GPS disabilitato
 * - Dialog per permesso negato
 * - Dialog per permesso negato permanentemente
 *
 * @param locationService Service per ottenere la posizione GPS
 * @param onLocationObtained Callback invocato quando le coordinate sono state ottenute con successo
 * @param onLoadingChange Callback per notificare lo stato di loading (opzionale)
 * @param onPermissionDenied Callback invocato quando i permessi sono negati (opzionale)
 * @param content Contenuto composable che può richiedere i permessi tramite requestLocation()
 */
@Composable
fun LocationPermissionHandler(
    locationService: LocationService,
    onLocationObtained: (Coordinates) -> Unit,
    onLoadingChange: (Boolean) -> Unit = {},
    onPermissionDenied: () -> Unit = {},
    content: @Composable (requestLocation: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showLocationDisabledWarning by remember { mutableStateOf(false) }
    var showPermissionDeniedWarning by remember { mutableStateOf(false) }
    var showPermissionPermanentlyDeniedWarning by remember { mutableStateOf(false) }

    // Gestione permessi location
    val locationPermission = rememberMultiplePermissions(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    ) { statuses ->
        when {
            // Almeno un permesso concesso
            statuses.any { it.value.isGranted } -> {
                scope.launch {
                    onLoadingChange(true)
                    try {
                        val coords = locationService.getCurrentLocation()
                        coords?.let { onLocationObtained(it) }
                    } catch (ex: SecurityException) {
                        // Gestito
                    } catch (ex: IllegalStateException) {
                        showLocationDisabledWarning = true
                    } finally {
                        onLoadingChange(false)
                    }
                }
            }
            // Tutti i permessi negati permanentemente
            statuses.all { it.value == PermissionStatus.PermanentlyDenied } -> {
                showPermissionPermanentlyDeniedWarning = true
                onPermissionDenied()
            }
            // Permessi negati
            else -> {
                showPermissionDeniedWarning = true
                onPermissionDenied()
            }
        }
    }

    // Funzione per richiedere la posizione
    val requestLocation: () -> Unit = {
        if (locationPermission.statuses.any { it.value.isGranted }) {
            // Permesso già garantito: richiedi posizione direttamente
            scope.launch {
                onLoadingChange(true)
                try {
                    val coords = locationService.getCurrentLocation()
                    coords?.let { onLocationObtained(it) }
                } catch (ex: SecurityException) {
                    showPermissionDeniedWarning = true
                } catch (ex: IllegalStateException) {
                    showLocationDisabledWarning = true
                } finally {
                    onLoadingChange(false)
                }
            }
        } else {
            // Richiedi permessi
            locationPermission.launchPermissionRequest()
        }
    }

    // Renderizza il contenuto passando la funzione requestLocation
    content(requestLocation)

    // Dialog di warning
    LocationWarningDialogs(
        showLocationDisabledWarning = showLocationDisabledWarning,
        onDismissLocationDisabled = { showLocationDisabledWarning = false },
        onEnableLocation = {
            locationService.openLocationSettings()
            showLocationDisabledWarning = false
        },
        showPermissionDeniedWarning = showPermissionDeniedWarning,
        onDismissPermissionDenied = { showPermissionDeniedWarning = false },
        onGrantPermission = {
            locationPermission.launchPermissionRequest()
            showPermissionDeniedWarning = false
        },
        showPermissionPermanentlyDeniedWarning = showPermissionPermanentlyDeniedWarning,
        onDismissPermissionPermanentlyDenied = { showPermissionPermanentlyDeniedWarning = false },
        onOpenSettings = {
            val intent = android.content.Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            ).apply {
                data = android.net.Uri.fromParts("package", context.packageName, null)
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            }
            showPermissionPermanentlyDeniedWarning = false
        }
    )
}

/**
 * Composable che mostra i 3 AlertDialog per i warning di localizzazione.
 *
 * @param showLocationDisabledWarning Flag per mostrare il dialog GPS disabilitato
 * @param onDismissLocationDisabled Callback per chiudere il dialog GPS disabilitato
 * @param onEnableLocation Callback per aprire le impostazioni GPS
 * @param showPermissionDeniedWarning Flag per mostrare il dialog permesso negato
 * @param onDismissPermissionDenied Callback per chiudere il dialog permesso negato
 * @param onGrantPermission Callback per richiedere nuovamente il permesso
 * @param showPermissionPermanentlyDeniedWarning Flag per mostrare il dialog permesso negato permanentemente
 * @param onDismissPermissionPermanentlyDenied Callback per chiudere il dialog permesso permanentemente negato
 * @param onOpenSettings Callback per aprire le impostazioni dell'app
 */
@Composable
fun LocationWarningDialogs(
    showLocationDisabledWarning: Boolean,
    onDismissLocationDisabled: () -> Unit,
    onEnableLocation: () -> Unit,
    showPermissionDeniedWarning: Boolean,
    onDismissPermissionDenied: () -> Unit,
    onGrantPermission: () -> Unit,
    showPermissionPermanentlyDeniedWarning: Boolean,
    onDismissPermissionPermanentlyDenied: () -> Unit,
    onOpenSettings: () -> Unit
) {
    // GPS disabilitato
    if (showLocationDisabledWarning) {
        AlertDialog(
            title = { Text(stringResource(R.string.gps_disabled_title)) },
            text = { Text(stringResource(R.string.gps_disabled_message)) },
            confirmButton = {
                TextButton(onClick = onEnableLocation) {
                    Text(stringResource(R.string.enable))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissLocationDisabled) {
                    Text(stringResource(R.string.cancel))
                }
            },
            onDismissRequest = onDismissLocationDisabled
        )
    }

    // Permesso negato
    if (showPermissionDeniedWarning) {
        AlertDialog(
            title = { Text(stringResource(R.string.location_permission_denied_title)) },
            text = { Text(stringResource(R.string.location_permission_denied_message)) },
            confirmButton = {
                TextButton(onClick = onGrantPermission) {
                    Text(stringResource(R.string.grant))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissPermissionDenied) {
                    Text(stringResource(R.string.cancel))
                }
            },
            onDismissRequest = onDismissPermissionDenied
        )
    }

    // Permesso negato permanentemente
    if (showPermissionPermanentlyDeniedWarning) {
        AlertDialog(
            title = { Text(stringResource(R.string.permission_required_title)) },
            text = { Text(stringResource(R.string.permission_permanently_denied_message)) },
            confirmButton = {
                TextButton(onClick = onOpenSettings) {
                    Text(stringResource(R.string.settings))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissPermissionPermanentlyDenied) {
                    Text(stringResource(R.string.cancel))
                }
            },
            onDismissRequest = onDismissPermissionPermanentlyDenied
        )
    }
}