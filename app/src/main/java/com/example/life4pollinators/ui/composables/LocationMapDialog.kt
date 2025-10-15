package com.example.life4pollinators.ui.composables

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.life4pollinators.R
import com.example.life4pollinators.utils.*
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@SuppressLint("QueryPermissionsNeeded")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationMapDialog(
    initialLatitude: Double?,
    initialLongitude: Double?,
    onLocationSelected: (Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val locationService = remember { LocationService(context) }
    val userCoordinates by locationService.coordinates.collectAsState()
    val isLoadingLocation by locationService.isLoadingLocation.collectAsState()

    var mapViewInstance: MapView? by remember { mutableStateOf(null) }
    var selectedLocation by remember {
        mutableStateOf(
            if (initialLatitude != null && initialLongitude != null) {
                Coordinates(initialLatitude, initialLongitude)
            } else null
        )
    }

    var showLocationDisabledWarning by remember { mutableStateOf(false) }
    var showPermissionDeniedWarning by remember { mutableStateOf(false) }
    var showPermissionPermanentlyDeniedWarning by remember { mutableStateOf(false) }

    // Carica la posizione utente all'apertura se il permesso è già garantito
    LaunchedEffect(Unit) {
        val hasPermission = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED ||
                context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            scope.launch {
                try {
                    locationService.getCurrentLocation()
                } catch (ex: Exception) {
                    // Silenzioso, non importa se fallisce
                }
            }
        }
    }

    val locationPermission = rememberMultiplePermissions(
        listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    ) { statuses ->
        when {
            statuses.any { it.value.isGranted } -> {
                scope.launch {
                    try {
                        val coords = locationService.getCurrentLocation()
                        coords?.let {
                            selectedLocation = it
                            mapViewInstance?.controller?.animateTo(
                                GeoPoint(it.latitude, it.longitude),
                                15.0,
                                500L
                            )
                        }
                    } catch (ex: SecurityException) {
                        // Handled
                    } catch (ex: IllegalStateException) {
                        showLocationDisabledWarning = true
                    }
                }
            }
            statuses.all { it.value == PermissionStatus.PermanentlyDenied } ->
                showPermissionPermanentlyDeniedWarning = true
            else ->
                showPermissionDeniedWarning = true
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.select_location)) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                selectedLocation?.let {
                                    onLocationSelected(it.latitude, it.longitude)
                                    onDismiss()
                                }
                            },
                            enabled = selectedLocation != null
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Confirm")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    modifier = Modifier.padding(bottom = 64.dp),
                    onClick = {
                        if (locationPermission.statuses.any { it.value.isGranted }) {
                            scope.launch {
                                try {
                                    val coords = locationService.getCurrentLocation()
                                    coords?.let {
                                        selectedLocation = it
                                        mapViewInstance?.controller?.animateTo(
                                            GeoPoint(it.latitude, it.longitude),
                                            15.0,
                                            500L
                                        )
                                    }
                                } catch (ex: SecurityException) {
                                    showPermissionDeniedWarning = true
                                } catch (ex: IllegalStateException) {
                                    showLocationDisabledWarning = true
                                }
                            }
                        } else {
                            locationPermission.launchPermissionRequest()
                        }
                    }
                ) {
                    if (isLoadingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        Icon(Icons.Default.MyLocation, contentDescription = "My Location")
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Mappa OSM
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)

                            // Centro Italia: Roma approssimativamente
                            val italyCenterPoint = GeoPoint(41.9028, 12.4964)

                            // Se c'è già una posizione selezionata, mostra quella
                            val startPoint = selectedLocation?.let {
                                GeoPoint(it.latitude, it.longitude)
                            } ?: italyCenterPoint

                            // Zoom: se c'è una selezione zoom più vicino, altrimenti vista Italia
                            val startZoom = if (selectedLocation != null) 15.0 else 6.0

                            controller.setZoom(startZoom)
                            controller.setCenter(startPoint)

                            overlays.add(object : org.osmdroid.views.overlay.Overlay() {
                                override fun onSingleTapConfirmed(
                                    e: android.view.MotionEvent,
                                    mapView: MapView
                                ): Boolean {
                                    val projection = mapView.projection
                                    val geoPoint = projection.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
                                    selectedLocation = Coordinates(geoPoint.latitude, geoPoint.longitude)
                                    mapView.invalidate()
                                    return true
                                }
                            })

                            mapViewInstance = this
                        }
                    },
                    update = { mapView ->
                        mapView.overlays.removeAll { it is Marker }

                        // Marker posizione selezionata
                        selectedLocation?.let { coords ->
                            val selectedMarker = Marker(mapView).apply {
                                position = GeoPoint(coords.latitude, coords.longitude)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = context.getString(R.string.add_sighting_selected_location)
                                icon = ContextCompat.getDrawable(context, R.drawable.ic_marker_default)
                            }
                            mapView.overlays.add(selectedMarker)
                        }

                        // Marker posizione utente (sempre visibile se disponibile)
                        userCoordinates?.let { userCoords ->
                            val isSameLocation = selectedLocation?.let {
                                distanceBetween(it, userCoords) < 10.0
                            } ?: false

                            if (!isSameLocation) {
                                val userMarker = Marker(mapView).apply {
                                    position = GeoPoint(userCoords.latitude, userCoords.longitude)
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                    title = context.getString(R.string.my_location_marker_title)
                                    icon = ContextCompat.getDrawable(context, R.drawable.ic_my_location_marker)
                                }
                                mapView.overlays.add(userMarker)
                            }
                        }

                        mapView.invalidate()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                // Info box in basso
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        selectedLocation?.let {
                            Text(
                                text = "Lat: %.4f, Lng: %.4f".format(it.latitude, it.longitude),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = stringResource(R.string.tap_to_change_location),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } ?: run {
                            Text(
                                text = stringResource(R.string.tap_to_select_location),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs di warning
    if (showLocationDisabledWarning) {
        AlertDialog(
            title = { Text(stringResource(R.string.gps_disabled_title)) },
            text = { Text(stringResource(R.string.gps_disabled_message)) },
            confirmButton = {
                TextButton(onClick = {
                    locationService.openLocationSettings()
                    showLocationDisabledWarning = false
                }) { Text(stringResource(R.string.enable)) }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDisabledWarning = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            onDismissRequest = { showLocationDisabledWarning = false }
        )
    }

    if (showPermissionDeniedWarning) {
        AlertDialog(
            title = { Text(stringResource(R.string.location_permission_denied_title)) },
            text = { Text(stringResource(R.string.location_permission_denied_message)) },
            confirmButton = {
                TextButton(onClick = {
                    locationPermission.launchPermissionRequest()
                    showPermissionDeniedWarning = false
                }) { Text(stringResource(R.string.grant)) }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDeniedWarning = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            onDismissRequest = { showPermissionDeniedWarning = false }
        )
    }

    if (showPermissionPermanentlyDeniedWarning) {
        AlertDialog(
            title = { Text(stringResource(R.string.permission_required_title)) },
            text = { Text(stringResource(R.string.permission_permanently_denied_message)) },
            confirmButton = {
                TextButton(onClick = {
                    val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = android.net.Uri.fromParts("package", context.packageName, null)
                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    }
                    showPermissionPermanentlyDeniedWarning = false
                }) { Text(stringResource(R.string.settings)) }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionPermanentlyDeniedWarning = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            onDismissRequest = { showPermissionPermanentlyDeniedWarning = false }
        )
    }
}