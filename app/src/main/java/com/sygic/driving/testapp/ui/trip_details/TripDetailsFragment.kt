package com.sygic.driving.testapp.ui.trip_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.sygic.driving.data.TripEventType
import com.sygic.driving.testapp.R
import com.sygic.driving.testapp.core.utils.*
import com.sygic.driving.testapp.databinding.FragmentTripDetailsBinding
import com.sygic.driving.testapp.domain.driving.model.DrivingTripDetails
import com.sygic.driving.testapp.domain.driving.model.DrivingTripEvent
import com.sygic.driving.testapp.domain.driving.model.DrivingTripSegment
import com.sygic.driving.testapp.ui.trip_details.utils.centerMap
import com.sygic.driving.testapp.ui.trip_details.utils.toGoogleMapMarker
import com.sygic.driving.testapp.ui.trip_details.utils.toGoogleMapPolyline
import com.sygic.driving.testapp.ui.trip_details.utils.toListOfProperties
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import java.util.*

@AndroidEntryPoint
class TripDetailsFragment : Fragment() {

    private lateinit var binding: FragmentTripDetailsBinding
    private val viewModel: TripDetailsViewModel by viewModels()

    private lateinit var googleMap: GoogleMap
    private val mapFragment: SupportMapFragment
        get() = childFragmentManager.findFragmentById(R.id.fragmentMap) as SupportMapFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTripDetailsBinding.inflate(inflater, container, false).apply {
            mapContainer.visibility = View.INVISIBLE
            listProperties.layoutManager = LinearLayoutManager(requireContext())
            backToTrip.setOnClickListener { viewModel.onBackToTrip() }
        }

        mapFragment.getMapAsync { map ->
            googleMap = map
            googleMap.setOnMarkerClickListener { marker ->
                viewModel.onEventClicked(marker.tag as DrivingTripEvent)
                true
            }
            viewModel.onMapReady()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // title
        launchAndRepeatWithViewLifecycle {
            combine(
                viewModel.tripDetails,
                viewModel.eventClicked
            ) { tripDetails, clickedEvent ->
                clickedEvent?.getTitle() ?: tripDetails.startTime.toTripName()
            }.collect { title ->
                binding.title.text = title
            }
        }

        // details
        launchAndRepeatWithViewLifecycle {
            combine(
                viewModel.tripDetails,
                viewModel.eventClicked
            ) { tripDetails, clickedEvent ->
                clickedEvent?.toListOfNameValues() ?: tripDetails.toListOfNameValues()
            }.collect { properties ->
                binding.listProperties.adapter = TripDetailsPropertiesAdapter(properties)
            }
        }

        // show/hide back to trip
        launchAndRepeatWithViewLifecycle {
            viewModel.eventClicked.collect { event ->
                binding.backToTrip.isVisible = event != null
            }
        }

        // trajectory on map
        launchAndRepeatWithViewLifecycle {
            viewModel.tripSegments.collect { segments ->
                showSegmentsOnMap(segments)
            }
        }

        // events on map
        launchAndRepeatWithViewLifecycle {
            viewModel.events.collect { events ->
                showEventsOnMap(events)
            }
        }

        // is refreshing
        launchAndRepeatWithViewLifecycle {
            viewModel.isRefreshing.collect {
                binding.progressRefreshing.isVisible = it
            }
        }

        // error
        launchAndRepeatWithViewLifecycle {
            viewModel.error.collect { error ->
                binding.error.isVisible = error != null
                binding.error.text = error ?: ""
            }
        }
    }

    override fun onDestroyView() {
        viewModel.onMapDestroyed()
        super.onDestroyView()
    }

    private fun Date.toTripName(): String {
        return requireContext().getStringFormat(R.string.trip_details_trip_name, formatDate())
    }

    private fun showSegmentsOnMap(segments: List<DrivingTripSegment>) {
        val polylines = segments.map { it.toGoogleMapPolyline() }
        polylines.forEach { polyline ->
            googleMap.addPolyline(polyline)
        }
        googleMap.centerMap(polylines)

        binding.mapContainer.apply {
            visibility = View.VISIBLE
            animate().alpha(1f)
        }
    }

    private fun showEventsOnMap(events: List<DrivingTripEvent>) {
        events.onEach { event ->
                val markerOptions = event.toGoogleMapMarker(event.getTitle()) ?: return@onEach
                val marker = googleMap.addMarker(markerOptions)
                marker?.tag = event
            }
    }

    private fun DrivingTripDetails.toListOfNameValues(): List<NameValueProperty> {
        return toListOfProperties(requireContext())
    }

    private fun DrivingTripEvent.toListOfNameValues(): List<NameValueProperty> {
        return toListOfProperties(requireContext())
    }

    private fun DrivingTripEvent.getTitle(): String? {
        val resId = when(type) {
            TripEventType.Acceleration -> R.string.trip_event_name_acceleration
            TripEventType.Braking -> R.string.trip_event_name_braking
            TripEventType.Cornering -> R.string.trip_event_name_cornering
            TripEventType.Distraction -> R.string.trip_event_name_distraction
            TripEventType.Speeding -> R.string.trip_event_name_speeding
            TripEventType.PotHole -> R.string.trip_event_name_pothole
            TripEventType.Harsh -> R.string.trip_event_name_harsh
            else -> return null
        }
        return getString(resId)
    }
}



