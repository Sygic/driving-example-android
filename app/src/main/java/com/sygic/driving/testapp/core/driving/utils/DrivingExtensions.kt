package com.sygic.driving.testapp.core.driving.utils

import android.location.Location
import com.sygic.driving.Driving
import com.sygic.driving.TripDiscardReason
import com.sygic.driving.data.DetectorState
import com.sygic.driving.data.TripEvent
import com.sygic.driving.data.TripState
import com.sygic.driving.testapp.domain.driving.model.DrivingTripEvent
import com.sygic.driving.testapp.domain.driving.model.DrivingTripState
import com.sygic.driving.testapp.domain.driving.model.toDrivingTripEvent
import com.sygic.driving.trips.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*

fun Driving.angleFlow(): Flow<Double> = callbackFlow {
    val listener = object : Driving.EventListener {
        override fun onDetectorAngleChanged(radians: Double) {
            trySend(radians)
        }
    }
    addEventListener(listener)
    awaitClose { removeEventListener(listener) }
}

fun Driving.detectorStateFlow(): Flow<DetectorState> = callbackFlow {
    val listener = object : Driving.EventListener {
        override fun onDetectorStateChanged(state: DetectorState) {
            trySend(state)
        }
    }
    trySend(detectorState)
    addEventListener(listener)
    awaitClose { removeEventListener(listener) }
}

fun Driving.eventsFlow(): Flow<DrivingTripEvent> = callbackFlow {
    val listener = object: Driving.EventListener {
        override fun onEventStarted(event: TripEvent) {
            trySend(event.toDrivingTripEvent(true))
        }

        override fun onEventUpdated(event: TripEvent) {
            trySend(event.toDrivingTripEvent(true))
        }

        override fun onEventFinished(event: TripEvent) {
            trySend(event.toDrivingTripEvent(false))
        }

        override fun onEventCancelled(event: TripEvent) {
            trySend(event.toDrivingTripEvent(false))
        }
    }

    addEventListener(listener)
    awaitClose { removeEventListener(listener) }
}

fun Driving.tripStateFlow(): Flow<DrivingTripState> = callbackFlow {

    val listener = object: Driving.EventListener {
        override fun onTripStarted(time: Date, location: Location) {
            trySend(DrivingTripState(TripState.Started, startTime = time))
        }

        override fun onTripFinished(time: Date, location: Location) {
            trySend(DrivingTripState(TripState.NotStarted))
        }

        override fun onTripPossiblyStarted(time: Date, location: Location) {
            trySend(DrivingTripState(TripState.PossiblyStarted, startTime = time))
        }

        override fun onTripStartCancelled(time: Date) {
            trySend(DrivingTripState(TripState.NotStarted))
        }

        override fun onTripDiscarded(reason: TripDiscardReason) {
            trySend(DrivingTripState(TripState.NotStarted, discardReason = reason))
        }
    }

    trySend(DrivingTripState(tripState))

    addEventListener(listener)
    awaitClose { removeEventListener(listener) }
}

fun Driving.altitudeFlow(): Flow<Double> = callbackFlow {

    val listener = object: Driving.SensorListener {
        override fun onRelativeAltitude(
            time: Date,
            relativeAltitudeInMeters: Double,
            pressureInKPascals: Double
        ) {
            trySend(relativeAltitudeInMeters)
        }
    }

    addSensorListener(listener)
    awaitClose { removeSensorListener(listener) }
}

fun Driving.distanceDrivenFlow(): Flow<Double> = callbackFlow {

    val listener = object: Driving.EventListener {
        override fun onDistanceDrivenUpdated(distanceInMeters: Double) {
            trySend(distanceInMeters)
        }
    }
    trySend(tripDrivenDistance)
    addEventListener(listener)
    awaitClose { removeEventListener(listener) }
}

fun Driving.systemLocationFlow(): Flow<Location> = callbackFlow {
    val listener = object: Driving.LocationListener {
        override fun onLocation(location: Location) {
            trySend(location)
        }
    }
    addLocationListener(listener)
    awaitClose { removeLocationListener(listener) }
}

fun Driving.computedLocationFlow(): Flow<Location> = callbackFlow {
    val listener = object: Driving.LocationListener {
        override fun onComputedLocation(location: Location) {
            trySend(location)
        }
    }
    addLocationListener(listener)
    awaitClose { removeLocationListener(listener) }
}

suspend fun LocalTripsManager.getTripHeaders(): List<TripHeader> = suspendCancellableCoroutine { continuation ->
    getTripHeaders(object : TripHeadersCallback {
        override fun onTripHeaders(tripHeaders: List<TripHeader>) {
            continuation.resume(tripHeaders, null)
        }
    })
}

suspend fun LocalTripsManager.getTripDetails(tripRecord: TripRecord): TripDetails? = suspendCancellableCoroutine { continuation ->
    getTripDetails(tripRecord, object : TripDetailsCallback {
        override fun onTripDetails(tripDetails: TripDetails?) {
            continuation.resume(tripDetails, null)
        }
    })
}
