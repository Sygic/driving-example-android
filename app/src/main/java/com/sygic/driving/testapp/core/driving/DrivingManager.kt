package com.sygic.driving.testapp.core.driving

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import com.sygic.driving.*
import com.sygic.driving.data.DetectorState
import com.sygic.driving.licensing.SygicLicense
import com.sygic.driving.testapp.domain.driving.model.DrivingTripEvent
import com.sygic.driving.testapp.domain.driving.model.DrivingTripState
import com.sygic.driving.testapp.core.driving.utils.*
import com.sygic.driving.testapp.core.platform.notification.NotificationProvider
import com.sygic.driving.testapp.core.settings.AppSettings
import com.sygic.driving.testapp.core.utils.Constants
import com.sygic.driving.testapp.core.utils.WHILE_SUBSCRIBED_WITH_TIMEOUT
import com.sygic.driving.testapp.core.utils.countryIso
import com.sygic.driving.testapp.core.utils.toPercent
import com.sygic.driving.trips.LocalTripsPolicy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Class that adapts Driving library. Initializes/deinitializes library,
 * provides Driving instance and Driving callbacks as flows.
 *
 * To call methods from Driving library, use [Driving] instance from [drivingInstance] flow.
 */
class DrivingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appSettings: AppSettings,
    private val notificationProvider: NotificationProvider
) {

    private val active: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val scope = ProcessLifecycleOwner.get().lifecycleScope
    private val jobs = mutableListOf<Job>()

    private val _initState: MutableStateFlow<Driving.InitState> = MutableStateFlow(Driving.initState)

    /**
     * Provides initializationState: [Driving.InitState] of Driving library.
     */
    val initState = _initState.asStateFlow()

    private val userId: StateFlow<String?> = appSettings.userId
        .stateIn(scope, SharingStarted.WhileSubscribed(), null)

    /**
     * Provides [Driving] instance to be used for calling methods from Driving library.
     * [initIfNeeded] should be called when collecting this flow, e.g. in ViewModel constructor.
     */
    val drivingInstance: StateFlow<Driving?> = active.combine(userId) { active, user ->
        // (re)initialize when activated or if the user ID has changed
        deinitializeInternal()
        user ?: return@combine null
        return@combine if(!active) null else initialize()
    }.stateIn(scope, SharingStarted.Eagerly, null)

    /**
     * Initializes Driving library with configuration from [AppSettings]. If library
     * is already initialized, nothing happens.
     *
     * After this method is called, every time [AppSettings.userId] has changed, library will reinitialize
     * and [drivingInstance] flow will emit a new instance.
     */
    fun initIfNeeded() {
        active.value = true
    }

    /** Call to deactivate Driving functionality, e.g. when user logs out. */
    fun deinitialize() {
        active.value = false
    }

    // Driving callbacks as flows
    val detectorState: StateFlow<DetectorState> = drivingFlow { driving ->
        driving.detectorStateFlow()
    }.stateIn(scope, WHILE_SUBSCRIBED_WITH_TIMEOUT, DetectorState.Disoriented)

    val angle: Flow<Double> = drivingFlow { driving -> driving.angleFlow() }

    val tripState: StateFlow<DrivingTripState> = drivingFlow { driving ->
        driving.tripStateFlow()
    }.stateIn(scope, WHILE_SUBSCRIBED_WITH_TIMEOUT, DrivingTripState())

    val events: SharedFlow<DrivingTripEvent> = drivingFlow { driving ->
        driving.eventsFlow()
    }.shareIn(scope, WHILE_SUBSCRIBED_WITH_TIMEOUT, 1)

    val simulationRunning: StateFlow<Boolean> = drivingFlow { driving ->
        driving.simulationManager.isSimulationRunningLiveData.asFlow()
    }.stateIn(scope, WHILE_SUBSCRIBED_WITH_TIMEOUT, false)

    val distanceDriven: SharedFlow<Double> = drivingFlow { driving ->
        driving.distanceDrivenFlow()
    }.shareIn(scope, WHILE_SUBSCRIBED_WITH_TIMEOUT, 1)

    val altitude: StateFlow<Double> = drivingFlow { driving ->
        driving.altitudeFlow()
    }.stateIn(scope, WHILE_SUBSCRIBED_WITH_TIMEOUT, 0.0)

    val passiveLocation: StateFlow<Location> = drivingFlow { driving ->
        driving.passiveLocationFlow()
    }.stateIn(scope, WHILE_SUBSCRIBED_WITH_TIMEOUT, Location(""))

    private suspend fun initialize(): Driving? {

        val initResult = Driving.initialize(getInitializer())
        _initState.value = initResult

        return if (initResult == Driving.InitState.Initialized) {
            startCollectingSettings()
            Driving.getInstance()
        }
        else
            null
    }

    private fun deinitializeInternal() {
        _initState.value = Driving.InitState.Uninitialized

        cancelAllJobs()

        if(Driving.isInitialized)
            Driving.deinitialize()
    }

    private suspend fun getTripValidityCriteria(): TripValidityCriteria {
        return TripValidityCriteria(
            minimalTripDurationSeconds = appSettings.minTripDurationSeconds.first(),
            minimalTripLengthMeters = appSettings.minTripLengthMeters.first()
        )
    }

    private suspend fun getConfiguration(): Configuration {
        return Configuration.Builder()
            .sendOnMobileData(true)
            .disableTripDetectionInLowPowerMode(appSettings.disableDetectionInPowerSaver.first())
            .disableTripDetectionIfBatteryIsLowerThan(
                appSettings.disableDetectionIfBatteryLowerThan.first().toPercent())
            .disableMotionActivity(!appSettings.enableMotionActivity.first())
            .tripValidityCriteria(getTripValidityCriteria())
            .localTripsPolicy(LocalTripsPolicy.Enabled)
            .tripEndTimeout(TimeUnit.MINUTES.toSeconds(5).toInt())
            .drivingServerUrl(Constants.DRB_SERVER_DEVICE_URL)
            .build()
    }

    private fun getVehicleSettings(): VehicleSettings {
        return VehicleSettings.Builder()
            .vehicleType(VehicleType.Car)
            .maxSpeedKph(130)
            .build()
    }

    private suspend fun getInitializer(): Driving.Initializer {
        if(Constants.SYGIC_LICENSE.isEmpty()) {
            Log.e("Driving", "Sygic license is not set! Please set `sygic.license` in your local.properties file.")
        }

        return Driving.Initializer(
            context = context,
            clientId = Constants.DRIVING_CLIENT_ID,
            userId = appSettings.userId.first(),
            license = SygicLicense.KeyString(Constants.SYGIC_LICENSE),
            configuration = getConfiguration(),
            vehicleSettings = getVehicleSettings(),
            notificationProvider = notificationProvider,
            countryIso = context.countryIso,
            developerMode = appSettings.developerMode.first(),
            loggingLevel = LogSeverity.Debug,
            allowNoGyroMode = true,
            sygicAuth = SygicAuthConfig.Default(
                authUrl = Constants.AUTH_URL)
        )
    }

    private fun startCollectingSettings() {
        cancelAllJobs()

        // change configuration
        val configJob = scope.launch {
            combine(
                appSettings.disableDetectionIfBatteryLowerThan,
                appSettings.disableDetectionInPowerSaver,
                appSettings.minTripDurationSeconds,
                appSettings.minTripLengthMeters,
                appSettings.enableMotionActivity
            ) { _ ->
            }
                .collect {
                    drivingInstance.value?.configuration = getConfiguration()
                }
        }
        jobs.add(configJob)

        // change developer mode
        val developerModeJob = scope.launch {
            appSettings.developerMode.collect { developerMode ->
                drivingInstance.value?.developerMode = developerMode
            }
        }
        jobs.add(developerModeJob)

        // enable/disable trip detection
        val tripDetectionJob = scope.launch {
            appSettings.automaticTripDetection.distinctUntilChanged().collect { tripDetectionEnabled ->
                if(tripDetectionEnabled)
                    drivingInstance.value?.enableTripDetection()
                else
                    drivingInstance.value?.disableTripDetection()
            }
        }
        jobs.add(tripDetectionJob)
    }

    private fun cancelAllJobs() {
        // cancel all jobs that we created
        jobs.forEach { job -> job.cancel() }
        jobs.clear()
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    private fun <T> drivingFlow(flowCreator: (driving: Driving) -> Flow<T>): Flow<T> {
        return drivingInstance.flatMapLatest { driving ->
            if (driving != null) flowCreator(driving) else emptyFlow()
        }
    }
}