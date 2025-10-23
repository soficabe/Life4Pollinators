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

/**
 * Dialog fullscreen con mappa interattiva per selezione posizione.
 *
 * Utilizza OpenStreetMap (OSMDroid) per mostrare una mappa interattiva.
 *
 * Funzionalità:
 * - Tap su mappa per selezionare posizione
 * - FAB per centrarsi sulla posizione corrente (GPS)
 * - Marker rosso per posizione selezionata
 * - Marker blu per posizione utente (se disponibile)
 * - Gestione permessi location con dialog informativi
 * - Zoom e pan tramite touch gestures
 *
 * Comportamento iniziale:
 * - Se c'è una posizione già selezionata: zoom su quella
 * - Altrimenti: mostra l'Italia intera (zoom out)
 * - Se ha permessi: carica automaticamente posizione utente
 *
 * @param initialLatitude Latitudine iniziale (se già selezionata)
 * @param initialLongitude Longitudine iniziale (se già selezionata)
 * @param onLocationSelected Callback con coordinate selezionate
 * @param onDismiss Callback di chiusura dialog
 */
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

    // Service per gestione GPS
    val locationService = remember { LocationService(context) }
    val userCoordinates by locationService.coordinates.collectAsState()
    val isLoadingLocation by locationService.isLoadingLocation.collectAsState()

    // Riferimento alla MapView per controllo programmatico
    var mapViewInstance: MapView? by remember { mutableStateOf(null) }

    // Posizione selezionata dall'utente
    var selectedLocation by remember {
        mutableStateOf(
            if (initialLatitude != null && initialLongitude != null) {
                Coordinates(initialLatitude, initialLongitude)
            } else null
        )
    }

    // Carica posizione utente all'apertura se permesso già garantito
    LaunchedEffect(Unit) {
        val hasPermission = context.checkSelfPermission(
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                context.checkSelfPermission(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            scope.launch {
                try {
                    locationService.getCurrentLocation()
                } catch (ex: Exception) {
                    // Silenzioso: non importa se fallisce
                }
            }
        }
    }

    // Funzione helper per animare la mappa verso le coordinate
    val animateMapToLocation: (Coordinates) -> Unit = { coords ->
        mapViewInstance?.controller?.animateTo(
            GeoPoint(coords.latitude, coords.longitude),
            15.0, // Zoom level
            500L  // Durata animazione (ms)
        )
    }

    LocationPermissionHandler(
        locationService = locationService,
        onLocationObtained = { coords ->
            selectedLocation = coords
            animateMapToLocation(coords)
        },
        onLoadingChange = { /* isLoadingLocation già gestito dal service */ }
    ) { requestLocation ->

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false) // Fullscreen
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
                            // Pulsante conferma (disabilitato se nessuna selezione)
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
                // FAB per centrarsi su posizione corrente
                floatingActionButton = {
                    FloatingActionButton(
                        modifier = Modifier.padding(bottom = 64.dp),
                        onClick = { requestLocation() }
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
                    // Mappa OSM integrata tramite AndroidView
                    AndroidView(
                        factory = { ctx ->
                            MapView(ctx).apply {
                                setTileSource(TileSourceFactory.MAPNIK) // Stile mappa
                                setMultiTouchControls(true) // Pinch-to-zoom

                                // Centro Italia: Roma (approssimativo)
                                val italyCenterPoint = GeoPoint(41.9028, 12.4964)

                                // Determina punto di partenza
                                val startPoint = selectedLocation?.let {
                                    GeoPoint(it.latitude, it.longitude)
                                } ?: italyCenterPoint

                                // Zoom: se c'è selezione zoom vicino, altrimenti vista Italia
                                val startZoom = if (selectedLocation != null) 15.0 else 6.0

                                controller.setZoom(startZoom)
                                controller.setCenter(startPoint)

                                // Overlay per catturare tap sulla mappa
                                overlays.add(object : org.osmdroid.views.overlay.Overlay() {
                                    override fun onSingleTapConfirmed(
                                        e: android.view.MotionEvent,
                                        mapView: MapView
                                    ): Boolean {
                                        // Converti pixel tap in coordinate geografiche
                                        val projection = mapView.projection
                                        val geoPoint = projection.fromPixels(
                                            e.x.toInt(),
                                            e.y.toInt()
                                        ) as GeoPoint

                                        selectedLocation = Coordinates(
                                            geoPoint.latitude,
                                            geoPoint.longitude
                                        )
                                        mapView.invalidate() // Ridisegna mappa
                                        return true
                                    }
                                })

                                mapViewInstance = this
                            }
                        },
                        update = { mapView ->
                            // Rimuovi tutti i marker esistenti
                            mapView.overlays.removeAll { it is Marker }

                            // Marker posizione selezionata (rosso)
                            selectedLocation?.let { coords ->
                                val selectedMarker = Marker(mapView).apply {
                                    position = GeoPoint(coords.latitude, coords.longitude)
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    title = context.getString(R.string.add_sighting_selected_location)
                                    icon = ContextCompat.getDrawable(
                                        context,
                                        R.drawable.ic_marker_default
                                    )
                                }
                                mapView.overlays.add(selectedMarker)
                            }

                            // Marker posizione utente (blu) se disponibile e diversa da quella selezionata
                            userCoordinates?.let { userCoords ->
                                val isSameLocation = selectedLocation?.let {
                                    distanceBetween(it, userCoords) < 10.0 // Soglia 10 metri
                                } ?: false

                                if (!isSameLocation) {
                                    val userMarker = Marker(mapView).apply {
                                        position = GeoPoint(userCoords.latitude, userCoords.longitude)
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                        title = context.getString(R.string.my_location_marker_title)
                                        icon = ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_my_location_marker
                                        )
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

                    // Info box in basso con coordinate selezionate
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
                                    text = "Lat: %.4f, Lng: %.4f".format(
                                        it.latitude,
                                        it.longitude
                                    ),
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
    }
}