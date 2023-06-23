package com.sygic.driving.testapp.core.platform.notification

import android.app.Notification
import android.content.Context
import com.sygic.driving.notification.NotificationProvider
import com.sygic.driving.testapp.R
import com.sygic.driving.testapp.core.utils.Constants

class NotificationProvider(val context: Context):
    NotificationProvider {

    override fun getTripDetectionNotification(): Notification =
        NotificationUtils.getNotification(
            context,
            context.getString(R.string.notification_recording_trips),
            R.drawable.ic_notif_small
        )


    override fun getTripStartedNotification(): Notification =
        NotificationUtils.getNotification(
            context,
            context.getString(R.string.notification_trip_running),
            R.drawable.ic_car
        )

    override fun getTripDetectionNotificationId(): Int = Constants.NOTIFICATION_ID_RECORDING

    override fun getTripStartedNotificationId(): Int = Constants.NOTIFICATION_ID_TRIP_RUNNING

}