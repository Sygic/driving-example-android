package com.sygic.driving.testapp.core.utils

import android.app.PendingIntent
import android.os.Build

fun Float.mpsToKph(): Float = this * 3.6f

fun Float.format(digits: Int): String = String.format("%.${digits}f", this)

fun Double.format(digits: Int): String = String.format("%.${digits}f", this)

fun Double.radToDeg() = this * (180.0/3.14159)

fun Double.kmToM() = this * 1000.0

fun Long.millisToSeconds() = this / 1000

fun Int.toPercent(): Double {
    return toDouble() / 100.0
}

fun Int.metersToKm(): Double = toDouble() / 1000.0

fun Int.asMutableFlag(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this or PendingIntent.FLAG_MUTABLE
    } else {
        this
    }
}

fun Int.asImmutableFlag(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this or PendingIntent.FLAG_IMMUTABLE
    } else {
        this
    }
}

fun Int.daysToMillis(): Long {
    return this * 24 * 60 * 60 * 1000L
}
