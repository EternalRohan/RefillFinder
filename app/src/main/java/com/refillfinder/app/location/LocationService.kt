package com.refillfinder.app.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class UserLocation(val lat: Double, val lng: Double)

/**
 * Wraps FusedLocationProviderClient in a Kotlin Flow.
 *
 * Syllabus coverage:
 *  - Fused Location Provider
 *  - Location Updates
 *  - callbackFlow for converting callback APIs → Flow
 *  - DisposableEffect pattern (cleanup via awaitClose)
 *  - Runtime Permissions (checked before use)
 */
class LocationService(private val context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
        10_000L   // Update every 10 seconds
    ).apply {
        setMinUpdateIntervalMillis(5_000L)
        setWaitForAccurateLocation(false)
    }.build()

    /**
     * Emits location updates as a Flow.
     * Caller must have ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION granted.
     */
    @SuppressLint("MissingPermission")
    fun locationFlow(): Flow<UserLocation> = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    trySend(UserLocation(loc.latitude, loc.longitude))
                }
            }
        }

        fusedClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        )

        // awaitClose = DisposableEffect equivalent — runs when flow is cancelled
        awaitClose {
            fusedClient.removeLocationUpdates(callback)
        }
    }

    /**
     * Get the last known location once (non-streaming).
     */
    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocation(): UserLocation? {
        return try {
            val task = fusedClient.lastLocation
            // Use kotlinx-coroutines-play-services await()
            val location = com.google.android.gms.tasks.Tasks.await(task)
            location?.let { UserLocation(it.latitude, it.longitude) }
        } catch (e: Exception) {
            null
        }
    }
}
