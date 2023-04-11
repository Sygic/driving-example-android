package com.sygic.driving.testapp.ui.map.util

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions

object MapUtils {

    fun createPolyline(locations: List<Location>, type: PolylineType): PolylineOptions =
        PolylineOptions().apply {
            applyPolylineStyle(type)
            locations.forEach { add(it.toLatLng()) }
        }

    private fun Location.toLatLng() = LatLng(latitude, longitude)

    private fun PolylineOptions.applyPolylineStyle(polylineType: PolylineType) {
        when (polylineType) {
            PolylineType.System -> {
                color(0x7F0000FF)
            }
            PolylineType.Computed -> {
                color(0x7FFF0000)
            }
        }
        width(6f)
    }


    enum class PolylineType {
        System, Computed
    }
}