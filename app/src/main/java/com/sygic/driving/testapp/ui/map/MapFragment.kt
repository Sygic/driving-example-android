package com.sygic.driving.testapp.ui.map

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.sygic.driving.testapp.R
import com.sygic.driving.testapp.core.utils.checkPermission
import com.sygic.driving.testapp.core.utils.launchAndRepeatWithViewLifecycle
import com.sygic.driving.testapp.databinding.FragmentMapBinding
import com.sygic.driving.testapp.ui.map.util.MapUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MapFragment: Fragment() {

    private val viewModel: MapViewModel by viewModels()
    private lateinit var binding: FragmentMapBinding

    private lateinit var googleMap: GoogleMap
    private val mapFragment: SupportMapFragment
        get() = childFragmentManager.findFragmentById(R.id.fragmentMap) as SupportMapFragment

    private var lastAddedSystemLocation: Location? = null
    private var lastAddedComputedLocation: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false).apply {
            checkSystemLocations.setOnClickListener {
                viewModel.onToggleSystemLocationsChecked()
            }
            checkComputedLocations.setOnClickListener {
                viewModel.onToggleComputedLocationsChecked()
            }
            fabFixGps.setOnClickListener { viewModel.onFixGpsClicked() }
        }
        mapFragment.getMapAsync { map ->
            googleMap = map.apply {
                if(requireContext().checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    isMyLocationEnabled = true
                    uiSettings.isMyLocationButtonEnabled = false
                }
                setOnCameraMoveStartedListener { reason ->
                    if(reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                        viewModel.setUserScrolling(true)
                    }
                }
                setOnCameraIdleListener {
                    viewModel.setUserScrolling(false)
                }
                setOnCameraMoveListener {
                    viewModel.onMapScrolled()
                }
            }
            viewModel.onMapReady()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        launchAndRepeatWithViewLifecycle {
            viewModel.systemLocationsChecked.collect {
                binding.checkSystemLocations.isChecked = it
            }
        }
        launchAndRepeatWithViewLifecycle {
            viewModel.computedLocationsChecked.collect {
                binding.checkComputedLocations.isChecked = it
            }
        }
        launchAndRepeatWithViewLifecycle {
            viewModel.isGpsFixed.collect { fixed ->
                binding.fabFixGps.setImageResource(
                    if (fixed) R.drawable.ic_gps_fixed else R.drawable.ic_gps_not_fixed
                )
            }
        }
        launchAndRepeatWithViewLifecycle {
            viewModel.showGpsLocation.collect {
                val zoom = googleMap.cameraPosition.zoom
                val newZoom = if(zoom < 10f) 15f else zoom
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    LatLng(it.latitude, it.longitude),
                    newZoom
                )
                googleMap.animateCamera(cameraUpdate)
            }
        }
        launchAndRepeatWithViewLifecycle {
            viewModel.showSystemLocationsPolyline.collectLatest {
                googleMap.addPolyline(MapUtils.createPolyline(it, MapUtils.PolylineType.System))
                lastAddedSystemLocation = it.lastOrNull()
            }
        }
        launchAndRepeatWithViewLifecycle {
            viewModel.appendSystemLocation.collect {
                lastAddedSystemLocation?.let { lastLocation ->
                    googleMap.addPolyline(
                        MapUtils.createPolyline(
                            listOf(lastLocation, it),
                            MapUtils.PolylineType.System
                        )
                    )
                }
                lastAddedSystemLocation = it
            }
        }
        launchAndRepeatWithViewLifecycle {
            viewModel.showComputedLocationsPolyline.collectLatest {
                googleMap.addPolyline(MapUtils.createPolyline(it, MapUtils.PolylineType.Computed))
                lastAddedComputedLocation = it.lastOrNull()
            }
        }
        launchAndRepeatWithViewLifecycle {
            viewModel.appendComputedLocation.collect {
                lastAddedComputedLocation?.let { lastLocation ->
                    googleMap.addPolyline(
                        MapUtils.createPolyline(
                            listOf(lastLocation, it),
                            MapUtils.PolylineType.Computed
                        )
                    )
                }
                lastAddedComputedLocation = it
            }
        }
        launchAndRepeatWithViewLifecycle {
            viewModel.clearMap.collect {
                googleMap.clear()
                lastAddedSystemLocation = null
                lastAddedComputedLocation = null
            }
        }
        launchAndRepeatWithViewLifecycle {
            viewModel.isTripRunning.collectLatest { isRunning ->
                binding.imgTripRunning.isVisible = false
                if(isRunning) {
                    animateBlinking(binding.imgTripRunning)
                }
            }
        }
        launchAndRepeatWithViewLifecycle {
            viewModel.isSimulationRunning.collectLatest { isRunning ->
                binding.imgSimulationRunning.isVisible = false
                if(isRunning) {
                    animateBlinking(binding.imgSimulationRunning)
                }
            }
        }
    }

    override fun onResume() {
        viewModel.redrawCurrentTrip()
        super.onResume()
    }

    override fun onDestroyView() {
        viewModel.onMapDestroyed()
        super.onDestroyView()
    }

    private suspend fun animateBlinking(view: View) {
        while(true) {
            view.isVisible = view.isVisible.not()
            delay(1000)
        }
    }
}