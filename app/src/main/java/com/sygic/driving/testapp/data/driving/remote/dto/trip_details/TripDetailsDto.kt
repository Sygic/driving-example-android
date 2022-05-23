package com.sygic.driving.testapp.data.driving.remote.dto.trip_details

import com.google.gson.annotations.SerializedName
import java.util.*

data class TripDetailsDto(
    val createdDate: Date,
    val distanceKm: Double,
    val endDate: Date,
    val events: List<EventDto>,
    val id: String,
    val matchedRoute: String,
    val rawRoute: String,
    val recordingMode: RecordingMode,
    val scores: List<ScoreDto>,
    val startDate: Date,
    val status: String,
    val user: UserDto,
    val vehicle: VehicleDto
)

enum class RecordingMode {
    @SerializedName("manual") Manual,
    @SerializedName("automatic") Automatic
}