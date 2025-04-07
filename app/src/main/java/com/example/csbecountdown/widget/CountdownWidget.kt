package com.example.csbecountdown.widget

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent

class CountdownWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Log.d("CountdownWidget", "provideGlance called for ID: $id")

        // Provide the content - our Material You enabled content
        provideContent {
            CountdownWidgetContent()
        }

        // Request widget update to keep the countdown current
        WidgetUpdater.requestUpdate(context)
    }
}

// This receiver is what Android system calls when the widget is added to the home screen
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
}