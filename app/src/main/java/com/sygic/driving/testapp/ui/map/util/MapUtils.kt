package com.sygic.driving.testapp.ui.map.util

import android.graphics.Color
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
                color(Color.rgb(100,100, 255))
            }
            PolylineType.Computed -> {
                color(Color.rgb(255, 100, 100))
            }
        }
        width(10f)
    }


    enum class PolylineType {
        System, Computed
    }
}