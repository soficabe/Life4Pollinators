package com.example.life4pollinators.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Data class che rappresenta una coordinata geografica.
 *
 * @property latitude Latitudine in gradi decimali
 * @property longitude Longitudine in gradi decimali
 */
data class Coordinates(val latitude: Double, val longitude: Double)

/**
 * Service per la gestione della localizzazione GPS.
 *
 * Utilizza Google Play Services Location API per ottenere la posizione corrente
 * dell'utente.
 *
 * Responsabilità:
 * - Ottenere la posizione GPS corrente
 * - Gestire stati di loading
 * - Aprire le impostazioni di localizzazione del dispositivo
 *
 * @property ctx Context Android necessario per accedere ai servizi di sistema
 */
class LocationService(
    private val ctx: Context
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)
    private val locationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _coordinates = MutableStateFlow<Coordinates?>(null)

    // Ultime coordinate ottenute
    val coordinates = _coordinates.asStateFlow()

    private val _isLoadingLocation = MutableStateFlow(false)

    // Indica se è in corso una richiesta di posizione
    val isLoadingLocation = _isLoadingLocation.asStateFlow()

    /**
     * Ottiene la posizione GPS corrente dell'utente.
     *
     * Processo:
     * 1. Verifica che il GPS sia abilitato
     * 2. Verifica che i permessi siano concessi
     * 3. Richiede la posizione
     * 4. Aggiorna coordinates e isLoadingLocation
     *
     * @return Coordinate geografiche o null se la posizione non è disponibile
     * @throws IllegalStateException Se il GPS è disabilitato
     * @throws SecurityException Se i permessi di localizzazione non sono concessi
     */
    suspend fun getCurrentLocation(): Coordinates? {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            throw IllegalStateException("GPS disabled")
        }
        if (
            ContextCompat.checkSelfPermission(
                ctx,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            throw SecurityException("Location permission not granted")
        }

        _isLoadingLocation.value = true
        val location = withContext(Dispatchers.IO) {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()
        }
        _isLoadingLocation.value = false

        _coordinates.value =
            if (location != null) Coordinates(location.latitude, location.longitude) else null

        return coordinates.value
    }

    /**
     * Apre la schermata delle impostazioni di localizzazione del sistema.
     *
     * Permette all'utente di abilitare il GPS se disabilitato.
     */
    @SuppressLint("QueryPermissionsNeeded")
    fun openLocationSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (intent.resolveActivity(ctx.packageManager) != null) {
            ctx.startActivity(intent)
        }
    }
}

/**
 * Calcola la distanza in metri tra due coordinate geografiche.
 *
 * @param start Coordinate di partenza
 * @param end Coordinate di destinazione
 * @return Distanza in metri tra i due punti
 */
fun distanceBetween(
    start: Coordinates,
    end: Coordinates
): Float {
    val result = FloatArray(1)
    Location.distanceBetween(
        start.latitude, start.longitude,
        end.latitude, end.longitude,
        result
    )
    return result[0]
}