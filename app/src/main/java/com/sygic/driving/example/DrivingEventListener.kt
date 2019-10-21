package com.sygic.driving.example

import com.sygic.driving.Driving
import com.sygic.driving.data.TripEvent
import com.sygic.driving.data.TripReport

class DrivingEventListener(
    private val logger: Logger,
    private val drivingViewModel: DrivingViewModel
): Driving.EventListener {
    override fun onDetectorAngleChanged(radians: Double) {
    }

    override fun onDetectorStateChanged(state: Int) {
    }

    override fun onEventCancelled(event: TripEvent) {
        logger.log(event, "cancelled")
    }

    override fun onEventFinished(event: TripEvent) {
        logger.log(event, "ended")
    }

    override fun onEventStarted(event: TripEvent) {
        logger.log(event, "started")
    }

    override fun onEventUpdated(event: TripEvent) {
    }

    override fun onTripFinished(timestamp: Double) {
        logger.log("Trip ended")
        drivingViewModel.isInTrip.value = false
    }

    override fun onTripFinalData(trip: TripReport) {
    }

    override fun onTripStarted(timestamp: Double) {
        logger.log("Trip started")
        drivingViewModel.isInTrip.value = true
    }

}