package com.sygic.driving.testapp.ui.realtime

import android.Manifest
import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sygic.driving.Driving
import com.sygic.driving.data.DetectorState
import com.sygic.driving.data.TripState
import com.sygic.driving.testapp.R
import com.sygic.driving.testapp.domain.driving.model.DrivingTripEvent
import com.sygic.driving.testapp.core.utils.*
import com.sygic.driving.testapp.databinding.FragmentRealtimeBinding
import com.sygic.driving.testapp.core.utils.UiEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.*


@AndroidEntryPoint
class RealtimeFragment : Fragment() {

    private val viewModel: RealtimeViewModel by viewModels()

    private lateinit var binding: FragmentRealtimeBinding

    private val drivingPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) listOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACTIVITY_RECOGNITION,
        Manifest.permission.POST_NOTIFICATIONS
    )
    else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) listOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACTIVITY_RECOGNITION
    )
    else listOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private val requestDrivingPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            // Driving library can run without permissions, so we ignore the result, but normally
            // you need to ensure at least location permission in order to work properly.
        }

    private val requestBgLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted)
                hideBgPermissionWarning()
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRealtimeBinding.inflate(inflater).apply {

            btnStartTrip.setOnClickListener { viewModel.onStartTrip() }
            btnEndTrip.setOnClickListener { viewModel.onEndTrip() }

            btnStopSimulation.setOnClickListener { viewModel.stopSimulation() }

            // FIXME fix reading of LiveValueViews from XML
            liveAltitude.setDescription(R.string.realtime_altitude)
            liveDistance.setDescription(R.string.realtime_distance_driven)
            liveStartTime.setDescription(R.string.realtime_start_time)
            liveSpeed.setDescription(R.string.realtime_speed)
            liveDuration.setDescription(R.string.realtime_trip_duration)

            btnGrant.setOnClickListener { requestBackgroundLocationPermission() }
        }

        bindViewModel()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkDrivingPermissions()
    }

    private fun bindViewModel() {
        // UI background from init, trip and detector state
        launchAndRepeatWithViewLifecycle {
            combine(
                viewModel.initState,
                viewModel.tripState,
                viewModel.detectorState
            ) { initState, tripState, detectorState ->
                when {
                    initState !is Driving.InitState.Initialized -> Color.GRAY
                    tripState.state == TripState.NotStarted -> Color.TRANSPARENT
                    else -> when (detectorState) {
                        DetectorState.Disoriented -> Color.RED
                        DetectorState.HasGravity -> Color.YELLOW
                        DetectorState.Oriented -> Color.GREEN
                    }
                }
            }.collect { bgColor ->
                binding.layoutContainer.setBackgroundColor(bgColor)
            }
        }

        // error if not initialized
        launchAndRepeatWithViewLifecycle {
            viewModel.initState.collect { initState ->
                when (initState) {
                    Driving.InitState.Initialized, Driving.InitState.Uninitialized ->
                        hideError()
                    is Driving.InitState.IncompatibleHardware ->
                        showError(R.string.error_driving_incompatible_hardware)
                    is Driving.InitState.InvalidLicense ->
                        showError(R.string.error_driving_invalid_license)
                    is Driving.InitState.OtherError ->
                        showError(R.string.realtime_error_driving_other_error)
                }
            }
        }

        // start trip button enabled
        launchAndRepeatWithViewLifecycle {
            combine(viewModel.tripState, viewModel.initState) { tripState, initState ->
                initState == Driving.InitState.Initialized && tripState.state != TripState.Started
            }.collect { enabled ->
                binding.btnStartTrip.isEnabled = enabled
            }
        }

        // end trip button enabled
        launchAndRepeatWithViewLifecycle {
            combine(viewModel.tripState, viewModel.initState) { tripState, initState ->
                initState == Driving.InitState.Initialized && tripState.state == TripState.Started
            }.collect { enabled ->
                binding.btnEndTrip.isEnabled = enabled
            }
        }

        // reset event values when trip ended
        launchAndRepeatWithViewLifecycle {
            viewModel.tripState
                .map { it.state }
                .filter { it == TripState.NotStarted }
                .collect {
                    adjustEventViews(null, binding.tvTitleA, binding.tvA)
                    adjustEventViews(null, binding.tvTitleB, binding.tvB)
                    adjustEventViews(null, binding.tvTitleC, binding.tvC)
                    adjustEventViews(null, binding.tvTitleD, binding.tvD)
                    adjustEventViews(null, binding.tvTitleH, binding.tvH)
                }
        }

        // trip status
        launchAndRepeatWithViewLifecycle {
            viewModel.tripState.collect { tripState ->
                binding.tvIsInTrip.text = when (tripState.state) {
                    TripState.Started -> getString(R.string.realtime_in_trip).uppercase()
                    TripState.NotStarted -> getString(R.string.realtime_not_in_trip)
                    TripState.PossiblyStarted -> getString(R.string.realtime_possibly_in_trip)
                }
            }
        }

        // detector angle
        launchAndRepeatWithViewLifecycle {
            viewModel.angle.collect { angle ->
                binding.tvAngle.text = angle?.radToDeg()?.format(1) ?: ""
            }
        }

        // EVENTS
        launchAndRepeatWithViewLifecycle {
            viewModel.acceleration
                .collect { acceleration ->
                    adjustEventViews(acceleration, binding.tvTitleA, binding.tvA)
                }
        }

        launchAndRepeatWithViewLifecycle {
            viewModel.braking.collect { braking ->
                adjustEventViews(braking, binding.tvTitleB, binding.tvB)
            }
        }

        launchAndRepeatWithViewLifecycle {
            viewModel.cornering.collect { cornering ->
                adjustEventViews(cornering, binding.tvTitleC, binding.tvC)
            }
        }

        launchAndRepeatWithViewLifecycle {
            viewModel.distraction.collect { distraction ->
                adjustEventViews(distraction, binding.tvTitleD, binding.tvD)
            }
        }

        launchAndRepeatWithViewLifecycle {
            viewModel.harsh.collect { harsh ->
                adjustEventViews(harsh, binding.tvTitleH, binding.tvH)
            }
        }

        launchAndRepeatWithViewLifecycle {
            viewModel.speed.map { value ->
                if (value < 0.0f) 0.0f else value.mpsToKph()
            }.collect { speed ->
                binding.liveSpeed.value = speed.toInt().toString()
            }
        }

        // LIVE VALUES
        // altitude
        launchAndRepeatWithViewLifecycle {
            viewModel.altitude
                .map { it.format(2) }
                .collect { textValue ->
                    binding.liveAltitude.value = textValue
                }
        }

        // trip start time
        launchAndRepeatWithViewLifecycle {
            viewModel.tripStartTime
                .map { formattedDateOrNoValue(it) }
                .collect { textValue ->
                    binding.liveStartTime.value = textValue
                }
        }

        // trip duration
        launchAndRepeatWithViewLifecycle {
            viewModel.tripDurationSeconds
                .onEach { it?.times(1000L) }
                .map { formattedDurationOrNoValue(it) }
                .collect { textValue ->
                    binding.liveDuration.value = textValue
                }
        }

        // distance driven
        launchAndRepeatWithViewLifecycle {
            viewModel.distanceDriven
                .map { value ->
                    if (value != null) value / 1000.0 else null
                }
                .map { formattedDoubleOrNoValue(it, 3) }
                .collect { textValue ->
                    binding.liveDistance.value = textValue
                }
        }

        // is simulation running
        launchAndRepeatWithViewLifecycle {
            viewModel.simulationRunning.collect { isSimulationRunning ->
                binding.btnStopSimulation.visibility =
                    if (isSimulationRunning) View.VISIBLE else View.GONE
            }
        }

        // warning trip will not end automatically
        launchAndRepeatWithViewLifecycle {
            viewModel.uiEvents.collect { event ->
                when(event) {
                    is UiEvent.ShowToast ->
                        Toast.makeText(requireContext(), event.resId, Toast.LENGTH_LONG).show()
                    else -> Unit
                }
            }
        }
    }

    override fun onResume() {
        val bgPermissionMissing = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_DENIED

        if (bgPermissionMissing)
            showBgPermissionWarning()
        else
            hideBgPermissionWarning()

        super.onResume()
    }

    private fun adjustEventViews(
        event: DrivingTripEvent?,
        textViewTitle: TextView,
        textViewValue: TextView
    ) {
        if (event?.active == true) {
            textViewValue.text = event.currentSize?.format(2) ?: ""
            textViewTitle.alpha = 1.0f
        } else {
            textViewValue.text = ""
            textViewTitle.alpha = 0.5f
        }
    }

    private fun showError(@StringRes resId: Int) {
        binding.error.apply {
            text = requireContext().getStringFormat(
                R.string.realtime_error_driving_not_initialized, getString(resId)
            )
            visibility = View.VISIBLE
        }
    }

    private fun hideError() {
        binding.error.visibility = View.GONE
    }

    private fun showBgPermissionWarning() {
        binding.warning.visibility = View.VISIBLE
    }

    private fun hideBgPermissionWarning() {
        binding.warning.visibility = View.GONE
    }

    private fun checkDrivingPermissions() {
        val notGrantedPermissions = drivingPermissions.filterNot { permission ->
            isPermissionGranted(permission)
        }
        if (notGrantedPermissions.isNotEmpty()) {
            requestDrivingPermissions.launch(notGrantedPermissions.toTypedArray())
        }
    }

    @TargetApi(30)
    private fun requestBackgroundLocationPermission() {
        if (!isPermissionGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            AlertDialog.Builder(context)
                .setTitle(R.string.realtime_bg_permission_dialog_title)
                .setMessage(R.string.realtime_bg_permission_dialog_text)
                .setPositiveButton(R.string.button_grant) { _, _ ->
                    requestBgLocationPermission.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
                .setNegativeButton(R.string.button_dismiss) { _, _ -> }
                .create()
                .show()
        }
    }

}
