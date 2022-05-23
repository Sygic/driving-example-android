package com.sygic.driving.testapp.domain.driving.use_case

import com.sygic.driving.testapp.core.driving.DrivingManager
import com.sygic.driving.testapp.core.driving.utils.getTripDetails
import com.sygic.driving.testapp.core.utils.Resource
import com.sygic.driving.testapp.domain.driving.model.DrivingTripDetails
import com.sygic.driving.testapp.domain.driving.model.toDrivingTripDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetLocalTripDetails @Inject constructor(
    private val drivingManager: DrivingManager
) {

    operator fun invoke(fileName: String): Flow<Resource<DrivingTripDetails>> = flow {
        emit(Resource.Loading())

        val driving = drivingManager.drivingInstance.value
        if(driving == null) {
            emit(Resource.Error(message = "Driving is not initialized"))
            return@flow
        }

        with(driving.localTripsManager) {
            val tripRecord = getTripRecordByFileName(fileName)
            if (tripRecord == null) {
                emit(Resource.Error(message = "Failed to get TripRecord"))
                return@flow
            }

            val details = getTripDetails(tripRecord)
            if(details == null) {
                emit(Resource.Error(message = "Failed to get TripDetails"))
                return@flow
            }

            emit(Resource.Success(data = details.toDrivingTripDetails()))
        }
    }
}