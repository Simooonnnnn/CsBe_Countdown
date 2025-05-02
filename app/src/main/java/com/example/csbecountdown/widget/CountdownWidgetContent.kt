package com.example.csbecountdown.widget

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
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.csbecountdown.MainActivity
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

@Composable
fun CountdownWidgetContent() {
    val context = LocalContext.current

    // Get colors - using the improved DynamicColorHelper
    val backgroundColor = DynamicColorHelper.getSurface(context)
    val textColor = DynamicColorHelper.getOnSurface(context)
    val containerColor = DynamicColorHelper.getPrimaryContainer(context)
    val containerTextColor = DynamicColorHelper.getOnPrimaryContainer(context)

    // Calculate time left until July 4th at 12:00 (UTC+2)
    val timeLeft = calculateTimeLeft()

    // Create the entire widget layout with proper vertical centering
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(16.dp)
            .background(ColorProvider(backgroundColor))
            .clickable(actionStartActivity<MainActivity>()),
        contentAlignment = Alignment.Center  // Center all content
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            verticalAlignment = Alignment.Vertical.CenterVertically  // Center vertically
        ) {
            // Title - now changes based on whether we're counting up or down
            Text(
                text = if (timeLeft.isCountingUp) "Days since CsBe" else "CsBe Countdown",
                style = TextStyle(
                    color = ColorProvider(textColor),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = GlanceModifier.height(12.dp))

            // Countdown row - simplified to match the minimalist app design
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                CountdownUnit(
                    value = timeLeft.days,
                    label = "Days",
                    containerColor = containerColor,
                    textColor = containerTextColor
                )

                CountdownUnit(
                    value = timeLeft.hours,
                    label = "Hours",
                    containerColor = containerColor,
                    textColor = containerTextColor
                )

                CountdownUnit(
                    value = timeLeft.minutes,
                    label = "Min",  // Shortened to save space
                    containerColor = containerColor,
                    textColor = containerTextColor
                )
            }
        }
    }
}

@Composable
fun CountdownUnit(
    value: Long,
    label: String,
    containerColor: Color,
    textColor: Color
) {
    Column(
        modifier = GlanceModifier.padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        // Digit
        Text(
            text = String.format("%02d", value),  // Leading zero format
            style = TextStyle(
                color = ColorProvider(textColor),
                fontSize = 32.sp,  // Larger digits
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )

        // Label
        Text(
            text = label,
            style = TextStyle(
                color = ColorProvider(textColor.copy(alpha = 0.8f)),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        )
    }
}

// Modified TimeLeft data class to include isCountingUp flag
data class TimeLeft(
    val days: Long = 0,
    val hours: Long = 0,
    val minutes: Long = 0,
    val seconds: Long = 0,
    val isCountingUp: Boolean = false  // New flag to indicate if we're counting up
)

// Modified to handle counting up after target date
fun calculateTimeLeft(): TimeLeft {
    // Target date: Fixed to July 4th, 2025 at 12:00 (UTC+2)
    val targetDate = Calendar.getInstance().apply {
        timeZone = TimeZone.getTimeZone("GMT+2")
        set(Calendar.YEAR, 2025)  // Fixed to 2025
        set(Calendar.MONTH, Calendar.JULY)
        set(Calendar.DAY_OF_MONTH, 4)
        set(Calendar.HOUR_OF_DAY, 12)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val currentTime = System.currentTimeMillis()
    val targetTime = targetDate.timeInMillis

    // Check if we've passed the target date
    val isCountingUp = currentTime >= targetTime

    // If current time passed target time, calculate time SINCE target
    if (isCountingUp) {
        val difference = currentTime - targetTime
        val days = TimeUnit.MILLISECONDS.toDays(difference)
        val hours = TimeUnit.MILLISECONDS.toHours(difference) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(difference) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(difference) % 60

        return TimeLeft(days, hours, minutes, seconds, isCountingUp = true)
    }

    // Otherwise calculate time UNTIL target
    val difference = targetTime - currentTime
    val days = TimeUnit.MILLISECONDS.toDays(difference)
    val hours = TimeUnit.MILLISECONDS.toHours(difference) % 24
    val minutes = TimeUnit.MILLISECONDS.toMinutes(difference) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(difference) % 60

    return TimeLeft(days, hours, minutes, seconds, isCountingUp = false)
}