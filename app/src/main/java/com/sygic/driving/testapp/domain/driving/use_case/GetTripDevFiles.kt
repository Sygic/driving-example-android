package com.sygic.driving.testapp.domain.driving.use_case

import com.sygic.driving.testapp.core.driving.DrivingManager
import java.io.File
import javax.inject.Inject

class GetTripDevFiles @Inject constructor(
    private val drivingManager: DrivingManager
) {

    operator fun invoke(tripId: String): List<File> {
        val driving = drivingManager.drivingInstance.value ?: return emptyList()
        val tripRecord = driving.localTripsManager.getTripRecordByFileName(tripId)
        return tripRecord?.developerFiles() ?: emptyList()
    }
}