package com.sygic.driving.testapp.core.settings

import android.content.Context
import com.sygic.driving.testapp.BuildConfig
import com.sygic.driving.testapp.R
import com.sygic.driving.testapp.core.utils.BatteryOptimizationState
import com.sygic.driving.testapp.core.utils.getAndroidId
import com.sygic.driving.testapp.core.utils.getBoolean
import com.sygic.driving.testapp.core.utils.getInteger
import kotlinx.coroutines.flow.Flow

abstract class AppSettings(context: Context) {
    
    abstract val userId: Flow<String>
    abstract val developerMode: Flow<Boolean>

    abstract val automaticTripDetection: Flow<Boolean>
    abstract val enableMotionActivity: Flow<Boolean>
    abstract val disableDetectionIfBatteryLowerThan: Flow<Int>
    abstract val disableDetectionInPowerSaver: Flow<Boolean>

    abstract val batteryOptimizationState: Flow<BatteryOptimizationState>

    abstract val endTripsAutomatically: Flow<Boolean>
    abstract val minTripDurationSeconds: Flow<Int>
    abstract val minTripLengthMeters: Flow<Int>

    abstract val appVersion: Flow<String>
    
    abstract suspend fun setUserId(userId: String)
    abstract suspend fun setDeveloperMode(developerMode: Boolean)
    abstract suspend fun setAutomaticTripDetection(automaticTripDetection: Boolean)
    abstract suspend fun setEnableMotionActivity(enableMotionActivity: Boolean)
    abstract suspend fun setDisableDetectionIfBatteryLowerThan(disableDetectionIfBatteryLowerThan: Int)
    abstract suspend fun setDisableDetectionInPowerSaver(disableDetectionInPowerSaver: Boolean)
    abstract suspend fun setBatteryOptimizationState(batteryOptimizationState: BatteryOptimizationState)
    abstract suspend fun setEndTripsAutomatically(endTripsAutomatically: Boolean)
    abstract suspend fun setMinTripDurationSeconds(minTripDurationSeconds: Int)
    abstract suspend fun setMinTripLengthMeters(minTripLengthMeters: Int)

    val keyAutomaticTripDetection =
        context.getString(R.string.key_automatic_trip_detection)

    val keyDeveloperMode =
        context.getString(R.string.key_developer_mode)

    val keyMotionActivity =
        context.getString(R.string.key_motion_activity)

    val keyDisableDetectionBatteryLowerThan =
        context.getString(R.string.key_disable_detection_battery_lower_than)

    val keyDisableDetectionInPowerSaver =
        context.getString(R.string.key_disable_detection_power_saver)

    val keyBatteryOptimization =
        context.getString(R.string.key_battery_optimization)

    val keyAppVersion =
        context.getString(R.string.key_app_version)

    val keyUserId =
        context.getString(R.string.key_user_id)

    val keyEndTripsAutomatically =
        context.getString(R.string.key_end_trips_automatically)

    val keyMinTripDuration =
        context.getString(R.string.key_min_trip_duration)

    val keyMinTripLength =
        context.getString(R.string.key_min_trip_length)


    protected val defaultAutomaticTripDetection =
        context.getBoolean(R.bool.default_automatic_trip_detection)

    protected val defaultDeveloperMode =
        context.getBoolean(R.bool.default_developer_mode)

    protected val defaultMotionActivity =
        context.getBoolean(R.bool.default_motion_activity)

    protected val defaultDisableDetectionBatteryLowerThan =
        context.getInteger(R.integer.default_disable_detection_battery_lower_than)

    protected val defaultDisableDetectionInPowerSaver =
        context.getBoolean(R.bool.default_disable_detection_power_saver)

    protected val defaultBatteryOptimization =
        context.getInteger(R.integer.default_battery_optimization)

    protected val defaultAppVersion = BuildConfig.APP_VERSION

    protected val defaultUserId = context.getAndroidId()

    protected val defaultEndTripsAutomatically =
        context.getBoolean(R.bool.default_end_trips_automatically)

    protected val defaultMinTripDuration =
        context.getInteger(R.integer.default_min_trip_duration)

    protected val defaultMinTripLength =
        context.getInteger(R.integer.default_min_trip_length)


}