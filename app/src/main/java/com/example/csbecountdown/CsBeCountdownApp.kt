package com.example.csbecountdown

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.work.Configuration
import com.example.csbecountdown.notifications.CountdownNotificationManager
import com.example.csbecountdown.settings.NotificationPreferenceManager
import com.example.csbecountdown.widget.WidgetUpdater

class CsBeCountdownApp : Application(), Configuration.Provider {

    // For notifications management
    private lateinit var notificationManager: CountdownNotificationManager
    private lateinit var preferenceManager: NotificationPreferenceManager

    override fun onCreate() {
        super.onCreate()
        Log.d("CsBeCountdownApp", "Application created")

        try {
            WidgetUpdater.requestUpdate(this)
            Log.d("CsBeCountdownApp", "Widget updates scheduled with improved mechanism")
        } catch (e: Exception) {
            Log.e("CsBeCountdownApp", "Failed to schedule widget updates", e)
        }
    }

    // Configure WorkManager with debug logging enabled
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()
}