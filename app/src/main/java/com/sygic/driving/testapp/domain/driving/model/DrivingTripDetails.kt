package com.sygic.driving.testapp.domain.driving.model

import com.sygic.driving.testapp.ui.trip_details.utils.summaryString
import com.sygic.driving.trips.TripDetails
import com.sygic.driving.trips.TripUploadStatus
import java.util.*

class DrivingTripDetails(
    val startTime: Date,
    val endTime: Date,
    val lengthMeters: Int,
    val durationSeconds: Int,
    val segments: List<DrivingTripSegment>,
    val events: List<DrivingTripEvent>,
    val uploadStatus: TripUploadStatus?,
    val storage: DrivingTripStorage,
    val properties: SortedMap<DrivingTripDetailsProperty, String>
)

enum class DrivingTripDetailsProperty {
    StartReason, EndReason, RecordingMode, EventsSummary, Score, EvaluationStatus
}

fun TripDetails.toDrivingTripDetails(): DrivingTripDetails {
    return DrivingTripDetails(
        startTime = header.startTime,
        endTime = header.endTime,
        lengthMeters = header.drivenDistanceMeters,
        durationSeconds = header.drivingDurationSeconds,
        segments = segments.map { it.toDrivingTripSegment() },
        events = events.map { it.toDrivingTripEvent() },
        uploadStatus = header.uploadStatus,
        storage = DrivingTripStorage.Local,
        properties = sortedMapOf(
            DrivingTripDetailsProperty.StartReason to header.startReason.toString(),
            DrivingTripDetailsProperty.EndReason to header.endReason.toString(),
            DrivingTripDetailsProperty.EventsSummary to events.summaryString()
        )
    )
}

