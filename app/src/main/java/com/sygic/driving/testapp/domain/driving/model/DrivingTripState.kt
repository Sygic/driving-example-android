package com.sygic.driving.testapp.domain.driving.model

import com.sygic.driving.TripDiscardReason
import com.sygic.driving.data.TripState
import java.util.*

class DrivingTripState(
    val state: TripState = TripState.NotStarted,
    val startTime: Date? = null,
    val discardReason: TripDiscardReason? = null
)