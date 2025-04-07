package com.example.csbecountdown.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Handles widget updates using AlarmManager for precise timing
 */
object WidgetUpdater {

    // Make this constant public so it can be accessed by the receiver
    const val ACTION_UPDATE_WIDGET = "com.example.csbecountdown.ACTION_UPDATE_WIDGET"

    /**
     * Schedule a widget update using AlarmManager for more precise timing
     */
    fun requestUpdate(context: Context) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetComponent = ComponentName(context, CountdownWidgetReceiver::class.java)

            // Only schedule updates if we have widgets
            val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)
            if (widgetIds.isEmpty()) {
                Log.d("WidgetUpdater", "No widgets found to update")
                return
            }

            // Create intent for the update
            val updateIntent = Intent(context, WidgetUpdateReceiver::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }

            // Create PendingIntent for the update
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                updateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Schedule update for 1 second from now
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val triggerTime = System.currentTimeMillis() + 1000 // 1 second

            // On newer Android versions, use setExactAndAllowWhileIdle for precise timing
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }

            Log.d("WidgetUpdater", "Widget update scheduled via AlarmManager")
        } catch (e: Exception) {
            Log.e("WidgetUpdater", "Failed to schedule widget update", e)
        }
    }
}

/**
 * Receives the broadcast when it's time to update the widget
 */
class WidgetUpdateReceiver : BroadcastReceiver() {

    // Create a coroutine scope for async operations
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == WidgetUpdater.ACTION_UPDATE_WIDGET) {
            Log.d("WidgetUpdateReceiver", "Received widget update broadcast")

            // Update the widgets in a coroutine to use suspend functions
            coroutineScope.launch {
                try {
                    // Use GlanceAppWidgetManager to get widget instances
                    val glanceWidgetManager = GlanceAppWidgetManager(context)
                    val glanceIds = glanceWidgetManager.getGlanceIds(CountdownWidget::class.java)

                    // If we have widgets, update them
                    if (glanceIds.isNotEmpty()) {
                        CountdownWidget().updateAll(context)
                        Log.d("WidgetUpdateReceiver", "Updated ${glanceIds.size} widgets")

                        // Schedule next update
                        WidgetUpdater.requestUpdate(context)
                    } else {
                        Log.d("WidgetUpdateReceiver", "No widgets to update")
                    }
                } catch (e: Exception) {
                    Log.e("WidgetUpdateReceiver", "Error updating widgets", e)
                }
            }
        }
    }
}