package com.sygic.driving.testapp.ui.local_trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sygic.driving.testapp.R
import com.sygic.driving.testapp.core.driving.DrivingManager
import com.sygic.driving.testapp.core.utils.Resource
import com.sygic.driving.testapp.domain.driving.model.DrivingTripStorage
import com.sygic.driving.testapp.domain.driving.use_case.DeleteTrip
import com.sygic.driving.testapp.domain.driving.use_case.GetLocalTrips
import com.sygic.driving.testapp.domain.driving.use_case.GetTripDevFiles
import com.sygic.driving.testapp.domain.driving.use_case.SimulateTrip
import com.sygic.driving.testapp.core.utils.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
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

    private val _uiEvents = Channel<UiEvent>()
    val uiEvents = _uiEvents.receiveAsFlow()

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
            _uiEvents.send(UiEvent.NavigateTo(navDir))
        }
    }

    fun onTripSimulate(tripId: String) {
        viewModelScope.launch {
            simulateTrip(tripId)
            _uiEvents.send(UiEvent.PopBackStack)
        }
    }

    fun onTripSend(tripId: String) {
        viewModelScope.launch {
            val files = getTripDeveloperFiles(tripId)
            if(files.isEmpty())
                _uiEvents.send(UiEvent.ShowToast(R.string.local_trip_send_failed))
            else
                _uiEvents.send(UiEvent.ShareFiles(files))
        }
    }

    fun onTripDelete(tripId: String) {
        viewModelScope.launch {
            deleteTrip(tripId)
            refreshLocalTrips()
        }
    }

}