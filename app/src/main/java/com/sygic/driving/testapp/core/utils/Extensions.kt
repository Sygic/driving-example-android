package com.sygic.driving.testapp.core.utils

import android.os.Build
import kotlinx.coroutines.flow.SharingStarted

val <T> T.exhaustive: T
    get() = this

val WHILE_SUBSCRIBED_WITH_TIMEOUT = SharingStarted.WhileSubscribed(5000L)

fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.lowercase().startsWith(manufacturer.lowercase())) {
            model.capitalize()
        } else {
            manufacturer.capitalize() + " " + model
        }
    }