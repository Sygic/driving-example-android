package com.sygic.driving.example

import android.os.Bundle
import android.telephony.TelephonyManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import com.sygic.driving.Configuration
import com.sygic.driving.Driving
import com.sygic.driving.VehicleSettings
import com.sygic.driving.VehicleType
import com.sygic.driving.api.Callback
import com.sygic.driving.api.TripDetails
import com.sygic.driving.api.TripsView
import com.sygic.driving.api.UserStats
import java.util.Date

// Set to false if you want to stop trip detection when app is not running
private const val RUN_IN_BACKGROUND = true

class MainActivity : AppCompatActivity() {

    private val drivingViewModel: DrivingViewModel by viewModels()

    private lateinit var drivingEventsListener: DrivingEventListener

    private lateinit var btnLastTrips: Button
    private lateinit var btnLastTripDetails: Button
    private lateinit var btnUserStats: Button
    private lateinit var btnStartTrip: Button
    private lateinit var btnEndTrip: Button
    private lateinit var btnClearLog: Button
    private lateinit var textTripState: TextView

    private lateinit var logger: Logger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnLastTrips = findViewById(R.id.btnLastTrips)
        btnLastTrips.setOnClickListener{ lasTrips() }

        btnLastTripDetails = findViewById(R.id.btnLastTripDetails)
        btnLastTripDetails.setOnClickListener{ lastTripDetails() }

        btnUserStats = findViewById(R.id.btnUserStats)
        btnUserStats.setOnClickListener{ userStats() }

        btnStartTrip = findViewById(R.id.btnStartTrip)
        btnStartTrip.setOnClickListener{ startTrip() }

        btnEndTrip = findViewById(R.id.btnEndTrip)
        btnEndTrip.setOnClickListener{ endTrip() }

        btnClearLog = findViewById(R.id.btnClearLog)
        btnClearLog.setOnClickListener{ logger.clear() }

        textTripState = findViewById(R.id.tvTripState)

        drivingViewModel.isInTrip.observe(this, Observer {
            isInTrip -> changeTripState(isInTrip)
        })

        logger = Logger(this, findViewById(R.id.logger), findViewById(R.id.scrollView))
        drivingEventsListener = DrivingEventListener(logger, drivingViewModel)

        if(!Driving.isInitialized) {
            enableButtons(false)
            initSygicDriving()
        }
        else {
            enableButtons(true)
            setupSygicDriving()
        }
    }

    override fun onDestroy() {
        if(Driving.isInitialized) {
            with(Driving.getInstance()) {
                removeEventListener(drivingEventsListener)

                if(!RUN_IN_BACKGROUND && !isChangingConfigurations) {
                    disableTripDetection()
                    Driving.deinitialize()
                }
            }
        }
        super.onDestroy()
    }

    private fun initSygicDriving() {
        val configuration = Configuration.Builder()
            .sendOnMobileData(true)
            .build()

        val vehicleSettings = VehicleSettings.Builder()
            .vehicleType(VehicleType.Car)
            .build()


        val initListener = object: Driving.InitListener {
            override fun onInitStateChanged(state: Driving.InitState) {
                when(state) {
                    Driving.InitState.Initialized -> {
                        logger.log("Driving lib initialized successfully")
                        setupSygicDriving()
                    }
                    else -> logger.log("Failed to initialize Driving lib: $state")
                }
            }
        }

        val userId = User(this).id

        val initializer =
            Driving.Initializer.Builder(applicationContext, "<your_client_id>", userId, initListener)
                .configuration(configuration)
                .vehicleSettings(vehicleSettings)
                .userCountry(getCountryIso())
                .notificationProvider(DrivingNotificationProvider(applicationContext))
                .build()

        Driving.initialize(initializer)
    }

    private fun setupSygicDriving() {
        with(Driving.getInstance()) {
            addEventListener(drivingEventsListener)
            enableTripDetection()

            drivingViewModel.isInTrip.value = isTripRunning
        }
    }

    private fun getCountryIso(): String {
        (getSystemService(FragmentActivity.TELEPHONY_SERVICE) as? TelephonyManager)?.let {
            return it.simCountryIso
        }
        return ""
    }

    private fun enableButtons(enable: Boolean) {
        btnLastTrips.isEnabled = enable
        btnLastTripDetails.isEnabled = enable
        btnUserStats.isEnabled = enable
        btnStartTrip.isEnabled = enable
        btnEndTrip.isEnabled = enable
    }

    private fun getLastTrips(resultCallback: (TripsView) -> Unit) {
        val today = Date()
        val monthAgo = Date(today.time - 5L*31L*24L*60L*60L*1000L)

        Driving.getInstance().serverApi.userTrips(monthAgo, today)
            .pageSize(10)
            .page(0)
            .callback(object: Callback<TripsView> {
                override fun onResult(isSuccessful: Boolean, errorCode: Int, result: TripsView?) {
                    if(result != null) {
                        resultCallback(result)
                    }
                    else if(!isSuccessful) {
                        logger.log("Failed to get trips: Error $errorCode")
                    }
                    else {
                        logger.log("No trips received")
                    }
                }

            })
            .send()
    }

    private fun lasTrips() {
        getLastTrips {
            logger.logTitle("Trips:")
            for(trip in it.trips) {
                logger.log(trip)
            }
        }
    }

    private fun lastTripDetails() {
        getLastTrips {
            if(it.trips.isNotEmpty()) {
                val lastTripId = it.trips[0].externalId
                getTripDetails(lastTripId)
            }
        }
    }

    private fun getTripDetails(tripId: String) {
        Driving.getInstance().serverApi.userTripDetails(tripId)
            .callback(object: Callback<TripDetails> {
                override fun onResult(isSuccessful: Boolean, errorCode: Int, result: TripDetails?) {
                    result?.let {
                        logger.logTitle("Last Trip Detail:")
                        logger.log(it)
                    }
                }
            })
            .send()
    }

    private fun userStats() {
        Driving.getInstance().serverApi.liveStats()
            .callback(object: Callback<Array<UserStats>> {
                override fun onResult(isSuccessful: Boolean, errorCode: Int, result: Array<UserStats>?) {
                    result?.let {
                        logger.logTitle("Statistics:")
                        for(stats in it) {
                            logger.log(stats)
                        }
                    }
                }
            })
            .send()
    }

    private fun startTrip() {
        Driving.getInstance().startTrip()
    }

    private fun endTrip() {
        Driving.getInstance().endTrip()
    }

    private fun changeTripState(running: Boolean) {
        textTripState.text = if(running) "In Trip" else "Idle"
    }
}
