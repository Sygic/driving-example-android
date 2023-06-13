package com.sygic.driving.testapp.ui.realtime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sygic.driving.Driving
import com.sygic.driving.TripDiscardReason
import com.sygic.driving.data.TripEventType
import com.sygic.driving.data.TripState
import com.sygic.driving.testapp.*
import com.sygic.driving.testapp.core.driving.DrivingManager
import com.sygic.driving.testapp.domain.driving.model.DrivingTripEvent
import com.sygic.driving.testapp.core.settings.AppSettings
import com.sygic.driving.testapp.core.utils.WHILE_SUBSCRIBED_WITH_TIMEOUT
import com.sygic.driving.testapp.core.utils.millisToSeconds
import com.sygic.driving.testapp.domain.driving.use_case.EndTrip
import com.sygic.driving.testapp.domain.driving.use_case.StartTrip
import com.sygic.driving.testapp.core.utils.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class RealtimeViewModel @Inject constructor(
    private val drivingManager: DrivingManager,
    private val appSettings: AppSettings,
    private val startTrip: StartTrip,
    private val endTrip: EndTrip
) : ViewModel() {

    init {
        drivingManager.initIfNeeded()
    }

    val initState: StateFlow<Driving.InitState> = drivingManager.initState

    private val drivingInstance = drivingManager.drivingInstance

    private val _uiEvents: MutableSharedFlow<UiEvent> = MutableSharedFlow(0)
    val uiEvents = _uiEvents.asSharedFlow()

    val tripState = drivingManager.tripState.apply {
        // check for discarded trips
        mapNotNull { it.discardReason }
            .onEach { discardReason ->
                val resId = when(discardReason) {
                    TripDiscardReason.TooShortLength -> R.string.realtime_trip_discarded_distance
                    TripDiscardReason.TooShortDuration -> R.string.realtime_trip_discarded_duration
                }
                _uiEvents.emit(UiEvent.ShowToast(resId))
            }.launchIn(viewModelScope)
    }

    val detectorState = drivingManager.detectorState

    val acceleration: Flow<DrivingTripEvent> =
        drivingManager.events.filter { it.type == TripEventType.Acceleration }

    val braking: Flow<DrivingTripEvent> =
        drivingManager.events.filter { it.type == TripEventType.Braking }

    val cornering: Flow<DrivingTripEvent> =
        drivingManager.events.filter { it.type == TripEventType.Cornering }

    val distraction: Flow<DrivingTripEvent> =
        drivingManager.events.filter { it.type == TripEventType.Distraction }

    val harsh: Flow<DrivingTripEvent> =
        drivingManager.events.filter { it.type == TripEventType.Harsh }

    val speed: Flow<Float> = drivingManager.bluetoothDeviceSpeed
        //.map { it.speed }

    val tripStartTime: StateFlow<Date?> = drivingManager.tripState
        .map { it.startTime }
        .stateIn(viewModelScope, WHILE_SUBSCRIBED_WITH_TIMEOUT, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val tripDurationSeconds: Flow<Long?> = tripStartTime.flatMapLatest { startTime ->
        if (startTime == null)
            flowOf(null)
        else
            tripDurationCounterFlow()
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 1)

    @OptIn(ExperimentalCoroutinesApi::class)
    val distanceDriven: Flow<Double?> = drivingManager.tripState.flatMapLatest { tripState ->
        if (tripState.state == TripState.NotStarted)
            flowOf(null)
        else
            drivingManager.distanceDriven
    }

    val angle = drivingManager.angle.combine(tripState.map {it.state}) { angle, state ->
        if(state == TripState.NotStarted) null else angle
    }

    val altitude = combine(drivingManager.altitude, tripState) { altitude, tripState ->
        if(tripState.state != TripState.NotStarted) altitude else 0.0
    }

    val simulationRunning = drivingManager.simulationRunning

    val bluetoothConnected = drivingManager.bluetoothConnected
    val bluetoothDataTrafficEvent = drivingManager.bluetoothDataTrafficEvent

    fun onStartTrip() {
        viewModelScope.launch {
            if(startTrip() && !appSettings.endTripsAutomatically.first()) {
                _uiEvents.emit(UiEvent.ShowToast(R.string.realtime_warning_no_auto_trip_end))
            }
        }
    }

    fun onEndTrip() {
        viewModelScope.launch {
            endTrip()
        }
    }

    fun stopSimulation() {
        drivingInstance.value?.simulationManager?.stop()
    }

    private fun tripDurationCounterFlow(): Flow<Long> = flow {
        var startTime: Date? = tripStartTime.value
        while (startTime != null) {
            val now = Date()
            val duration = (now.time - startTime.time).millisToSeconds()
            emit(duration)

            kotlinx.coroutines.delay(1000L)
            startTime = tripStartTime.value
        }
    }
}