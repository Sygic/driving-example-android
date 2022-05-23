package com.sygic.driving.testapp.domain.driving.use_case

import com.sygic.driving.testapp.core.driving.DrivingManager
import com.sygic.driving.testapp.core.settings.AppSettings
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class StartTrip @Inject constructor(
    private val drivingManager: DrivingManager,
    private val appSettings: AppSettings
) {
    suspend operator fun invoke(): Boolean {
        val driving = drivingManager.drivingInstance.value ?: return false

        if(appSettings.endTripsAutomatically.first())
            driving.startTrip()
        else
            driving.startTripWithManualEnd()

        return true
    }
}