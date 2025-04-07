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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

// Enhanced light theme colors as Color objects
private val LIGHT_SURFACE = Color(0xFFF8F9FF)
private val LIGHT_ON_SURFACE = Color(0xFF1A1C1E)
private val LIGHT_PRIMARY = Color(0xFF006AF5)
private val LIGHT_PRIMARY_CONTAINER = Color(0xFFD8E2FF)
private val LIGHT_ON_PRIMARY_CONTAINER = Color(0xFF001A41)
private val LIGHT_SECONDARY_CONTAINER = Color(0xFFD7E3F7)
private val LIGHT_ON_SECONDARY_CONTAINER = Color(0xFF101C2B)
private val LIGHT_TERTIARY_CONTAINER = Color(0xFFF2DAFF)
private val LIGHT_ON_TERTIARY_CONTAINER = Color(0xFF251431)
private val LIGHT_OUTLINE = Color(0xFFBDBFCE)

@Composable
fun CountdownWidgetContent() {
    val context = LocalContext.current

    // Calculate time left until July 4th at 12:00 (UTC+2)
    val timeLeft = calculateTimeLeft()
    Log.d("CountdownWidget", "Rendering widget with timeLeft: $timeLeft")

    // Get current date formatted nicely
    val currentDate = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())

    // Create the entire widget layout with enhanced colors and styling
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(20.dp)
            .background(LIGHT_SURFACE)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            // Header with title and date
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                // App title
                Text(
                    text = "CsBe Countdown",
                    style = TextStyle(
                        color = ColorProvider(LIGHT_PRIMARY),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )

                // Current date
                Text(
                    text = currentDate,
                    style = TextStyle(
                        color = ColorProvider(LIGHT_ON_SURFACE),
                        fontSize = 14.sp,
                        textAlign = TextAlign.End
                    ),
                    modifier = GlanceModifier
                )
            }

            // Target date information
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(LIGHT_PRIMARY_CONTAINER.copy(alpha = 0.5f))
                    .cornerRadius(12.dp)
                    .padding(vertical = 6.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "July 4th, 12:00 PM (UTC+2)",
                    style = TextStyle(
                        color = ColorProvider(LIGHT_ON_PRIMARY_CONTAINER),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(16.dp))

            // Countdown row with improved styling
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                // Days
                CountdownUnit(
                    value = timeLeft.days,
                    label = "Days",
                    modifier = GlanceModifier.defaultWeight(),
                    containerColor = LIGHT_PRIMARY_CONTAINER,
                    textColor = LIGHT_ON_PRIMARY_CONTAINER
                )

                Spacer(modifier = GlanceModifier.width(8.dp))

                // Hours
                CountdownUnit(
                    value = timeLeft.hours,
                    label = "Hours",
                    modifier = GlanceModifier.defaultWeight(),
                    containerColor = LIGHT_SECONDARY_CONTAINER,
                    textColor = LIGHT_ON_SECONDARY_CONTAINER
                )

                Spacer(modifier = GlanceModifier.width(8.dp))

                // Minutes
                CountdownUnit(
                    value = timeLeft.minutes,
                    label = "Minutes",
                    modifier = GlanceModifier.defaultWeight(),
                    containerColor = LIGHT_TERTIARY_CONTAINER,
                    textColor = LIGHT_ON_TERTIARY_CONTAINER
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Status info at the bottom
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (timeLeft.days > 30) {
                        "A long wait ahead..."
                    } else if (timeLeft.days > 7) {
                        "Getting closer!"
                    } else if (timeLeft.days > 0) {
                        "Just ${timeLeft.days} days to go!"
                    } else if (timeLeft.hours > 0) {
                        "Hours remaining!"
                    } else if (timeLeft.minutes > 0) {
                        "Almost there!"
                    } else {
                        "It's happening now!"
                    },
                    style = TextStyle(
                        color = ColorProvider(LIGHT_ON_SURFACE),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}

@Composable
fun CountdownUnit(
    value: Long,
    label: String,
    modifier: GlanceModifier = GlanceModifier,
    containerColor: Color = LIGHT_PRIMARY_CONTAINER,
    textColor: Color = LIGHT_ON_PRIMARY_CONTAINER
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        // Box with countdown digit with enhanced styling
        Box(
            modifier = GlanceModifier
                .size(width = 60.dp, height = 60.dp)
                .cornerRadius(16.dp)
                .background(containerColor)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format("%02d", value),
                style = TextStyle(
                    color = ColorProvider(textColor),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        Text(
            text = label,
            style = TextStyle(
                color = ColorProvider(LIGHT_ON_SURFACE),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
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
        // Target date: July 4th at 12:00 (UTC+2)
        val targetDate = Calendar.getInstance().apply {
            timeZone = TimeZone.getTimeZone("GMT+2")
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