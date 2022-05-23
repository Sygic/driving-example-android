package com.sygic.driving.testapp.data.driving.remote.dto.trips

import com.google.gson.annotations.SerializedName

data class TripsPageDto(
    @SerializedName("data")
    val trips: List<TripDto>,
    val page: Int,
    val pageSize: Int,
    val pagesCount: Int,
    val totalItemsCount: Int
)