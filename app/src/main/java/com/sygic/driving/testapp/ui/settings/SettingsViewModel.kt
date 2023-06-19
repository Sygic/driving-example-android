package com.sygic.driving.testapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sygic.driving.core.external_device.ExternalDeviceFeature
import com.sygic.driving.testapp.core.driving.DrivingManager
import com.sygic.driving.testapp.core.driving.utils.externalDeviceVinFlow
import com.sygic.driving.testapp.core.settings.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val drivingManager: DrivingManager,
): ViewModel() {

    private var getVinJob: Job? = null

    private val _vin = MutableStateFlow<String?>(null)
    val vin = _vin.asStateFlow()

    init {
        requestVin()
    }

    private fun requestVin() {
        combine(
            drivingManager.bluetoothConnected,
            drivingManager.drivingInstance
        ) { btConnected, driving ->
            if(!btConnected || driving == null) {
                getVinJob?.cancel()
                getVinJob = null
            }
            else if(getVinJob == null) {
                getVinJob = viewModelScope.launch {
                    _vin.value = driving.externalDeviceVinFlow().first()
                    cancel()
                    getVinJob = null
                }
                driving.requestExternalDeviceFeature(ExternalDeviceFeature.VIN)
            }
        }.launchIn(viewModelScope)
    }
}