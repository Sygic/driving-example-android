package com.sygic.driving.testapp.domain.driving.use_case

import com.sygic.driving.Driving
import com.sygic.driving.testapp.core.driving.utils.getTripHeaders
import com.sygic.driving.testapp.core.driving.utils.toDrivingTripHeader
import com.sygic.driving.testapp.core.utils.Resource
import com.sygic.driving.testapp.domain.driving.model.DrivingTripHeader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetLocalTrips @Inject constructor() {
    suspend operator fun invoke(driving: Driving): Flow<Resource<List<DrivingTripHeader>>> = flow {
        emit(Resource.Loading())
        val tripHeaders = driving.localTripsManager.getTripHeaders()
            .map { it.toDrivingTripHeader() }
            .sortedByDescending { it.startTime }
        emit(Resource.Success(tripHeaders))
    }
}