package com.sygic.driving.example

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import com.sygic.driving.api.StatsPeriod
import com.sygic.driving.api.Trip
import com.sygic.driving.api.TripDetails
import com.sygic.driving.api.UserStats
import com.sygic.driving.data.TripEvent
import com.sygic.driving.data.TripEventType
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.*

class Logger(
    private val context: Context,
    private val layout: ViewGroup,
    private val scrollView: ScrollView
) {

    fun clear() {
        layout.removeAllViews()
    }

    fun log(text: String) {
        val textView = TextView(context)
        textView.text = text
        layout.addView(textView)
        scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
    }

    fun logTitle(text: String) {
        val textView = TextView(context).apply {
            this.text = text
            typeface = Typeface.DEFAULT_BOLD

        }
        layout.addView(textView)
        scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
    }

    fun log(trip: Trip) {
        log("${formatDate(trip.startDate)} - ${formatDate(trip.endDate)} -> ${String.format("%.1f", trip.totalDistanceInKm)} km")
    }

    fun log(trip: TripDetails) {
        log("${formatDate(trip.startDate)} - ${formatDate(trip.endDate)}")
        log("Driven: ${String.format("%.1f", trip.totalDistanceInKm)} km")
        log("Total score: ${trip.totalScore.toInt()}")
        log("Acceleration: ${trip.accelerationScore.toInt()}")
        log("Braking: ${trip.brakingScore.toInt()}")
        log("Cornering: ${trip.corneringScore.toInt()}")
        log("Distraction: ${trip.distractionScore.toInt()}")
        log("Speeding: ${trip.speedingScore.toInt()}")
    }

    fun log(stats: UserStats) {
        val period = when(stats.period.type) {
            StatsPeriod.Type.Total -> "Lifetime"
            StatsPeriod.Type.Last7Days -> "Last 7 days"
            else -> return
        }
        log(period)
        log("   Number of trips: ${stats.tripsCount}")
        log("   Overall: ${stats.totalScore.me.toInt()}")
        log("   Others: ${stats.totalScore.othersWorld.toInt()}")
    }

    fun log(event: TripEvent, action: String) {
        val type = when(event.eventType) {
            TripEventType.ACCELERATION -> "Acceleration"
            TripEventType.BRAKING -> "Braking"
            TripEventType.CORNERING -> "Cornering"
            TripEventType.DISTRACTION -> "Distraction"
            TripEventType.SPEEDING -> "Speeding"
            TripEventType.HARSH -> "Harsh"
            else -> "(other)"
        }

        val eventDate = Date((event.timestamp * 1000.0).toLong())
        log("Event $type $action  @${formatTime(eventDate)}")
    }

    private fun formatDate(date: Date): String {
        val dateFormatter = SimpleDateFormat("yyyy/MM/dd hh:mm")
        return dateFormatter.format(date)
    }

    private fun formatTime(date: Date): String {
        val dateFormatter = SimpleDateFormat("hh:mm:ss")
        return dateFormatter.format(date)
    }
}