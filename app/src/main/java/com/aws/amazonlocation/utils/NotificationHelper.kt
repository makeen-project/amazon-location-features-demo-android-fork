package com.aws.amazonlocation.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.aws.amazonlocation.R

class NotificationHelper(private val context: Context) {

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_NAME
            }
            // Register the channel with the system
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun isNotificationGroupActive(): Boolean {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val activeNotifications = notificationManager.activeNotifications

        // Check if any of the active notifications belong to the specified group
        for (notification in activeNotifications) {
            val groupKey = notification.notification.group
            if (groupKey == GROUP_KEY_WORK_SIMULATION) {
                return true // Group is still active
            }
        }
        return false // Group is not active or device is below API 23
    }

    fun showNotification(notificationId: Int, subTitle: String, setGroupSummary: Boolean) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentText(subTitle)
            .setGroupSummary(setGroupSummary)
            .setGroup(GROUP_KEY_WORK_SIMULATION)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH).build()

        NotificationManagerCompat.from(context).apply {
            // notificationId is a unique int for each notification that you must define
            notify(notificationId, builder)
        }
    }
}
