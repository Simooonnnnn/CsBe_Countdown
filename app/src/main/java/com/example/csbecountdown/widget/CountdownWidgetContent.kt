package com.example.csbecountdown.widget

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.csbecountdown.MainActivity
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

// Basic light theme colors as Color objects
private val LIGHT_SURFACE = Color(0xFFFDFBFF)
private val LIGHT_ON_SURFACE = Color(0xFF1A1C1E)
private val LIGHT_PRIMARY_CONTAINER = Color(0xFFD8E2FF)
private val LIGHT_ON_PRIMARY_CONTAINER = Color(0xFF001A41)

@Composable
fun CountdownWidgetContent() {
    val context = LocalContext.current

    // Calculate time left until July 4th at 12:00 (UTC+1)
    val timeLeft = calculateTimeLeft()

    Log.d("CountdownWidget", "Rendering widget with timeLeft: $timeLeft")

    // Create the entire widget layout with basic colors
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(16.dp)
            .background(LIGHT_SURFACE)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = "CsBe Countdown",
                style = TextStyle(
                    color = ColorProvider(LIGHT_ON_SURFACE),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                ),
                modifier = GlanceModifier.fillMaxWidth()
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            Text(
                text = "July 4th, 12:00 PM (UTC+1)",
                style = TextStyle(
                    color = ColorProvider(LIGHT_ON_SURFACE),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = GlanceModifier.fillMaxWidth()
            )

            Spacer(modifier = GlanceModifier.height(16.dp))

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                // Days
                CountdownUnit(
                    value = timeLeft.days,
                    label = "Days",
                    modifier = GlanceModifier.defaultWeight()
                )

                Spacer(modifier = GlanceModifier.width(4.dp))

                // Hours
                CountdownUnit(
                    value = timeLeft.hours,
                    label = "Hours",
                    modifier = GlanceModifier.defaultWeight()
                )

                Spacer(modifier = GlanceModifier.width(4.dp))

                // Minutes
                CountdownUnit(
                    value = timeLeft.minutes,
                    label = "Min",
                    modifier = GlanceModifier.defaultWeight()
                )

                Spacer(modifier = GlanceModifier.width(4.dp))

                // Seconds
                CountdownUnit(
                    value = timeLeft.seconds,
                    label = "Sec",
                    modifier = GlanceModifier.defaultWeight()
                )
            }

            // Request an update for the widget to keep the countdown current
            WidgetUpdater.requestUpdate(context)
        }
    }
}

@Composable
fun CountdownUnit(
    value: Long,
    label: String,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        // Box with countdown digit
        Box(
            modifier = GlanceModifier
                .size(width = 48.dp, height = 48.dp)
                .cornerRadius(16.dp)
                .background(LIGHT_PRIMARY_CONTAINER)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format("%02d", value),
                style = TextStyle(
                    color = ColorProvider(LIGHT_ON_PRIMARY_CONTAINER),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(4.dp))

        Text(
            text = label,
            style = TextStyle(
                color = ColorProvider(LIGHT_ON_SURFACE),
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        )
    }
}

data class TimeLeft(
    val days: Long = 0,
    val hours: Long = 0,
    val minutes: Long = 0,
    val seconds: Long = 0
)

fun calculateTimeLeft(): TimeLeft {
    return try {
        // Target date: July 4th at 12:00 (UTC+1)
        val targetDate = Calendar.getInstance().apply {
            timeZone = TimeZone.getTimeZone("GMT+1")
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

        val currentTime = System.currentTimeMillis()
        val targetTime = targetDate.timeInMillis

        // If current time passed target time, return zeros
        if (currentTime >= targetTime) {
            return TimeLeft(0, 0, 0, 0)
        }

        val difference = targetTime - currentTime
        val days = TimeUnit.MILLISECONDS.toDays(difference)
        val hours = TimeUnit.MILLISECONDS.toHours(difference) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(difference) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(difference) % 60

        TimeLeft(days, hours, minutes, seconds)
    } catch (e: Exception) {
        Log.e("CountdownWidget", "Error calculating time left", e)
        TimeLeft(0, 0, 0, 0)
    }
}