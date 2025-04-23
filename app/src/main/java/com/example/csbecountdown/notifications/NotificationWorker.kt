package com.example.csbecountdown.notifications

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * Worker that displays countdown notifications at scheduled times
 */
class NotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    companion object {
        private const val TAG = "NotificationWorker"
        private const val DATA_DAYS_LEFT = "days_left"
        private const val DATA_NOTIFICATION_ID = "notification_id"
    }

    override fun doWork(): Result {
        try {
            // Get input data
            val daysLeft = inputData.getInt(DATA_DAYS_LEFT, -1)
            val notificationId = inputData.getInt(DATA_NOTIFICATION_ID, -1)

            if (daysLeft < 0 || notificationId < 0) {
                Log.e(TAG, "Invalid notification data: days=$daysLeft, id=$notificationId")
                return Result.failure()
            }

            Log.d(TAG, "Showing notification for $daysLeft days left, id=$notificationId")

            // Create notification manager
            val appContext = applicationContext

            // Check notification permission on Android 13+
            if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                // Get notification
                val notificationManager = CountdownNotificationManager(appContext as android.app.Application)
                val notification = notificationManager.createNotificationBuilder(daysLeft).build()

                // Display notification
                val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                systemNotificationManager.notify(notificationId, notification)

                Log.d(TAG, "Notification displayed successfully")
                return Result.success()
            } else {
                Log.w(TAG, "Notifications are disabled by the user")
                return Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show notification", e)
            return Result.failure()
        }
    }
}