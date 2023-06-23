package com.sygic.driving.testapp.core.driving.utils

import android.location.Location

object ComputedLocationsFilter {

    private const val MIN_DISTANCE_METERS = 5f

    private var lastLocation: Location? = null

    fun take(location: Location): Boolean {
        val lastLocation = lastLocation
        return if(lastLocation == null || lastLocation.distanceTo(location) > MIN_DISTANCE_METERS) {
            this.lastLocation = location
            true
        }
        else
            false
    }
}