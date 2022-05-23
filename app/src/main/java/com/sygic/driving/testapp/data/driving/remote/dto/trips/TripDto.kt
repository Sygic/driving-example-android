package com.sygic.driving.testapp.data.driving.remote.dto.trips

import java.util.*

data class TripDto(
    val createdDate: Date,
    val distanceKm: Double,
    val endDate: Date,
    val id: String,
    val recordingMode: String,
    val scores: List<ScoreDto>,
    val startDate: Date,
    val status: TripStatusDto,
    val user: UserDto,
    val vehicle: VehicleDto
)