package com.sygic.driving.testapp.ui.trip_details.utils

import android.content.Context
import com.sygic.driving.data.TripEvent
import com.sygic.driving.data.TripEventType
import com.sygic.driving.testapp.R
import com.sygic.driving.testapp.core.utils.*
import com.sygic.driving.testapp.domain.driving.model.*
import com.sygic.driving.testapp.ui.trip_details.NameValueProperty
import com.sygic.driving.trips.TripUploadStatus


fun DrivingTripDetails.toListOfProperties(ctx: Context): List<NameValueProperty> {
    val result = mutableListOf(
        Pair(
            ctx.getString(R.string.trip_details_length),
            ctx.getStringFormat(R.string.value_of_km, lengthMeters.metersToKm().format(3))
        ),
        Pair(
            ctx.getString(R.string.trip_details_duration),
            durationSeconds.toLong().formatDurationHhMmSs()
        ),
        Pair(
            ctx.getString(R.string.trip_details_start_time),
            startTime.formatDateTime()
        ),
        Pair(
            ctx.getString(R.string.trip_details_end_time),
            endTime.formatDateTime()
        ),
        Pair(
            ctx.getString(R.string.trip_details_storage),
            storage.toString()
        )
    )
    uploadStatus?.let {
        result.add(
            Pair(
                ctx.getString(R.string.trip_details_status),
                uploadStatus.toString(ctx)
            )
        )
    }
    properties.iterator().forEach {
        result.add(Pair(it.key.toString(ctx), it.value))
    }
    return result
}

fun DrivingTripDetailsProperty.toString(ctx: Context): String {
    return when(this) {
        DrivingTripDetailsProperty.StartReason -> ctx.getString(R.string.trip_details_start_reason)
        DrivingTripDetailsProperty.EndReason -> ctx.getString(R.string.trip_details_end_reason)
        DrivingTripDetailsProperty.EventsSummary -> ctx.getString(R.string.trip_details_events_summary)
        DrivingTripDetailsProperty.RecordingMode -> ctx.getString(R.string.trip_details_manual_trip)
        DrivingTripDetailsProperty.Score -> ctx.getString(R.string.trip_details_score_summary)
        DrivingTripDetailsProperty.EvaluationStatus -> ctx.getString(R.string.trip_details_status)
    }
}

fun DrivingTripEvent.toListOfProperties(ctx: Context): List<NameValueProperty> {
    val properties = mutableListOf(
        Pair(
            ctx.getString(R.string.event_details_time),
            time.formatDateTime()
        ),
        Pair(
            ctx.getString(R.string.event_details_duration),
            ctx.getStringFormat(R.string.value_of_sec, duration.format(3))
        )
    )

    peak?.let {
        properties.add(
            Pair(
                ctx.getString(R.string.event_details_peak),
                ctx.getStringFormat(R.string.value_of_g, it.format(3))
            )
        )
    }

    severity?.let {
        properties.add(
            Pair(
                ctx.getString(R.string.event_details_severity),
                it
            )
        )
    }

    return properties
}


fun List<TripEvent>.summaryString(): String {
    val a = count { it.eventType == TripEventType.Acceleration }
    val b = count { it.eventType == TripEventType.Braking }
    val c = count { it.eventType == TripEventType.Cornering }
    val d = count { it.eventType == TripEventType.Distraction }
    val h = count { it.eventType == TripEventType.Harsh }

    return "${a}xA ${b}xB ${c}xC ${d}xD ${h}xH"
}

fun TripUploadStatus.toString(context: Context): String {
    return when(this) {
        is TripUploadStatus.Unknown -> context.getString(R.string.local_trip_upload_unknown)
        is TripUploadStatus.Uploaded -> context.getString(R.string.local_trip_uploaded)
        is TripUploadStatus.NotUploaded -> context.getString(R.string.local_trip_not_uploaded)
        is TripUploadStatus.UploadFailed ->
            context.getStringFormat(R.string.local_trip_upload_error, lastUploadError)
    }
}