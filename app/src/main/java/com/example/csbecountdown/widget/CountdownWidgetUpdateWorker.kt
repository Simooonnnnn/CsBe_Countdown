package com.example.csbecountdown.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Handles widget updates using both WorkManager (for periodic reliable updates)
 * and AlarmManager (for better alignment with minute boundaries)
 */
object WidgetUpdater {

    // Constants for update mechanisms
    const val ACTION_UPDATE_WIDGET = "com.example.csbecountdown.ACTION_UPDATE_WIDGET"
    private const val WORK_NAME = "com.example.csbecountdown.WIDGET_UPDATE_WORK"
    private const val UPDATE_INTERVAL_MINUTES = 1L // Update every minute via WorkManager
    private const val INITIAL_DELAY_MS = 60000L // Wait 1 minute for the first update

    // Counters for logging
    private var updateAttempts = 0
    private var successfulUpdates = 0

    /**
     * Request both immediate and periodic updates for the widget
     */
    fun requestUpdate(context: Context) {
        // 1. Schedule periodic updates using WorkManager (reliable but less frequent)
        schedulePeriodicUpdates(context)

        // 2. Try to schedule updates aligned with minute boundaries
        scheduleMinuteAlignedUpdate(context)

        // Log update statistics
        updateAttempts++
        Log.d("WidgetUpdater", "Updates - Attempted: $updateAttempts, Successful: $successfulUpdates")
    }

    /**
     * Schedule regular updates using WorkManager (reliable background processing)
     * This will update the widget every minute
     */
    private fun schedulePeriodicUpdates(context: Context) {
        try {
            // Create constraints - run even when the device is in low power mode
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
                .build()

            // Create a periodic work request that runs every minute
            val updateRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                UPDATE_INTERVAL_MINUTES, TimeUnit.MINUTES)
                .setInitialDelay(INITIAL_DELAY_MS, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build()

            // Enqueue the work, replacing any existing work
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                updateRequest
            )

            Log.d("WidgetUpdater", "Periodic widget updates scheduled for every minute")
        } catch (e: Exception) {
            Log.e("WidgetUpdater", "Failed to schedule periodic widget updates", e)
        }
    }

    /**
     * Schedule an alarm to align updates with minute boundaries for more natural timing
     */
    private fun scheduleMinuteAlignedUpdate(context: Context) {
        try {
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                Intent(context, WidgetUpdateReceiver::class.java).apply {
                    action = ACTION_UPDATE_WIDGET
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // Calculate the next minute boundary
            val currentTimeMillis = System.currentTimeMillis()
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = currentTimeMillis
            calendar.add(Calendar.MINUTE, 1)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val nextMinute = calendar.timeInMillis

            // Set alarm for next minute boundary
            alarmManager.set(
                AlarmManager.RTC,
                nextMinute,
                pendingIntent
            )

            Log.d("WidgetUpdater", "Scheduled next update at minute boundary: ${nextMinute - currentTimeMillis}ms from now")
        } catch (e: Exception) {
            Log.e("WidgetUpdater", "Failed to schedule widget update", e)
        }
    }

    /**
     * Manually update all widgets right now
     */
    fun updateWidgetsNow(context: Context) {
        // Get a wake lock to ensure update completes
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "CsBeCountdown:WidgetUpdateWakeLock"
        )

        try {
            wakeLock.acquire(10*1000L) // 10 seconds max

            val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
            coroutineScope.launch {
                try {
                    // Update all widget instances
                    CountdownWidget().updateAll(context)
                    successfulUpdates++
                    Log.d("WidgetUpdater", "Successfully updated widgets")
                } catch (e: Exception) {
                    Log.e("WidgetUpdater", "Error updating widgets", e)
                } finally {
                    // Schedule the next update
                    scheduleMinuteAlignedUpdate(context)

                    // Release the wake lock
                    if (wakeLock.isHeld) {
                        wakeLock.release()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("WidgetUpdater", "Error in updateWidgetsNow", e)
            // Make sure we release the wake lock
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }
}

/**
 * WorkManager Worker class that updates widgets periodically
 */
class WidgetUpdateWorker(
    private val context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        Log.d("WidgetUpdateWorker", "WorkManager update triggered")
        try {
            // Update all widgets now
            WidgetUpdater.updateWidgetsNow(context)
            return Result.success()
        } catch (e: Exception) {
            Log.e("WidgetUpdateWorker", "Failed to update widgets", e)
            return Result.retry()
        }
    }
}

/**
 * Receives the broadcast when it's time to update the widget via AlarmManager
 */
class WidgetUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == WidgetUpdater.ACTION_UPDATE_WIDGET) {
            Log.d("WidgetUpdateReceiver", "Received widget update broadcast")
            WidgetUpdater.updateWidgetsNow(context)
        }
    }
}