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
import java.util.concurrent.TimeUnit

/**
 * Handles widget updates using both WorkManager (for periodic reliable updates)
 * and AlarmManager (for more frequent updates if possible)
 */
object WidgetUpdater {

    // Constants for update mechanisms
    const val ACTION_UPDATE_WIDGET = "com.example.csbecountdown.ACTION_UPDATE_WIDGET"
    private const val WORK_NAME = "com.example.csbecountdown.WIDGET_UPDATE_WORK"
    private const val UPDATE_INTERVAL_SECONDS = 15 // Update at least every 15 seconds via WorkManager
    private const val QUICK_UPDATE_INTERVAL_MS = 1000 // Try for 1-second updates with AlarmManager

    // Counters for logging
    private var updateAttempts = 0
    private var successfulUpdates = 0

    /**
     * Request both immediate and periodic updates for the widget
     */
    fun requestUpdate(context: Context) {
        // 1. Schedule periodic updates using WorkManager (reliable but less frequent)
        schedulePeriodicUpdates(context)

        // 2. Try to schedule quick updates using AlarmManager (may be throttled by system)
        scheduleQuickUpdate(context)

        // Log update statistics
        updateAttempts++
        Log.d("WidgetUpdater", "Updates - Attempted: $updateAttempts, Successful: $successfulUpdates")
    }

    /**
     * Schedule regular updates using WorkManager (reliable background processing)
     */
    private fun schedulePeriodicUpdates(context: Context) {
        try {
            // Create constraints - run even when the device is in low power mode
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
                .build()

            // Create a periodic work request
            val updateRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                UPDATE_INTERVAL_SECONDS.toLong(), TimeUnit.SECONDS)
                .setConstraints(constraints)
                .build()

            // Enqueue the work, replacing any existing work
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                updateRequest
            )

            Log.d("WidgetUpdater", "Periodic widget updates scheduled with WorkManager")
        } catch (e: Exception) {
            Log.e("WidgetUpdater", "Failed to schedule periodic widget updates", e)
        }
    }

    /**
     * Try to schedule a quick update using AlarmManager for more immediate feedback
     * Note: These may be throttled by the system on newer Android versions
     */
    private fun scheduleQuickUpdate(context: Context) {
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
            val triggerTime = System.currentTimeMillis() + QUICK_UPDATE_INTERVAL_MS

            // Try to use the most reliable alarm method available on this device
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    // On Android 12+, check if we can schedule exact alarms
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                        Log.d("WidgetUpdater", "Scheduled exact alarm (Android 12+)")
                    } else {
                        // Fall back to inexact alarm
                        alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                        Log.d("WidgetUpdater", "No exact alarm permission, using regular alarm")
                    }
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    // Android 6-11
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    Log.d("WidgetUpdater", "Using setExactAndAllowWhileIdle")
                }
                else -> {
                    // Older Android versions
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    Log.d("WidgetUpdater", "Using setExact for older Android")
                }
            }
        } catch (e: Exception) {
            Log.e("WidgetUpdater", "Failed to schedule quick widget update", e)
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
                    scheduleQuickUpdate(context)

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