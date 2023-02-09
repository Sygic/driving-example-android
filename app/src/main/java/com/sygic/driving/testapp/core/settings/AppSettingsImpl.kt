package com.sygic.driving.testapp.core.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.sygic.driving.VehicleType
import com.sygic.driving.testapp.core.utils.BatteryOptimizationState
import com.sygic.driving.testapp.core.utils.toBatteryOptimizationState
import com.sygic.driving.testapp.core.utils.toInt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "driving_test_app_settings"
)

class AppSettingsImpl(context: Context): AppSettings(context) {

    private val dataStore = context.dataStore

    private val prefAutomaticTripDetection = booleanPreferencesKey(keyAutomaticTripDetection)
    private val prefDeveloperMode = booleanPreferencesKey(keyDeveloperMode)
    private val prefMotionActivity = booleanPreferencesKey(keyMotionActivity)
    private val prefDisableDetectionBatteryLowerThan = intPreferencesKey(keyDisableDetectionBatteryLowerThan)
    private val prefDisableDetectionInPowerSaver = booleanPreferencesKey(keyDisableDetectionInPowerSaver)
    private val prefBatteryOptimization = intPreferencesKey(keyBatteryOptimization)
    private val prefAppVersion = stringPreferencesKey(keyAppVersion)
    private val prefUserId = stringPreferencesKey(keyUserId)
    private val prefEndTripsAutomatically = booleanPreferencesKey(keyEndTripsAutomatically)
    private val prefMinTripDuration = intPreferencesKey(keyMinTripDuration)
    private val prefMinTripLength = intPreferencesKey(keyMinTripLength)
    private val prefVehicleType = stringPreferencesKey(keyVehicleType)


    override val userId: Flow<String> = dataStore.data.map { preferences ->
        preferences[prefUserId] ?: defaultUserId
    }
    
    override val developerMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[prefDeveloperMode] ?: defaultDeveloperMode
    }
        
    override val automaticTripDetection: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[prefAutomaticTripDetection] ?: defaultAutomaticTripDetection
    }
        
    override val enableMotionActivity: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[prefMotionActivity] ?: defaultMotionActivity
    }
        
    override val disableDetectionIfBatteryLowerThan: Flow<Int> = dataStore.data.map { preferences ->
        preferences[prefDisableDetectionBatteryLowerThan] ?: defaultDisableDetectionBatteryLowerThan
    }
        
    override val disableDetectionInPowerSaver: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[prefDisableDetectionInPowerSaver] ?: defaultDisableDetectionInPowerSaver
    }
        
    override val batteryOptimizationState: Flow<BatteryOptimizationState> = dataStore.data.map { preferences ->
        (preferences[prefBatteryOptimization] ?: defaultBatteryOptimization).toBatteryOptimizationState()
    }
        
    override val endTripsAutomatically: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[prefEndTripsAutomatically] ?: defaultEndTripsAutomatically
    }
        
    override val minTripDurationSeconds: Flow<Int> = dataStore.data.map { preferences ->
        preferences[prefMinTripDuration] ?: defaultMinTripDuration
    }
        
    override val minTripLengthMeters: Flow<Int> = dataStore.data.map { preferences ->
        preferences[prefMinTripLength] ?: defaultMinTripLength
    }
        
    override val appVersion: Flow<String> = dataStore.data.map { preferences ->
        preferences[prefAppVersion] ?: defaultAppVersion
    }

    override val vehicleType: Flow<VehicleType> = dataStore.data.map { preferences ->
        (preferences[prefVehicleType] ?: defaultVehicleType).toVehicleType()
    }

    override suspend fun setUserId(userId: String) {
        dataStore.edit { preferences ->
            preferences[prefUserId] = userId
        }
    }

    override suspend fun setDeveloperMode(developerMode: Boolean) {
        dataStore.edit { preferences ->
            preferences[prefDeveloperMode] = developerMode
        }
    }

    override suspend fun setAutomaticTripDetection(automaticTripDetection: Boolean) {
        dataStore.edit { preferences ->
            preferences[prefAutomaticTripDetection] = automaticTripDetection
        }
    }

    override suspend fun setEnableMotionActivity(enableMotionActivity: Boolean) {
        dataStore.edit { preferences ->
            preferences[prefMotionActivity] = enableMotionActivity
        }
    }

    override suspend fun setDisableDetectionIfBatteryLowerThan(disableDetectionIfBatteryLowerThan: Int) {
        dataStore.edit { preferences ->
            preferences[prefDisableDetectionBatteryLowerThan] = disableDetectionIfBatteryLowerThan
        }
    }

    override suspend fun setDisableDetectionInPowerSaver(disableDetectionInPowerSaver: Boolean) {
        dataStore.edit { preferences ->
            preferences[prefDisableDetectionInPowerSaver] = disableDetectionInPowerSaver
        }
    }

    override suspend fun setBatteryOptimizationState(batteryOptimizationState: BatteryOptimizationState) {
        dataStore.edit { preferences ->
            preferences[prefBatteryOptimization] = batteryOptimizationState.toInt()
        }
    }

    override suspend fun setEndTripsAutomatically(endTripsAutomatically: Boolean) {
        dataStore.edit { preferences ->
            preferences[prefEndTripsAutomatically] = endTripsAutomatically
        }
    }

    override suspend fun setMinTripDurationSeconds(minTripDurationSeconds: Int) {
        dataStore.edit { preferences ->
            preferences[prefMinTripDuration] = minTripDurationSeconds
        }
    }

    override suspend fun setMinTripLengthMeters(minTripLengthMeters: Int) {
        dataStore.edit { preferences ->
            preferences[prefMinTripLength] = minTripLengthMeters
        }
    }

    override suspend fun setVehicleType(vehicleType: String) {
        dataStore.edit { preferences ->
            preferences[prefVehicleType] = vehicleType
        }
    }

    private fun String.toVehicleType(): VehicleType =
        when(this) {
            keyVehicleTypeTruck -> VehicleType.Truck
            else -> VehicleType.Car
        }
}