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

data class Coordinates(val latitude: Double, val longitude: Double)

class LocationService(
    private val ctx: Context
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)
    private val locationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _coordinates = MutableStateFlow<Coordinates?>(null)
    val coordinates = _coordinates.asStateFlow()

    private val _isLoadingLocation = MutableStateFlow(false)
    val isLoadingLocation = _isLoadingLocation.asStateFlow()

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