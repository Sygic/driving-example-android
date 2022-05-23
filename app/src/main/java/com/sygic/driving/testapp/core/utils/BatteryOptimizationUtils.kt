package com.sygic.driving.testapp.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

enum class BatteryOptimizationState {
    Unknown, Enabled, Disabled
}

fun Context.getBatteryOptimizationState(): BatteryOptimizationState {
    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        return BatteryOptimizationState.Unknown

    (getSystemService(Context.POWER_SERVICE) as? PowerManager)?.let {
        return if(it.isIgnoringBatteryOptimizations(packageName)) BatteryOptimizationState.Disabled
        else BatteryOptimizationState.Enabled
    }

    return BatteryOptimizationState.Unknown
}

fun Context.openBatteryOptimizationSettings(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        return false

    return try {
        val intent = Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Uri.parse("package:$packageName")
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun Int.toBatteryOptimizationState(): BatteryOptimizationState {
    return if(this in BatteryOptimizationState.values().indices)
        BatteryOptimizationState.values()[this]
    else
        BatteryOptimizationState.Unknown
}

fun BatteryOptimizationState.toInt(): Int {
    return ordinal
}