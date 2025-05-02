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
    }

    override fun doWork(): Result {
        try {
            // Get input data
            val daysLeft = inputData.getInt(CountdownNotificationManager.DATA_DAYS_LEFT, -1)
            val notificationId = inputData.getInt(CountdownNotificationManager.DATA_NOTIFICATION_ID, -1)

            if (daysLeft < 0 || notificationId < 0) {
                Log.e(TAG, "Invalid notification data: days=$daysLeft, id=$notificationId")
                return Result.failure()
            }

            Log.d(TAG, "Showing notification for $daysLeft days left, id=$notificationId")

            // Create history manager to track shown notifications
            val historyManager = NotificationHistoryManager(applicationContext as android.app.Application)

            // Check if this notification has already been shown (extra safety check)
            if (historyManager.isNotificationShown(daysLeft)) {
                Log.d(TAG, "Notification for $daysLeft days already shown, skipping")
                return Result.success()
            }

            // Check notification permission on Android 13+
            if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                // Get notification
                val notificationManager = CountdownNotificationManager(applicationContext as android.app.Application)
                val notification = notificationManager.createNotificationBuilder(daysLeft).build()

                // Display notification
                val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                systemNotificationManager.notify(notificationId, notification)

                // Mark this milestone as shown
                historyManager.markNotificationShown(daysLeft)

                Log.d(TAG, "Notification displayed and marked as shown")
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