package com.sygic.driving.testapp.domain.driving.use_case

import com.sygic.driving.testapp.core.driving.DrivingManager
import javax.inject.Inject

class SimulateTrip @Inject constructor(
    private val drivingManager: DrivingManager
) {

    operator fun invoke(tripId: String) {
        val driving = drivingManager.drivingInstance.value ?: return
        val tripRecord = driving.localTripsManager.getTripRecordByFileName(tripId) ?: return
        driving.simulationManager.play(trip = tripRecord, playbackSpeed = 4.0f)
    }

}