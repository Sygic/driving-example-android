package com.sygic.driving.example

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.sygic.driving.notification.NotificationProvider

class DrivingNotificationProvider(val context: Context): NotificationProvider {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "Driving"
        const val RECORDING_NOTIF_ID = 32
    }

    init {
        setupNotificationChannel()
    }

    override fun getTripDetectionNotification(): Notification? {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID
        )
        with(builder) {
            setSmallIcon(R.drawable.ic_notif_small)
            setContentTitle("Driving")
            setContentText("Recording trips")
            setContentIntent(pendingIntent)
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setDefaults(Notification.DEFAULT_ALL)

            return build()
        }
    }

    override fun getTripStartedNotification(): Notification? = null

    override fun getTripDetectionNotificationId(): Int = RECORDING_NOTIF_ID

    override fun getTripStartedNotificationId(): Int = 0

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notif_channel)
            val descriptionText = context.getString(R.string.notif_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}