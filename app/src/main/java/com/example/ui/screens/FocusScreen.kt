package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BlockShortsCard
import com.example.ui.components.StatCard
import com.example.ui.theme.AmberStreak
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.IndigoPrimary
import com.example.ui.viewmodel.StudyFlowViewModel
import java.time.LocalDate

@Composable
fun FocusScreen(
    viewModel: StudyFlowViewModel,
    modifier: Modifier = Modifier
) {
    val mode by viewModel.timerMode.collectAsState()
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val totalSeconds by viewModel.totalSecondsForMode.collectAsState()
    val isRunning by viewModel.isTimerRunning.collectAsState()
    val sessionsToday by viewModel.completedSessionsToday.collectAsState()
    val focusSessions by viewModel.focusSessions.collectAsState()
    val completionDialog by viewModel.timerCompletionDialog.collectAsState()

    val todayEpoch = LocalDate.now().toEpochDay()
    val todayFocusMinutes = focusSessions
        .filter { it.epochDay == todayEpoch && it.mode == "Focus" }
        .sumOf { it.durationMinutes }
    val totalFocusMinutesAllTime = focusSessions
        .filter { it.mode == "Focus" }
        .sumOf { it.durationMinutes }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    val progress = if (totalSeconds > 0) {
        (totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "timer_progress"
    )

    val modeColor = when (mode) {
        "Short Break" -> EmeraldAccent
        "Long Break" -> AmberStreak
        else -> IndigoPrimary
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Mode Selector Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            listOf("Focus", "Short Break", "Long Break").forEach { m ->
                FilterChip(
                    selected = mode == m,
                    onClick = { viewModel.selectTimerMode(m) },
                    label = { Text(m) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when (m) {
                            "Short Break" -> EmeraldAccent
                            "Long Break" -> AmberStreak
                            else -> IndigoPrimary
                        },
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("mode_chip_${m.lowercase().replace(" ", "_")}")
                )
            }
        }

        // Circular Timer Display
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(260.dp)
                .padding(8.dp)
        ) {
            // Background track
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 14.dp,
                strokeCap = StrokeCap.Round
            )

            // Progress track
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxSize(),
                color = modeColor,
                strokeWidth = 14.dp,
                strokeCap = StrokeCap.Round
            )

            // Timer Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = modeColor.copy(alpha = 0.15f),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = mode.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = modeColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag("countdown_display")
                )

                Text(
                    text = if (isRunning) "Deep focus in progress" else "Paused",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Session #${sessionsToday + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset button
            IconButton(
                onClick = { viewModel.resetTimer() },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("timer_reset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Timer",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Main Play/Pause Button
            Button(
                onClick = {
                    if (isRunning) viewModel.pauseTimer() else viewModel.startOrResumeTimer()
                },
                colors = ButtonDefaults.buttonColors(containerColor = modeColor),
                shape = CircleShape,
                modifier = Modifier
                    .size(72.dp)
                    .testTag("timer_play_pause_button")
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Start",
                    modifier = Modifier.size(36.dp)
                )
            }

            // Skip button
            IconButton(
                onClick = { viewModel.skipTimer() },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("timer_skip_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = "Skip Session",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Focus Statistics Cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Focus Statistics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Today's Focus",
                        value = "${todayFocusMinutes}m",
                        icon = Icons.Default.Timer,
                        iconColor = IndigoPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Today's Blocks",
                        value = "$sessionsToday",
                        icon = Icons.Default.Celebration,
                        iconColor = EmeraldAccent,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "All Time Hours",
                        value = "${totalFocusMinutesAllTime / 60}h",
                        icon = Icons.Default.Timer,
                        iconColor = AmberStreak,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Sessions",
                        value = "${focusSessions.size}",
                        icon = Icons.Default.Timer,
                        iconColor = IndigoPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Dedicated Block Shorts Feature Section
        BlockShortsCard(viewModel = viewModel)
    }

    // Completion Celebration Dialog
    if (completionDialog != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCompletionDialog() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Celebration,
                    contentDescription = null,
                    tint = AmberStreak,
                    modifier = Modifier.size(44.dp)
                )
            },
            title = {
                Text(
                    text = "Session Finished!",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = completionDialog ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissCompletionDialog()
                        if (mode == "Focus") {
                            viewModel.selectTimerMode("Short Break")
                            viewModel.startOrResumeTimer()
                        } else {
                            viewModel.selectTimerMode("Focus")
                            viewModel.startOrResumeTimer()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text(if (mode == "Focus") "Take Short Break" else "Start Next Focus")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.dismissCompletionDialog() }) {
                    Text("Dismiss")
                }
            }
        )
    }
}
