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

        // Initialize widget updates with improved mechanism
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