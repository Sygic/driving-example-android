package com.sygic.driving.testapp.ui.local_trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sygic.driving.testapp.R
import com.sygic.driving.testapp.core.driving.DrivingManager
import com.sygic.driving.testapp.core.utils.Resource
import com.sygic.driving.testapp.core.utils.SingleEvent
import com.sygic.driving.testapp.core.utils.UiEvent
import com.sygic.driving.testapp.domain.driving.model.DrivingTripStorage
import com.sygic.driving.testapp.domain.driving.use_case.DeleteTrip
import com.sygic.driving.testapp.domain.driving.use_case.GetLocalTrips
import com.sygic.driving.testapp.domain.driving.use_case.GetTripDevFiles
import com.sygic.driving.testapp.domain.driving.use_case.SimulateTrip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocalTripsViewModel @Inject constructor(
    drivingManager: DrivingManager,
    private val getLocalTrips: GetLocalTrips,
    private val simulateTrip: SimulateTrip,
    private val deleteTrip: DeleteTrip,
    private val getTripDeveloperFiles: GetTripDevFiles
): ViewModel() {

    private val refreshTripsTrigger: MutableSharedFlow<Unit> = MutableSharedFlow(replay = 1)

    private val _uiEvents = SingleEvent<UiEvent>()
    val uiEvents = _uiEvents.flow

    private val localTripsResource = combine(
        drivingManager.drivingInstance,
        refreshTripsTrigger
    ) { driving, _ ->
        driving
    }
        .filterNotNull()
        .flatMapLatest { driving ->
            getLocalTrips(driving)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), Resource.Loading())

    val isRefreshing = localTripsResource.map { it is Resource.Loading }

    val trips = localTripsResource.mapNotNull { it.data }

    init {
        drivingManager.initIfNeeded()
        refreshLocalTrips()
    }

    fun refreshLocalTrips() {
        viewModelScope.launch { refreshTripsTrigger.emit(Unit) }
    }

    fun onTripSelected(tripid: String) {
        viewModelScope.launch {
            val navDir = LocalTripsFragmentDirections
                .actionLocalTripsFragmentToTripDetailsFragment(
                    DrivingTripStorage.Local, tripid
                )
            _uiEvents.emit(UiEvent.NavigateTo(navDir))
        }
    }

    fun onTripSimulate(tripId: String) {
        viewModelScope.launch {
            simulateTrip(tripId)
            _uiEvents.emit(UiEvent.PopBackStack)
        }
    }

    fun onTripSimulateFast(tripId: String) {
        viewModelScope.launch {
            simulateTrip(tripId, playbackSpeed = 4.0f)
            _uiEvents.emit(UiEvent.PopBackStack)
        }
    }

    fun onTripSend(tripId: String) {
        viewModelScope.launch {
            val files = getTripDeveloperFiles(tripId)
            if(files.isEmpty())
                _uiEvents.emit(UiEvent.ShowToast(R.string.local_trip_send_failed))
            else
                _uiEvents.emit(UiEvent.ShareFiles(files))
        }
    }

    fun onTripDelete(tripId: String) {
        viewModelScope.launch {
            deleteTrip(tripId)
            refreshLocalTrips()
        }
    }

}