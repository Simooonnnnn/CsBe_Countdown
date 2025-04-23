package com.example.csbecountdown.notifications

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.csbecountdown.settings.NotificationPreferenceManager

/**
 * Initializes notifications during app startup
 */
class NotificationInitializer : Initializer<Unit> {

    companion object {
        private const val TAG = "NotificationInitializer"
    }

    override fun create(context: Context) {
        Log.d(TAG, "Initializing notification system")

        try {
            // Check if notifications are enabled in preferences
            val preferenceManager = NotificationPreferenceManager(context.applicationContext as android.app.Application)
            val notificationsEnabled = preferenceManager.getNotificationsEnabled()

            if (notificationsEnabled) {
                Log.d(TAG, "Notifications are enabled, scheduling")
                val notificationManager = CountdownNotificationManager(context.applicationContext as android.app.Application)
                notificationManager.scheduleNotifications()
            } else {
                Log.d(TAG, "Notifications are disabled, skipping scheduling")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize notifications", e)
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        // WorkManager should be initialized before this initializer
        return listOf(WorkManagerInitializer::class.java)
    }
}

/**
 * Custom initializer for WorkManager to ensure it's properly initialized
 */
class WorkManagerInitializer : Initializer<WorkManager> {
    override fun create(context: Context): WorkManager {
        val configuration = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()

        WorkManager.initialize(context, configuration)
        return WorkManager.getInstance(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}