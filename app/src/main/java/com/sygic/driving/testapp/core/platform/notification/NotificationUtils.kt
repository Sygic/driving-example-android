package com.sygic.driving.testapp.core.platform.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import com.sygic.driving.testapp.R
import com.sygic.driving.testapp.core.utils.asImmutableFlag
import com.sygic.driving.testapp.ui.MainActivity

private const val NOTIFICATION_CHANNEL_ID = "DriverScoring"

object NotificationUtils {

    fun setupNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notif_channel)
            val descriptionText = context.getString(R.string.notif_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun getNotification(context: Context, text: String, @DrawableRes icon: Int): Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT.asImmutableFlag()
        )

        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        with(builder) {
            setSmallIcon(icon)
            setContentTitle(context.getString(R.string.app_name))
            setContentText(text)
            setLargeIcon(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.ic_launcher_foreground
                )
            )
            setContentIntent(pendingIntent)
            priority = NotificationCompat.PRIORITY_HIGH
            setDefaults(Notification.DEFAULT_ALL)

            return build()
        }
    }
}