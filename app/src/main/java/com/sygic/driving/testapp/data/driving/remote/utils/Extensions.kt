package com.sygic.driving.testapp.data.driving.remote.utils

import com.sygic.driving.data.TripEventType
import com.sygic.driving.testapp.core.utils.GooglePolylineUtils
import com.sygic.driving.testapp.core.utils.format
import com.sygic.driving.testapp.core.utils.kmToM
import com.sygic.driving.testapp.core.utils.millisToSeconds
import com.sygic.driving.testapp.data.driving.remote.dto.trip_details.EventDto
import com.sygic.driving.testapp.data.driving.remote.dto.trip_details.EventTypeDto
import com.sygic.driving.testapp.data.driving.remote.dto.trip_details.ScoreDto
import com.sygic.driving.testapp.data.driving.remote.dto.trip_details.TripDetailsDto
import com.sygic.driving.testapp.data.driving.remote.dto.trips.TripDto
import com.sygic.driving.testapp.data.driving.remote.dto.trips.TripStatusDto
import com.sygic.driving.testapp.domain.driving.model.*

fun TripDto.toDrivingTripHeader(): DrivingTripHeader {
    return DrivingTripHeader(
        id = id,
        startTime = startDate,
        endTime = endDate,
        status = status.toDrivingTripHeaderStatus(),
        storage = DrivingTripStorage.Server
    )
}

fun TripStatusDto.toDrivingTripHeaderStatus(): DrivingTripHeaderStatus {
    return when(this) {
        TripStatusDto.Evaluated -> DrivingTripHeaderStatus.Success
        TripStatusDto.Error -> DrivingTripHeaderStatus.Error
        else -> DrivingTripHeaderStatus.None
    }
}

fun TripDetailsDto.toDrivingTripDetails(): DrivingTripDetails {
    return DrivingTripDetails(
        startTime = startDate,
        endTime = endDate,
        lengthMeters = distanceKm.kmToM().toInt(),
        durationSeconds = (endDate.time - startDate.time).millisToSeconds().toInt(),
        segments = listOf(rawRoute.googlePolylineToDrivingTripSegment()),
        events = events.map { it.toDrivingTripEvent() },
        uploadStatus = null,
        storage = DrivingTripStorage.Server,
        properties = sortedMapOf(
            DrivingTripDetailsProperty.RecordingMode to recordingMode.toString(),
            DrivingTripDetailsProperty.EvaluationStatus to status,
            DrivingTripDetailsProperty.Score to scores.toSummaryString()
        )
    )
}

fun String.googlePolylineToDrivingTripSegment(): DrivingTripSegment {
    return DrivingTripSegment(
        trajectory = GooglePolylineUtils.decode(this).toTypedArray(),
        isDriving = true
    )
}

fun EventDto.toDrivingTripEvent(): DrivingTripEvent {
    return DrivingTripEvent(
        type = this.type.toEventType(),
        time = timestamp,
        position = GooglePolylineUtils.getFirstPosition(rawRoute),
        duration = durationInSeconds,
        peak = null,
        severity = severity,
        currentSize = null,
        active = false
    )
}

fun EventTypeDto.toEventType(): TripEventType {
    return when(this) {
        EventTypeDto.Unknown -> TripEventType.Harsh
        EventTypeDto.Acceleration -> TripEventType.Acceleration
        EventTypeDto.Braking -> TripEventType.Braking
        EventTypeDto.Cornering -> TripEventType.Cornering
        EventTypeDto.Distraction -> TripEventType.Distraction
        EventTypeDto.Speeding -> TripEventType.Speeding
        EventTypeDto.Pothole -> TripEventType.PotHole
        EventTypeDto.HoleInData -> TripEventType.HoleInData
    }
}

fun List<ScoreDto>.toSummaryString(): String {
    return joinToString(separator = "\n") { score ->
        "${score.type}: ${score.score.format(1)}"
    }
}