package com.sygic.driving.testapp.ui.settings

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sygic.driving.testapp.core.settings.AppSettings
import com.sygic.driving.testapp.core.utils.SingleEvent
import com.sygic.driving.testapp.core.utils.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothDevicesViewModel @Inject constructor(
    private val appSettings: AppSettings
): ViewModel() {

    private val _uiEvent = SingleEvent<UiEvent>()
    val uiEvent = _uiEvent.flow

    private val _hasBluetoothPermission = MutableStateFlow<Boolean?>(null)
    val hasBluetoothPermission = _hasBluetoothPermission.asStateFlow()

    private val _startBluetoothScanEvent = SingleEvent<Unit>().apply {
        viewModelScope.launch {
            hasBluetoothPermission.filter { it == true }.collectLatest {
                emit(Unit)
            }
        }
    }
    val startBluetoothScanEvent: Flow<Unit> = _startBluetoothScanEvent.flow

    private val _scanRunning = MutableStateFlow(false)
    val scanRunning = _scanRunning.asStateFlow()

    private val _detectedDevices = mutableSetOf<BluetoothDevice>()
    val detectedDevices: List<BluetoothDevice>
        get() = _detectedDevices.toList()

    private val _devicesUpdatedEvent = SingleEvent<Unit>()
    val devicesUpdatedEvent = _devicesUpdatedEvent.flow

    fun onBluetoothPermissionResult(granted: Boolean) {
        _hasBluetoothPermission.value = granted
    }

    fun onStartScan() {
        if(_hasBluetoothPermission.value == true) {
            viewModelScope.launch {
                _startBluetoothScanEvent.emit(Unit)
            }
        }
    }

    fun onStartBluetoothScan() {
        viewModelScope.launch {
            _scanRunning.value = true
            _detectedDevices.clear()
            _devicesUpdatedEvent.emit(Unit)
        }
    }

    fun onStopBluetoothScan() {
        _scanRunning.value = false
    }

    fun onBluetoothDeviceDetected(device: BluetoothDevice) {
        viewModelScope.launch {
            _detectedDevices.add(device)
            _devicesUpdatedEvent.emit(Unit)
        }
    }

    @SuppressLint("MissingPermission")
    fun onDeviceSelected(device: BluetoothDevice) {
        viewModelScope.launch {
            appSettings.setBluetoothDongleName(device.name.orEmpty())
            appSettings.setBluetoothDongleAddress(device.address)
            _uiEvent.emit(UiEvent.PopBackStack)
        }
    }
}