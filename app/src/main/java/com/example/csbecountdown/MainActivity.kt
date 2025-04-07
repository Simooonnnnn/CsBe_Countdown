package com.example.csbecountdown

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.csbecountdown.ui.theme.CsBeCountdownTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownApp(viewModel: CountdownViewModel = viewModel()) {
    val timeLeft by viewModel.timeLeftState
    val countdownComplete by viewModel.countdownCompleteState
    var visible by remember { mutableStateOf(false) }

    // Animation entrance effect
    LaunchedEffect(key1 = "startup") {
        visible = true
    }

    // Current time label
    val currentTimeString = remember {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        String.format("Current time: %02d:%02d", hour, minute)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "CsBe Countdown",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Target date banner
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(800)) +
                        slideInVertically(
                            animationSpec = tween(800, easing = LinearOutSlowInEasing),
                            initialOffsetY = { -it }
                        )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "July 4th, 12:00 PM (UTC+2)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (!countdownComplete) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main countdown card
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1000)) +
                        expandVertically(
                            animationSpec = tween(1000, easing = LinearOutSlowInEasing),
                            expandFrom = Alignment.Top
                        )
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = 6.dp
                    ),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Status label
                        Text(
                            text = if (countdownComplete)
                                "CsBe has ended!"
                            else
                                "Time Until End of CsBe",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Only show the countdown if not complete
                        if (!countdownComplete) {
                            CountdownRow(timeLeft)

                            Spacer(modifier = Modifier.height(16.dp))

                            Divider(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Status message
                            val statusMessage = when {
                                timeLeft.days > 30 -> "Plenty of time remaining"
                                timeLeft.days > 7 -> "Less than a month to go"
                                timeLeft.days > 1 -> "Only ${timeLeft.days} days left!"
                                timeLeft.days == 1L -> "Last day!"
                                timeLeft.hours > 1 -> "Hours remaining!"
                                else -> "Final countdown!"
                            }

                            Text(
                                text = statusMessage,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            // Show completed message
                            Text(
                                text = "CsBe 2025 has concluded",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 32.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Info card
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1200)) +
                        slideInVertically(
                            animationSpec = tween(1200),
                            initialOffsetY = { it / 2 }
                        )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "This countdown app uses Material You theming to match your device colors!",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer
            Text(
                text = "CsBe Countdown • ${Calendar.getInstance().get(Calendar.YEAR)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
fun CountdownRow(timeLeft: TimeLeft) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CountdownDigit(
            value = timeLeft.days,
            label = "Days",
            color = MaterialTheme.colorScheme.primaryContainer,
            textColor = MaterialTheme.colorScheme.onPrimaryContainer
        )

        CountdownDigit(
            value = timeLeft.hours,
            label = "Hours",
            color = MaterialTheme.colorScheme.secondaryContainer,
            textColor = MaterialTheme.colorScheme.onSecondaryContainer
        )

        CountdownDigit(
            value = timeLeft.minutes,
            label = "Minutes",
            color = MaterialTheme.colorScheme.tertiaryContainer,
            textColor = MaterialTheme.colorScheme.onTertiaryContainer
        )

        CountdownDigit(
            value = timeLeft.seconds,
            label = "Seconds",
            color = MaterialTheme.colorScheme.primaryContainer,
            textColor = MaterialTheme.colorScheme.onPrimaryContainer,
            pulseAnimation = true
        )
    }
}

@Composable
fun CountdownDigit(
    value: Long,
    label: String,
    color: Color,
    textColor: Color,
    pulseAnimation: Boolean = false
) {
    // Pulse animation for seconds
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (pulseAnimation) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Elevation animation based on value changes
    var prevValue by remember { mutableStateOf(value) }
    val elevationAnimation by animateFloatAsState(
        targetValue = if (prevValue != value) 12f else 4f,
        animationSpec = tween(
            durationMillis = 300,
            easing = LinearOutSlowInEasing
        ),
        label = "elevation"
    )

    LaunchedEffect(value) {
        if (prevValue != value) {
            prevValue = value
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .size(80.dp)
                .shadow(
                    elevation = elevationAnimation.dp,
                    shape = MaterialTheme.shapes.medium
                ),
            colors = CardDefaults.cardColors(
                containerColor = color
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format("%02d", value),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
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

    private val _countdownCompleteState = mutableStateOf(false)
    val countdownCompleteState: State<Boolean> = _countdownCompleteState

    // Create a separate coroutine scope since we're not using the viewModelScope directly
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    init {
        coroutineScope.launch {
            while (true) {
                val timeLeft = calculateTimeLeft()
                _timeLeftState.value = timeLeft

                // Check if countdown is complete
                if (timeLeft.days == 0L && timeLeft.hours == 0L &&
                    timeLeft.minutes == 0L && timeLeft.seconds == 0L) {
                    _countdownCompleteState.value = true
                    break
                }

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

    override fun onCleared() {
        super.onCleared()
        coroutineScope.cancel()
    }
}

@Preview(showBackground = true)
@Composable
fun CountdownAppPreview() {
    CsBeCountdownTheme {
        CountdownApp()
    }
}