package com.sygic.driving.testapp.core.location

import android.Manifest
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.sygic.driving.testapp.core.utils.checkPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class DriverLocationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    fun requestLocationUpdates(): Flow<Location> = callbackFlow {
        if(context.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_PASSIVE, 1000).build()
            val locationCallback = object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                    locationResult.lastLocation?.let { trySend(it) }
                }
            }
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let { trySend(it) }
            }
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            awaitClose {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }
    }

}