package com.sygic.driving.testapp.domain.driving.use_case

import com.sygic.driving.testapp.core.driving.DrivingManager
import com.sygic.driving.testapp.core.settings.AppSettings
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class EndTrip @Inject constructor(
    private val drivingManager: DrivingManager,
) {
    operator fun invoke() {
        drivingManager.drivingInstance.value?.endTrip()
    }
}