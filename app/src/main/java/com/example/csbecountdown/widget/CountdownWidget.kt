package com.example.csbecountdown.widget

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.datastore.preferences.core.longPreferencesKey

/**
 * Material-themed countdown widget
 */
class CountdownWidget : GlanceAppWidget() {

    // Define a preferences key for the last update timestamp
    companion object {
        private val LAST_UPDATE_KEY = longPreferencesKey("last_update")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Log.d("CountdownWidget", "provideGlance called for ID: $id")

        // Update the timestamp in widget state to force refresh
        try {
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[LAST_UPDATE_KEY] = System.currentTimeMillis()
                }
            }
        } catch (e: Exception) {
            Log.e("CountdownWidget", "Failed to update widget state", e)
        }

        // Provide the Material-styled content
        provideContent {
            CountdownWidgetContent()
        }

        // Request widget update to keep the countdown current
        WidgetUpdater.requestUpdate(context)
    }
}

/**
 * Widget receiver that handles widget lifecycle events
 */
class CountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.d("CountdownWidget", "onUpdate called for ${appWidgetIds.size} widgets")

        // Request an update to start the update cycle
        WidgetUpdater.requestUpdate(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d("CountdownWidget", "onEnabled called")

        // Initialize updates when the first widget is added
        WidgetUpdater.requestUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d("CountdownWidget", "onDisabled called - all widgets removed")
    }

    override fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray) {
        super.onRestored(context, oldWidgetIds, newWidgetIds)
        Log.d("CountdownWidget", "onRestored called")

        // Request updates for restored widgets
        WidgetUpdater.requestUpdate(context)
    }
}