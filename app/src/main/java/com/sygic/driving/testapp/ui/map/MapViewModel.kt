package com.sygic.driving.testapp.ui.map

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sygic.driving.data.TripState
import com.sygic.driving.testapp.core.driving.DrivingManager
import com.sygic.driving.testapp.core.location.DriverLocationManager
import com.sygic.driving.testapp.core.utils.SingleEvent
import com.sygic.driving.testapp.domain.driving.model.DrivingTripState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val drivingManager: DrivingManager,
    private val locationManager: DriverLocationManager
) : ViewModel() {

    private val googleMapReady = MutableStateFlow(false)

    private val _isGpsFixed = MutableStateFlow(false)
    val isGpsFixed = _isGpsFixed.asStateFlow()

    private val _showSystemLocationsPolyline = SingleEvent<List<Location>>()
    val showSystemLocationsPolyline = flowWhenMapReady(_showSystemLocationsPolyline.flow)

    private val _showComputedLocationsPolyline = SingleEvent<List<Location>>()
    val showComputedLocationsPolyline = flowWhenMapReady(_showComputedLocationsPolyline.flow)

    private val _appendSystemLocation = SingleEvent<Location>()
    val appendSystemLocation = flowWhenMapReady(_appendSystemLocation.flow).apply {
        viewModelScope.launch {
            isGpsFixed.collectLatest { isGpsFixed ->
                if(isGpsFixed) {
                    onEach {
                        _showGpsLocation.emit(it)
                    }.collect()
                }
            }
        }
    }

    private val _appendComputedLocation = SingleEvent<Location>()
    val appendComputedLocation = flowWhenMapReady(_appendComputedLocation.flow).apply {
        viewModelScope.launch {
            combine(isGpsFixed, _systemLocationsChecked) { isGpsFixed, systemLocationsChecked ->
                isGpsFixed && !systemLocationsChecked
            }.collectLatest { shouldFollowGps ->
                if(shouldFollowGps) {
                    onEach {
                        _showGpsLocation.emit(it)
                    }.collect()
                }
            }
        }
    }

    private val _showGpsLocation = SingleEvent<Location>()
    val showGpsLocation = flowWhenMapReady(_showGpsLocation.flow)

    private val _systemLocationsChecked = MutableStateFlow(true)
    val systemLocationsChecked = _systemLocationsChecked.asStateFlow()

    private val _computedLocationsChecked = MutableStateFlow(true)
    val computedLocationsChecked = _computedLocationsChecked.asStateFlow()

    private val isUserScrolling = MutableStateFlow(false)

    private val _clearMap = SingleEvent<Unit>()
    val clearMap = flowWhenMapReady(_clearMap.flow).apply {
        viewModelScope.launch {
            drivingManager.tripState
                .filter { it.state == TripState.Started }
                .collect { _clearMap.emit(Unit) }
        }
    }

    val isSimulationRunning = drivingManager.simulationRunning.apply {
        viewModelScope.launch {
            // clear map when simulation is started
            filter { it }.collect { _clearMap.emit(Unit) }
        }
    }
    val isTripRunning = combine(isSimulationRunning, drivingManager.tripState) { isSimulationRunning, tripState ->
        tripState.state != TripState.NotStarted && !isSimulationRunning
    }

    init {
        // first location fix
        viewModelScope.launch {
            locationManager.requestLocationUpdates().first().let {
                _showGpsLocation.emit(it)
                _isGpsFixed.value = true
            }
        }
        // follow current GPS position when fixed, and no trip or simulation is running
        viewModelScope.launch {
            combine(
                isGpsFixed,
                drivingManager.simulationRunning,
                drivingManager.tripState,
                _systemLocationsChecked,
                _computedLocationsChecked
            ) { isGpsFixed, isSimulationRunning, tripState, systemChecked, computedChecked ->
                isGpsFixed && ((!isSimulationRunning && tripState.state == TripState.NotStarted) || (!systemChecked && !computedChecked))
            }.collectLatest { shouldFollowCurrentLocation ->
                if(shouldFollowCurrentLocation) {
                    locationManager.requestLocationUpdates().collect {
                        _showGpsLocation.emit(it)
                    }
                }
            }
        }
        // handle system/computed checkbox states
        viewModelScope.launch {
            var appendSystemLocationJob: Job? = null
            var appendComputedLocationJob: Job? = null
            combine(_systemLocationsChecked, _computedLocationsChecked) { showSystem, showComputed ->
                object {
                    val showSystem = showSystem
                    val showComputed = showComputed
                }
            }.collectLatest {
                appendSystemLocationJob?.cancel()
                appendComputedLocationJob?.cancel()
                _clearMap.emit(Unit)
                if(it.showSystem) {
                    _showSystemLocationsPolyline.emit(drivingManager.currentTripSystemLocations.value)
                    appendSystemLocationJob = launch {
                        drivingManager.systemLocation.filterNotNull()
                            .collect { location ->
                                _appendSystemLocation.emit(location)
                            }
                    }
                }
                if(it.showComputed) {
                    _showComputedLocationsPolyline.emit(drivingManager.currentTripComputedLocations.value)
                    appendComputedLocationJob = launch {
                        drivingManager.computedLocation.filterNotNull()
                            .collect { location ->
                                _appendComputedLocation.emit(location)
                            }
                    }
                }
            }
        }
    }

    fun onMapReady() {
        googleMapReady.value = true
    }

    fun onMapDestroyed() {
        googleMapReady.value = false
    }

    fun onToggleSystemLocationsChecked() {
        _systemLocationsChecked.value = _systemLocationsChecked.value.not()
    }

    fun onToggleComputedLocationsChecked() {
        _computedLocationsChecked.value = _computedLocationsChecked.value.not()
    }

    fun onFixGpsClicked() {
        _isGpsFixed.value = true
    }

    fun onMapScrolled() {
        if(isUserScrolling.value) {
            _isGpsFixed.value = false
        }
    }

    fun setUserScrolling(isScrolling: Boolean) {
        isUserScrolling.value = isScrolling
    }

    private fun <T> flowWhenMapReady(flow: Flow<T>): Flow<T> =
        googleMapReady.flatMapLatest { isReady ->
            if (isReady) flow else emptyFlow()
        }
}