package com.sygic.driving.testapp.domain.driving.model

import android.location.Location
import com.sygic.driving.trips.MotionType
import com.sygic.driving.trips.TripSegment

class DrivingTripSegment(
    val trajectory: Array<Location>,
    val isDriving: Boolean
)

fun TripSegment.toDrivingTripSegment(): DrivingTripSegment {
    return DrivingTripSegment(
        trajectory = trajectory,
        isDriving = motionType == MotionType.Driving
    )
}