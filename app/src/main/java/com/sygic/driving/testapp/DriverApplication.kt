package com.sygic.driving.testapp

import androidx.multidex.MultiDexApplication
import com.sygic.driving.testapp.core.platform.notification.NotificationUtils
import com.testfairy.TestFairy
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DriverApplication: MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()

        if(!BuildConfig.DEBUG) {
            TestFairy.begin(this, "SDK-AQzTxOBz")
        }

        NotificationUtils.setupNotificationChannel(this)
    }
}