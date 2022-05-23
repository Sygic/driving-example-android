package com.sygic.driving.testapp.ui.trip_details.utils

import android.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.sygic.driving.data.TripEventType
import com.sygic.driving.testapp.domain.driving.model.DrivingTripEvent
import com.sygic.driving.testapp.domain.driving.model.DrivingTripSegment


private val drivingPolylineOptions: PolylineOptions
get() = PolylineOptions().apply {
    color(Color.BLUE)
    width(7.0f)
}

private val walkingPolylineOptions: PolylineOptions
get() = PolylineOptions().apply {
    color(Color.MAGENTA)
    width(5.0f)
}

fun DrivingTripSegment.toGoogleMapPolyline(): PolylineOptions {
    val polyline = if (isDriving) drivingPolylineOptions else walkingPolylineOptions
    trajectory.forEach { polyline.add(LatLng(it.latitude, it.longitude)) }
    return polyline
}

fun DrivingTripEvent.toGoogleMapMarker(title: String?): MarkerOptions? {
    title ?: return null
    val pos = position ?: return null
    val color = type.eventColor() ?: return null
    return MarkerOptions().apply {
        icon(BitmapDescriptorFactory.defaultMarker(color))
        title(title)
        position(LatLng(pos.latitude, pos.longitude))
    }
}

private fun TripEventType.eventColor() = when (this) {
    TripEventType.Acceleration -> BitmapDescriptorFactory.HUE_RED
    TripEventType.Braking -> BitmapDescriptorFactory.HUE_GREEN
    TripEventType.Cornering -> BitmapDescriptorFactory.HUE_BLUE
    TripEventType.Distraction -> BitmapDescriptorFactory.HUE_VIOLET
    TripEventType.Speeding -> BitmapDescriptorFactory.HUE_YELLOW
    TripEventType.PotHole -> BitmapDescriptorFactory.HUE_AZURE
    TripEventType.Harsh -> BitmapDescriptorFactory.HUE_YELLOW
    else -> null
}

fun GoogleMap.centerMap(polylines: List<PolylineOptions>) {
    if(polylines.isEmpty())
        return

    var minLat = 0.0
    var maxLat = 0.0
    var minLon = 0.0
    var maxLon = 0.0
    var foundValue = false
    for(polyline in polylines) {
        for (point in polyline.points) {
            if (!foundValue) {
                minLat = point.latitude
                maxLat = point.latitude
                minLon = point.longitude
                maxLon = point.longitude
                foundValue = true
            }

            minLat = point.latitude.coerceAtMost(minLat)
            maxLat = point.latitude.coerceAtLeast(maxLat)
            minLon = point.longitude.coerceAtMost(minLon)
            maxLon = point.longitude.coerceAtLeast(maxLon)
        }
    }

    if(foundValue) {
        if(minLat == maxLat) {
            minLat -= 0.1
            maxLat += 0.1
        }

        if(minLon == maxLon) {
            minLon -= 0.1
            maxLon += 0.1
        }

        val bounds = LatLngBounds(LatLng(minLat, minLon), LatLng(maxLat, maxLon))
        moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 250))
    }
}