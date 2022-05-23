package com.sygic.driving.testapp.data.driving.remote

import com.sygic.driving.testapp.data.driving.remote.dto.trips.TripsPageDto
import com.sygic.driving.testapp.data.driving.remote.dto.trip_details.TripDetailsWrapperDto
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.*

interface DrbsApi {

    @GET("/api/v2/driving/trips")
    fun getTrips(
        @HeaderMap authHeaders: Map<String, String>,
        @Query("RangeFrom") fromDate: Date,
        @Query("RangeTo") toDate: Date,
        @Query("UserExternalId") userId: String,
        @Query("UserType") userType: String = "user",
        @Query("RangeType") rangeType: String = "startDate",
        @Query("Page") page: Int = 1,
        @Query("PageSize") pageSize: Int = 50,
        @Query("OrderDirection") order: String = "desc"
    ): Call<TripsPageDto>

    @GET("/api/v2/driving/trips/{tripId}/detail")
    fun getTripDetails(
        @HeaderMap authHeaders: Map<String, String>,
        @Path("tripId") tripId: String
    ): Call<TripDetailsWrapperDto>
}
