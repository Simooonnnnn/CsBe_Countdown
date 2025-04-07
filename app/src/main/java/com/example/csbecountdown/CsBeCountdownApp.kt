package com.example.csbecountdown

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.work.Configuration
import com.example.csbecountdown.widget.WidgetUpdater

class CsBeCountdownApp : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        Log.d("CsBeCountdownApp", "Application created")

        // Initialize WorkManager with our configuration
        androidx.work.WorkManager.initialize(this, workManagerConfiguration)

        // Initialize widget updates
        try {
            // Use the new WidgetUpdater instead of direct WorkManager
            WidgetUpdater.requestUpdate(this)
            Log.d("CsBeCountdownApp", "Widget updates scheduled")
        } catch (e: Exception) {
            Log.e("CsBeCountdownApp", "Failed to schedule widget updates", e)
        }
    }

    // Configure WorkManager
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()
}