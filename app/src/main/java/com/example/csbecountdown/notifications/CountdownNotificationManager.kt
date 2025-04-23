package com.example.csbecountdown.notifications

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.csbecountdown.MainActivity
import com.example.csbecountdown.R
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Manages countdown notifications
 */
class CountdownNotificationManager(private val application: Application) {

    companion object {
        private const val TAG = "CountdownNotificationMgr"
        const val CHANNEL_ID = "countdown_notifications"

        // Notification IDs
        private const val NOTIFICATION_ID_BASE = 10000
        const val NOTIFICATION_ID_FINAL = 10999

        // WorkManager tags
        private const val WORK_TAG_PREFIX = "countdown_notification_"
        private const val DATA_DAYS_LEFT = "days_left"
        private const val DATA_NOTIFICATION_ID = "notification_id"
    }

    // Target date: July 4th at 12:00 (UTC+2)
    private val targetDate = Calendar.getInstance().apply {
        timeZone = TimeZone.getTimeZone("GMT+2")
        set(Calendar.MONTH, Calendar.JULY)
        set(Calendar.DAY_OF_MONTH, 4)
        set(Calendar.HOUR_OF_DAY, 12)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        // If target date has already passed this year, set to next year
        if (timeInMillis < System.currentTimeMillis()) {
            add(Calendar.YEAR, 1)
        }
    }

    private val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val workManager = WorkManager.getInstance(application)

    init {
        createNotificationChannel()
    }

    /**
     * Create notification channel for Android O and above
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Countdown Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for countdown milestones"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    /**
     * Schedule all notifications based on the countdown milestones
     */
    fun scheduleNotifications() {
        Log.d(TAG, "Scheduling notifications")
        cancelAllNotifications() // Clear existing notifications first

        val currentTime = System.currentTimeMillis()
        val targetTime = targetDate.timeInMillis
        val daysLeft = TimeUnit.MILLISECONDS.toDays(targetTime - currentTime)

        Log.d(TAG, "Days left until target: $daysLeft")

        // Key milestone days (70, 60, 50, 40, 30, 20, 10)
        val milestoneDays = listOf(70, 60, 50, 40, 30, 20, 10)
        milestoneDays.forEach { milestone ->
            if (daysLeft >= milestone) {
                scheduleMilestoneNotification(milestone)
            }
        }

        // Final week (7, 6, 5, 4, 3, 2, 1)
        for (day in 7 downTo 1) {
            if (daysLeft >= day) {
                scheduleMilestoneNotification(day)
            }
        }

        // Final day (0)
        if (daysLeft >= 0) {
            scheduleFinalNotification()
        }

        Log.d(TAG, "All notifications scheduled")
    }

    /**
     * Schedule a notification for a specific milestone
     */
    private fun scheduleMilestoneNotification(daysLeft: Int) {
        val currentTime = System.currentTimeMillis()
        val targetTime = targetDate.timeInMillis
        val totalDaysLeft = TimeUnit.MILLISECONDS.toDays(targetTime - currentTime)

        // Only schedule if we haven't passed this milestone
        if (totalDaysLeft < daysLeft) {
            Log.d(TAG, "Milestone $daysLeft days already passed, skipping")
            return
        }

        // Calculate delay until this notification should trigger
        val delayInDays = totalDaysLeft - daysLeft
        val delayInMillis = TimeUnit.DAYS.toMillis(delayInDays)

        // Create work data
        val notificationId = NOTIFICATION_ID_BASE + daysLeft
        val notificationData = Data.Builder()
            .putInt(DATA_DAYS_LEFT, daysLeft)
            .putInt(DATA_NOTIFICATION_ID, notificationId)
            .build()

        // Create work request
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)
            .setInputData(notificationData)
            .addTag(WORK_TAG_PREFIX + daysLeft)
            .build()

        // Schedule the work
        workManager.enqueueUniqueWork(
            WORK_TAG_PREFIX + daysLeft,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

        Log.d(TAG, "Scheduled notification for $daysLeft days left (delay: $delayInDays days)")
    }

    /**
     * Schedule the final notification for when countdown reaches zero
     */
    private fun scheduleFinalNotification() {
        val currentTime = System.currentTimeMillis()
        val targetTime = targetDate.timeInMillis

        if (targetTime <= currentTime) {
            Log.d(TAG, "Target date already passed, not scheduling final notification")
            return
        }

        val delayInMillis = targetTime - currentTime

        // Create work data
        val notificationData = Data.Builder()
            .putInt(DATA_DAYS_LEFT, 0)
            .putInt(DATA_NOTIFICATION_ID, NOTIFICATION_ID_FINAL)
            .build()

        // Create work request
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)
            .setInputData(notificationData)
            .addTag(WORK_TAG_PREFIX + "final")
            .build()

        // Schedule the work
        workManager.enqueueUniqueWork(
            WORK_TAG_PREFIX + "final",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

        Log.d(TAG, "Scheduled final notification (delay: ${TimeUnit.MILLISECONDS.toDays(delayInMillis)} days)")
    }

    /**
     * Create notification builder with app's styling
     */
    fun createNotificationBuilder(daysLeft: Int): NotificationCompat.Builder {
        // Create an intent for opening the app
        val intent = Intent(application, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            application,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create appropriate title and message based on days left
        val title = when (daysLeft) {
            0 -> "Countdown Complete!"
            1 -> "Just 1 Day Left!"
            else -> "$daysLeft Days Left"
        }

        val message = when (daysLeft) {
            0 -> "The countdown has reached zero. July 4th has arrived!"
            1 -> "Tomorrow is July 4th!"
            in 2..7 -> "Only $daysLeft days until July 4th!"
            else -> "$daysLeft days until July 4th. Stay tuned!"
        }

        // Build the notification
        return NotificationCompat.Builder(application, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
    }

    /**
     * Cancel all scheduled notifications
     */
    fun cancelAllNotifications() {
        // Cancel any pending work
        workManager.cancelAllWorkByTag(WORK_TAG_PREFIX + "final")

        // Cancel milestone notifications
        val milestoneDays = listOf(70, 60, 50, 40, 30, 20, 10, 7, 6, 5, 4, 3, 2, 1)
        milestoneDays.forEach { milestone ->
            workManager.cancelAllWorkByTag(WORK_TAG_PREFIX + milestone)
        }

        // Cancel any displayed notifications
        notificationManager.cancelAll()

        Log.d(TAG, "All notifications canceled")
    }
}