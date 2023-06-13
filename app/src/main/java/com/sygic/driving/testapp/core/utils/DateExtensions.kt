package com.sygic.driving.testapp.core.utils

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

private val formatterHhMmSs = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)

fun Date.formatHhMmSs(): String {
    return formatterHhMmSs.format(this)
}

fun Date.formatDate(): String {
    return DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(this)
}

fun Date.formatDateTime(): String {
    return "${formatDate()} ${formatHhMmSs()}"
}

fun Date.addDays(days: Int): Date {
    return Date(time + days.daysToMillis())
}
