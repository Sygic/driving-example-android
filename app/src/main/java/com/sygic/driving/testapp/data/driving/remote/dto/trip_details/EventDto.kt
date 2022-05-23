package com.sygic.driving.testapp.data.driving.remote.dto.trip_details

import com.google.gson.annotations.SerializedName
import java.util.*

data class EventDto(
    val durationInSeconds: Double,
    val eventNumber: Int,
    val matchedRoute: String,
    val maxSpeedKmH: Any,
    val rawRoute: String,
    val severity: String,
    val speedKmH: Double,
    val speedLimitKmH: Any,
    val timestamp: Date,
    val type: EventTypeDto
)

enum class EventTypeDto {
    @SerializedName("unknown", alternate = ["harsh"]) Unknown,
    @SerializedName("acceleration") Acceleration,
    @SerializedName("braking")      Braking,
    @SerializedName("cornering")    Cornering,
    @SerializedName("distraction")  Distraction,
    @SerializedName("speeding")     Speeding,
    @SerializedName("pothole")      Pothole,
    @SerializedName("hole_in_data") HoleInData
}