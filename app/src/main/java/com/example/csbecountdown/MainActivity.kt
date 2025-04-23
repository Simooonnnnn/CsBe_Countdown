package com.example.csbecountdown

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.filled.Timer        // Import Timer icon (filled style)
import androidx.compose.material.icons.outlined.Timer      // Import Timer icon (outlined style)
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.csbecountdown.ui.theme.CsBeCountdownTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CsBeCountdownTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CountdownApp()
                }
            }
        }
    }
}

// Simple icon object with available icons - using real Timer icons now
object AppIcons {
    val Timer = Icons.Outlined.Timer
    val TimerSelected = Icons.Filled.Timer
    val Settings = Icons.Outlined.Settings
    val SettingsSelected = Icons.Filled.Settings
}

@Composable
fun CountdownApp(viewModel: CountdownViewModel = viewModel()) {
    val timeLeft by viewModel.timeLeftState
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Main content area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0 -> CountdownScreen(timeLeft)
                1 -> SettingsScreen()
            }
        }

        // Bottom navigation with Material icons
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            // Countdown tab
            NavigationBarItem(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = {
                    Icon(
                        imageVector = if (selectedTab == 0) AppIcons.TimerSelected else AppIcons.Timer,
                        contentDescription = "Countdown"
                    )
                },
                label = { Text("Countdown") }
            )

            // Settings tab
            NavigationBarItem(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = {
                    Icon(
                        imageVector = if (selectedTab == 1) AppIcons.SettingsSelected else AppIcons.Settings,
                        contentDescription = "Settings"
                    )
                },
                label = { Text("Settings") }
            )
        }
    }
}

@Composable
fun CountdownScreen(timeLeft: TimeLeft) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Header - more minimal and placed at the top
        Text(
            text = "CsBe Countdown",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.weight(0.7f))

        // Countdown digits - main focus of the screen
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CountdownDigit(value = timeLeft.days, label = "Days")
            CountdownDigit(value = timeLeft.hours, label = "Hours")
            CountdownDigit(value = timeLeft.minutes, label = "Minutes")
            CountdownDigit(value = timeLeft.seconds, label = "Seconds")
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun CountdownDigit(value: Long, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Digit - larger font exactly matching the screenshot
        Text(
            text = String.format("%02d", value),  // Format with leading zero
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 48.sp,
                letterSpacing = (-1).sp  // Tighter letter spacing for numbers
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Label - lighter weight with proper spacing
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                letterSpacing = 0.sp
            ),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Settings header
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Placeholder content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Coming soon",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

data class TimeLeft(
    val days: Long = 0,
    val hours: Long = 0,
    val minutes: Long = 0,
    val seconds: Long = 0
)

class CountdownViewModel : ViewModel() {
    // Target date: July 4th at 12:00 (UTC+2)
    private val targetDate = Calendar.getInstance().apply {
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

    private val _timeLeftState = mutableStateOf(calculateTimeLeft())
    val timeLeftState: State<TimeLeft> = _timeLeftState

    init {
        viewModelScope.launch {
            while (true) {
                _timeLeftState.value = calculateTimeLeft()
                delay(1000)
            }
        }
    }

    private fun calculateTimeLeft(): TimeLeft {
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

        return TimeLeft(days, hours, minutes, seconds)
    }
}

// Preview Composables
@Preview(showBackground = true)
@Composable
fun CountdownScreenPreview() {
    CsBeCountdownTheme {
        CountdownScreen(
            timeLeft = TimeLeft(
                days = 77,
                hours = 23,
                minutes = 33,
                seconds = 48
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    CsBeCountdownTheme {
        SettingsScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CountdownAppPreview() {
    // Create a preview with static data
    val previewViewModel = CountdownViewModel()
    // Use reflection to set the private field for preview
    val field = CountdownViewModel::class.java.getDeclaredField("_timeLeftState")
    field.isAccessible = true
    field.set(previewViewModel, mutableStateOf(TimeLeft(77, 23, 33, 48)))

    CsBeCountdownTheme {
        CountdownApp(viewModel = previewViewModel)
    }
}