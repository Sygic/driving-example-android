package com.sygic.driving.testapp.ui.settings

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.preference.*
import com.sygic.driving.VehicleType
import com.sygic.driving.testapp.R
import com.sygic.driving.testapp.core.settings.AppSettings
import com.sygic.driving.testapp.core.utils.BatteryOptimizationState
import com.sygic.driving.testapp.core.utils.getDeviceName
import com.sygic.driving.testapp.core.utils.getStringFormat
import com.sygic.driving.testapp.core.utils.launchAndRepeatWithViewLifecycle
import com.sygic.driving.testapp.core.utils.openBatteryOptimizationSettings
import com.sygic.driving.testapp.core.utils.shareFile
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment: PreferenceFragmentCompat() {

    @Inject
    lateinit var appSettings: AppSettings

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindSettings()
    }

    private fun bindSettings() {
        // automatic trip detection
        switchPref(appSettings.keyAutomaticTripDetection)?.let { prefTripDetection ->
            launchAndRepeatWithViewLifecycle {
                appSettings.automaticTripDetection.collect {
                    prefTripDetection.isChecked = it
                }
            }
            prefTripDetection.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    (newValue as? Boolean)?.let { enableTripDetection ->
                        launch { appSettings.setAutomaticTripDetection(enableTripDetection) }
                    }
                    true
                }
        }

        // motion activity
        switchPref(appSettings.keyMotionActivity)?.let { prefMotionActivity ->
            launchAndRepeatWithViewLifecycle {
                appSettings.enableMotionActivity.collect {
                    prefMotionActivity.isChecked = it
                }
            }
            prefMotionActivity.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    (newValue as? Boolean)?.let { motionActivity ->
                        launch { appSettings.setEnableMotionActivity(motionActivity) }
                    }
                    true
                }
        }

        // developer mode
        switchPref(appSettings.keyDeveloperMode)?.let { prefDeveloperMode ->
            launchAndRepeatWithViewLifecycle {
                appSettings.developerMode.collect {
                    prefDeveloperMode.isChecked = it
                }
            }
            prefDeveloperMode.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    (newValue as? Boolean)?.let { developerMode ->
                        launch { appSettings.setDeveloperMode(developerMode) }
                    }
                    true
                }
        }

        // battery threshold
        seekBarPref(appSettings.keyDisableDetectionBatteryLowerThan)?.let { prefBatteryThreshold ->
            launchAndRepeatWithViewLifecycle {
                appSettings.disableDetectionIfBatteryLowerThan.collect {
                    prefBatteryThreshold.summary =
                        requireContext().getStringFormat(R.string.settings_battery_threshold_desc, it)
                }
            }
            prefBatteryThreshold.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    (newValue as? Int)?.let {
                        launch { appSettings.setDisableDetectionIfBatteryLowerThan(it) }
                    }
                    true
                }
        }

        // disable in power saver
        switchPref(appSettings.keyDisableDetectionInPowerSaver)?.let { prefDisableInPowerSaver ->
            launchAndRepeatWithViewLifecycle {
                appSettings.disableDetectionInPowerSaver.collect {
                    prefDisableInPowerSaver.isChecked = it
                }
            }
            prefDisableInPowerSaver.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    (newValue as? Boolean)?.let {
                        launch { appSettings.setDisableDetectionInPowerSaver(it) }
                    }

                    true
                }
        }

        // battery optimization
        pref(appSettings.keyBatteryOptimization)?.let { prefBatteryOptimization ->
            launchAndRepeatWithViewLifecycle {
                appSettings.batteryOptimizationState.collect { batteryOptimizationState ->
                    prefBatteryOptimization.summary = getString(
                        when (batteryOptimizationState) {
                            BatteryOptimizationState.Unknown -> R.string.settings_battery_optimization_unknown
                            BatteryOptimizationState.Enabled -> R.string.settings_battery_optimization_enabled
                            BatteryOptimizationState.Disabled -> R.string.settings_battery_optimization_disabled
                        }
                    )
                    prefBatteryOptimization.onPreferenceClickListener =
                        if(batteryOptimizationState == BatteryOptimizationState.Enabled) {
                            Preference.OnPreferenceClickListener {
                                val context = requireContext()
                                if (context.openBatteryOptimizationSettings())
                                    Toast.makeText(
                                        context,
                                        R.string.settings_battery_optimization_dialog_open_failed,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                true
                            }
                        }
                        else null   // not clickable if optimization disabled
                }
            }
        }

        // user id
        editTextPref(appSettings.keyUserId)?.let { prefUserId ->
            launchAndRepeatWithViewLifecycle {
                appSettings.userId.collect {
                    prefUserId.summary = it
                }
            }
            prefUserId.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    (newValue as? String)?.let {
                        launch { appSettings.setUserId(it) }
                    }
                    true
                }

            prefUserId.setOnBindEditTextListener { edit ->
                edit.setText(prefUserId.summary)
                edit.selectAll()
            }
        }

        // min trip length
        seekBarPref(appSettings.keyMinTripLength)?.let { prefMinTripLength ->
            launchAndRepeatWithViewLifecycle {
                appSettings.minTripLengthMeters.collect {
                    prefMinTripLength.summary =
                        requireContext().getStringFormat(R.string.settings_minimal_trip_length_desc, it)
                }
            }

            prefMinTripLength.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                (newValue as? Int)?.let {
                    launch { appSettings.setMinTripLengthMeters(it) }
                }
                true
            }
        }

        // min trip duration
        seekBarPref(appSettings.keyMinTripDuration)?.let { prefMinTripDuration ->
            launchAndRepeatWithViewLifecycle {
                appSettings.minTripDurationSeconds.collect {
                    prefMinTripDuration.summary =
                        requireContext().getStringFormat(R.string.settings_minimal_trip_duration_desc, it)
                }
            }

            prefMinTripDuration.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                (newValue as? Int)?.let {
                    launch { appSettings.setMinTripDurationSeconds(it) }
                }
                true
            }
        }

        // end trips automatically
        switchPref(appSettings.keyEndTripsAutomatically)?.let { prefEndTripsAuto ->
            launchAndRepeatWithViewLifecycle {
                appSettings.endTripsAutomatically.collect {
                    prefEndTripsAuto.isChecked = it
                }
            }
            prefEndTripsAuto.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    (newValue as? Boolean)?.let {
                        launch { appSettings.setEndTripsAutomatically(it) }
                    }
                    true
                }
        }

        dropDownPref(appSettings.keyVehicleType)?.let { prefVehicleType ->
            launchAndRepeatWithViewLifecycle {
                appSettings.vehicleType.collect {
                    prefVehicleType.summary = it.string()
                }
            }
            prefVehicleType.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    (newValue as? String)?.let {
                        launch { appSettings.setVehicleType(it) }
                    }
                    true
                }
        }

        // send logs
        pref(appSettings.keySendLogs)?.let { pref ->
            pref.setOnPreferenceClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    shareDrivingLog()
                }
                true
            }
        }

        // app version
        pref(appSettings.keyAppVersion)?.let { prefAppVersion ->
            launchAndRepeatWithViewLifecycle {
                appSettings.appVersion.collect {
                    prefAppVersion.summary = it
                }
            }
        }

    }

    private suspend fun shareDrivingLog() {
        val drivingDir = File(requireContext().getExternalFilesDir(null), "Driving")
        val logFile = File(drivingDir, "androidLog.txt")
        if(logFile.exists()) {
            requireContext().shareFile(
                logFile,
                getString(R.string.settings_send_log_message_subject),
                getShareDrivingLogBody()
            )
        }
    }

    private suspend fun getShareDrivingLogBody(): String {
        with(StringBuilder()) {
            // user id
            append(getString(R.string.settings_send_log_message_body_user_id))
            append(" ")
            append(appSettings.userId.first())
            append("\n")
            // app version
            append(getString(R.string.settings_send_log_message_body_app_version))
            append(" ")
            append(appSettings.appVersion.first())
            append("\n")
            // android version
            append(getString(R.string.settings_send_log_message_body_android_version))
            append(" ")
            append(Build.VERSION.SDK_INT)
            append("\n")
            // device name
            append(getString(R.string.settings_send_log_message_body_device_name))
            append(" ")
            append(getDeviceName())
            append("\n")

            return this.toString()
        }
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) {
        lifecycleScope.launch(block = block)
    }

    private fun VehicleType.string(): String =
        requireContext().getString(
            when (this) {
                VehicleType.Truck -> R.string.settings_vehicle_type_truck
                else -> R.string.settings_vehicle_type_car
            }
        )
}

private fun PreferenceFragmentCompat.pref(key: String) = findPreference<Preference>(key)
private fun PreferenceFragmentCompat.switchPref(key: String) = findPreference<SwitchPreference>(key)
private fun PreferenceFragmentCompat.seekBarPref(key: String) = findPreference<SeekBarPreference>(key)
private fun PreferenceFragmentCompat.editTextPref(key: String) = findPreference<EditTextPreference>(key)
private fun PreferenceFragmentCompat.dropDownPref(key: String) = findPreference<DropDownPreference>(key)