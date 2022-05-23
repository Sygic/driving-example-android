package com.sygic.driving.testapp.data.driving.remote.dto.trip_details

import com.google.gson.annotations.SerializedName

data class TripDetailsWrapperDto(
    @SerializedName("data") val tripDetails: TripDetailsDto
)