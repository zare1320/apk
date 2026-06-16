package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Utility helper to handle Android Notification Channel creation, 
 * permission state verification, and safe local notification delivery.
 */
object NotificationHelper {
    const val CHANNEL_ID = "vetaris_notifications_channel"
    const val CHANNEL_NAME = "Vetaris Assistant Alerts"
    const val CHANNEL_DESC = "Configured alerts for patient diagnostic, vaccine reminders and health reports"

    /**
     * Registers the required notification channel for Android Oreo (O / API 26) and above.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Safeguards against API level differences to check if system permissions are active.
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /**
     * Dispatches a local system notification safely, catching potential SecurityExceptions
     * introduced on Android 13+ (API 33+) due to permission checks.
     */
    fun sendNotification(context: Context, title: String, content: String) {
        // Ensure channel is registered prior to showing notification
        createNotificationChannel(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // System standard informational icon
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(1001, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
