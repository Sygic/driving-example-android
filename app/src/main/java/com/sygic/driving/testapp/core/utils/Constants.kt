package com.sygic.driving.testapp.core.utils

import com.sygic.driving.testapp.BuildConfig

object Constants {

    const val DRIVING_CLIENT_ID = "sygic.driverbehaviour.test"

    const val SYGIC_LICENSE = BuildConfig.SYGIC_LICENSE

    const val AUTH_URL = "https://auth-testing.api.sygic.com/"
    const val DRB_SERVER_URL = "https://adas2-data-gw-testing.api.sygic.com/"

    const val ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ"

    const val NOTIFICATION_ID_RECORDING = 32
    const val NOTIFICATION_ID_TRIP_RUNNING = 33
}