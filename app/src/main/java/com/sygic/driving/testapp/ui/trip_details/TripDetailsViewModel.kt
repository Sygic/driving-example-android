package com.sygic.driving.testapp.ui.trip_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sygic.driving.testapp.core.utils.Resource
import com.sygic.driving.testapp.core.utils.WHILE_SUBSCRIBED_WITH_TIMEOUT
import com.sygic.driving.testapp.domain.driving.model.DrivingTripDetails
import com.sygic.driving.testapp.domain.driving.model.DrivingTripEvent
import com.sygic.driving.testapp.domain.driving.model.DrivingTripSegment
import com.sygic.driving.testapp.domain.driving.model.DrivingTripStorage
import com.sygic.driving.testapp.domain.driving.use_case.GetLocalTripDetails
import com.sygic.driving.testapp.domain.driving.use_case.GetServerTripDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripDetailsViewModel @Inject constructor(
    getLocalTripDetails: GetLocalTripDetails,
    getServerTripDetails: GetServerTripDetails,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId = savedStateHandle.get<String>("tripId") ?: ""
    private val tripStorage = savedStateHandle.get<DrivingTripStorage>("storage") ?: DrivingTripStorage.Local

    private val googleMapReady = MutableStateFlow(false)
    
    private val tripDetailsResource: StateFlow<Resource<DrivingTripDetails>> =
        when(tripStorage) {
            DrivingTripStorage.Local -> getLocalTripDetails(tripId)
            DrivingTripStorage.Server -> getServerTripDetails(tripId)
        }.stateIn(viewModelScope, WHILE_SUBSCRIBED_WITH_TIMEOUT, Resource.Loading())

    @OptIn(ExperimentalCoroutinesApi::class)
    val tripDetails = tripDetailsResource.mapNotNull { it.data }

    val events = tripDetails
        .map { it.events }
        .combine(googleMapReady) { events, mapReady ->
            if(mapReady) events else emptyList()
        }

    val isRefreshing = tripDetailsResource.map { it is Resource.Loading }

    val error = tripDetailsResource.map { it.message }

    val tripSegments: SharedFlow<List<DrivingTripSegment>> =
        combine(
            tripDetailsResource.mapNotNull { it.data },
            googleMapReady
        ) { tripDetails, mapReady ->
            if(mapReady) tripDetails else null
        }
            .mapNotNull { it?.segments }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)


    fun onMapReady() {
        googleMapReady.value = true
    }

    fun onMapDestroyed() {
        googleMapReady.value = false
    }

    private val _eventClicked: MutableStateFlow<DrivingTripEvent?> = MutableStateFlow(null)
    val eventClicked = _eventClicked.asStateFlow()

    fun onEventClicked(event: DrivingTripEvent) {
        _eventClicked.value = event
    }

    fun onBackToTrip() {
        _eventClicked.value = null
    }
}