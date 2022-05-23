package com.sygic.driving.testapp.domain.driving.model

import android.location.Location
import com.sygic.driving.data.TripEvent
import com.sygic.driving.data.TripEventType
import java.util.*

class DrivingTripEvent(
    val type: TripEventType,
    val time: Date,
    val position: Location?,
    val duration: Double,
    val peak: Double?,
    val severity: String?,
    val currentSize: Double?,
    val active: Boolean
)

fun TripEvent.toDrivingTripEvent(active: Boolean = false): DrivingTripEvent {
    return DrivingTripEvent(
        type = eventType,
        time = time,
        position = position,
        duration = length,
        peak = maxSize,
        severity = null,
        currentSize = currentSize,
        active = active)
}