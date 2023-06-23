package com.sygic.driving.testapp.ui.server_trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sygic.driving.testapp.core.utils.Resource
import com.sygic.driving.testapp.core.utils.SingleEvent
import com.sygic.driving.testapp.core.utils.UiEvent
import com.sygic.driving.testapp.core.utils.WHILE_SUBSCRIBED_WITH_TIMEOUT
import com.sygic.driving.testapp.domain.driving.model.DrivingTripStorage
import com.sygic.driving.testapp.domain.driving.use_case.GetServerTrips
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServerTripsViewModel @Inject constructor(
    private val getServerTrips: GetServerTrips
): ViewModel() {

    private val _uiEvents = SingleEvent<UiEvent>()
    val uiEvents = _uiEvents.flow

    private val getTripsTrigger = MutableSharedFlow<Unit>(1)

    private val tripsResource = getTripsTrigger.flatMapLatest {
        getServerTrips()
    }.stateIn(viewModelScope, WHILE_SUBSCRIBED_WITH_TIMEOUT, Resource.Loading())

    val trips = tripsResource
        .filter { it is Resource.Success }
        .mapNotNull { it.data }

    val isRefreshing = tripsResource.map { it is Resource.Loading }

    val error = tripsResource
        .map { it.message }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    fun onSwipeRefresh() {
        viewModelScope.launch { getTripsTrigger.emit(Unit) }
    }

    fun onTripSelected(id: String) {
        viewModelScope.launch {
            val action = ServerTripsFragmentDirections.actionServerTripsFragmentToTripDetailsFragment(
                DrivingTripStorage.Server, id
            )
            _uiEvents.emit(UiEvent.NavigateTo(action))
        }
    }

    init {
        viewModelScope.launch { getTripsTrigger.emit(Unit) }
    }
}
