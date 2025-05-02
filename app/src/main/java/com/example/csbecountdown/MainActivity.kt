package com.example.csbecountdown

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.csbecountdown.settings.SettingsViewModel
import com.example.csbecountdown.ui.theme.CsBeCountdownTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    // Request launcher for notification permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
        } else {
            // Permission denied
            Toast.makeText(this,
                "Notification permission denied. You won't receive countdown alerts.",
                Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check notification permission on Android 13+
        checkNotificationPermission()

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

    /**
     * Check and request notification permission if needed
     */
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Show rationale and then request
                    Toast.makeText(
                        this,
                        "Notification permission is needed to receive countdown alerts",
                        Toast.LENGTH_LONG
                    ).show()
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // First time asking for permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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
fun CountdownApp(
    countdownViewModel: CountdownViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val timeLeft by countdownViewModel.timeLeftState
    val isCountingUp by countdownViewModel.isCountingUp
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
                0 -> CountdownScreen(timeLeft, isCountingUp)
                1 -> SettingsScreen(settingsViewModel)
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
fun CountdownScreen(
    timeLeft: TimeLeft,
    isCountingUp: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Header - more minimal and placed at the top
        // Change text based on whether we're counting up or down
        Text(
            text = if (isCountingUp) "Days since CsBe" else "CsBe Countdown",
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

data class TimeLeft(
    val days: Long = 0,
    val hours: Long = 0,
    val minutes: Long = 0,
    val seconds: Long = 0
)

class CountdownViewModel : ViewModel() {
    // Target date: July 4th at 12:00 (UTC+2) - FIXED to 2025
    private val targetDate = Calendar.getInstance().apply {
        timeZone = TimeZone.getTimeZone("GMT+2")
        set(Calendar.YEAR, 2025)  // Fixed to 2025
        set(Calendar.MONTH, Calendar.JULY)
        set(Calendar.DAY_OF_MONTH, 4)
        set(Calendar.HOUR_OF_DAY, 12)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private val _timeLeftState = mutableStateOf(calculateTimeLeft())
    val timeLeftState: State<TimeLeft> = _timeLeftState

    // Add state to track if we are counting up (after target date has passed)
    private val _isCountingUp = mutableStateOf(System.currentTimeMillis() > targetDate.timeInMillis)
    val isCountingUp: State<Boolean> = _isCountingUp

    init {
        viewModelScope.launch {
            while (true) {
                // Update the counting up state
                _isCountingUp.value = System.currentTimeMillis() > targetDate.timeInMillis
                _timeLeftState.value = calculateTimeLeft()
                delay(1000)
            }
        }
    }

    private fun calculateTimeLeft(): TimeLeft {
        val currentTime = System.currentTimeMillis()
        val targetTime = targetDate.timeInMillis

        // If current time passed target time, calculate time SINCE target (counting up)
        if (currentTime >= targetTime) {
            val difference = currentTime - targetTime
            val days = TimeUnit.MILLISECONDS.toDays(difference)
            val hours = TimeUnit.MILLISECONDS.toHours(difference) % 24
            val minutes = TimeUnit.MILLISECONDS.toMinutes(difference) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(difference) % 60

            return TimeLeft(days, hours, minutes, seconds)
        }

        // Otherwise calculate time UNTIL target (counting down)
        val difference = targetTime - currentTime
        val days = TimeUnit.MILLISECONDS.toDays(difference)
        val hours = TimeUnit.MILLISECONDS.toHours(difference) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(difference) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(difference) % 60

        return TimeLeft(days, hours, minutes, seconds)
    }
}