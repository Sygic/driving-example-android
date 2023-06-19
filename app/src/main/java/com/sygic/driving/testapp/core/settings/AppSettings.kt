package com.sygic.driving.testapp.core.settings

import android.content.Context
import com.sygic.driving.VehicleType
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
    abstract val vehicleType: Flow<VehicleType>
    abstract val bluetoothDongleName: Flow<String>
    abstract val bluetoothDongleAddress: Flow<String>
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
    abstract suspend fun setVehicleType(vehicleType: String)
    abstract suspend fun setBluetoothDongleName(name: String)
    abstract suspend fun setBluetoothDongleAddress(address: String)

    val keyAutomaticTripDetection = context.getString(R.string.key_automatic_trip_detection)
    val keyDeveloperMode = context.getString(R.string.key_developer_mode)
    val keyMotionActivity = context.getString(R.string.key_motion_activity)
    val keyDisableDetectionBatteryLowerThan = context.getString(R.string.key_disable_detection_battery_lower_than)
    val keyDisableDetectionInPowerSaver = context.getString(R.string.key_disable_detection_power_saver)
    val keyBatteryOptimization = context.getString(R.string.key_battery_optimization)
    val keyCarVin = context.getString(R.string.key_car_vin)
    val keyAppVersion = context.getString(R.string.key_app_version)
    val keySendLogs = context.getString(R.string.key_send_logs)
    val keyUserId = context.getString(R.string.key_user_id)
    val keyEndTripsAutomatically = context.getString(R.string.key_end_trips_automatically)
    val keyMinTripDuration = context.getString(R.string.key_min_trip_duration)
    val keyMinTripLength = context.getString(R.string.key_min_trip_length)
    val keyVehicleType = context.getString(R.string.key_vehicle_type)
    val keyVehicleTypeCar = context.getString(R.string.key_vehicle_type_car)
    val keyVehicleTypeTruck = context.getString(R.string.key_vehicle_type_truck)
    val keyBluetoothDongleName = context.getString(R.string.key_bluetooth_dongle_name)
    val keyBluetoothDongleAddress = context.getString(R.string.key_bluetooth_dongle_address)

    protected val defaultAutomaticTripDetection = context.getBoolean(R.bool.default_automatic_trip_detection)
    protected val defaultDeveloperMode = context.getBoolean(R.bool.default_developer_mode)
    protected val defaultMotionActivity = context.getBoolean(R.bool.default_motion_activity)
    protected val defaultAppVersion = BuildConfig.APP_VERSION
    protected val defaultUserId = context.getAndroidId()
    protected val defaultEndTripsAutomatically = context.getBoolean(R.bool.default_end_trips_automatically)
    protected val defaultMinTripDuration = context.getInteger(R.integer.default_min_trip_duration)
    protected val defaultMinTripLength = context.getInteger(R.integer.default_min_trip_length)
    protected val defaultVehicleType = context.getString(R.string.default_vehicle_type)
    protected val defaultBluetoothDongleName = context.getString(R.string.default_bluetooth_dongle_name)
    protected val defaultBluetoothDongleAddress = context.getString(R.string.default_bluetooth_dongle_address)
}