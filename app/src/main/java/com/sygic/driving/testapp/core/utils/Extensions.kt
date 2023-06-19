package com.sygic.driving.testapp.core.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.SharingStarted

val <T> T.exhaustive: T
    get() = this

fun Context.checkPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

val WHILE_SUBSCRIBED_WITH_TIMEOUT = SharingStarted.WhileSubscribed(5000L)