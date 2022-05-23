package com.sygic.driving.testapp.domain.driving.use_case

import com.sygic.driving.testapp.core.driving.DrivingManager
import javax.inject.Inject

class DeleteTrip @Inject constructor(
    private val drivingManager: DrivingManager
) {

    operator fun invoke(tripId: String) {
        val driving = drivingManager.drivingInstance.value ?: return
        with(driving.localTripsManager) {
            val tripRecord = getTripRecordByFileName(tripId) ?: return
            deleteTrip(tripRecord)
        }

    }

}