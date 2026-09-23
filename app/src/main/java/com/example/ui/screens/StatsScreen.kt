package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StatCard
import com.example.ui.theme.AmberStreak
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.IndigoPrimary
import com.example.ui.viewmodel.StudyFlowViewModel
import java.time.LocalDate

@Composable
fun StatsScreen(
    viewModel: StudyFlowViewModel,
    modifier: Modifier = Modifier
) {
    val focusSessions by viewModel.focusSessions.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val settings by viewModel.userSettings.collectAsState()

    val todayEpoch = remember { LocalDate.now().toEpochDay() }

    // Focus calculations
    val todayFocusMinutes = focusSessions
        .filter { it.epochDay == todayEpoch && it.mode == "Focus" }
        .sumOf { it.durationMinutes }

    val weeklyFocusMinutes = focusSessions
        .filter { it.epochDay >= todayEpoch - 7 && it.mode == "Focus" }
        .sumOf { it.durationMinutes }

    val monthlyFocusMinutes = focusSessions
        .filter { it.epochDay >= todayEpoch - 30 && it.mode == "Focus" }
        .sumOf { it.durationMinutes }

    val completedTasksCount = tasks.count { it.isCompleted }
    val totalTasksCount = tasks.size
    val completedSessionsCount = focusSessions.count { it.mode == "Focus" && it.isCompleted }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Title
        Column {
            Text(
                text = "Productivity Insights",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Track your focus consistency, streaks, and milestone completions.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Streak Banner Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("streak_banner_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = AmberStreak.copy(alpha = 0.12f)
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(AmberStreak.copy(alpha = 0.5f))
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(AmberStreak.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak Flame",
                        tint = AmberStreak,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🔥 ${settings.currentStreak} Day Streak",
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = AmberStreak
                    )
                    Text(
                        text = "Personal Best: ${settings.bestStreak} consecutive days",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Complete a task or focus block daily to keep your momentum burning!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Focus Time Metrics
        Text(
            text = "Focus Time Breakdown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Today",
                value = "${todayFocusMinutes / 60}h ${todayFocusMinutes % 60}m",
                icon = Icons.Default.Timer,
                iconColor = IndigoPrimary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Past 7 Days",
                value = "${weeklyFocusMinutes / 60}h ${weeklyFocusMinutes % 60}m",
                icon = Icons.Default.TrendingUp,
                iconColor = EmeraldAccent,
                modifier = Modifier.weight(1f)
            )
        }

        StatCard(
            title = "Past 30 Days (Monthly)",
            value = "${monthlyFocusMinutes / 60}h ${monthlyFocusMinutes % 60}m",
            subtitle = "Total focus sessions: $completedSessionsCount",
            icon = Icons.Default.Schedule,
            iconColor = IndigoPrimary,
            modifier = Modifier.fillMaxWidth()
        )

        // Weekly Focus Hours Visual Chart (Mon - Sun)
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
                    text = "Weekly Activity Distribution",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Hours focused per day over the current week",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                val daysOfWeekLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                val mockHours = listOf(2.5f, 3.2f, 1.8f, 4.0f, 2.0f, 3.5f, 1.5f)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    daysOfWeekLabels.forEachIndexed { index, day ->
                        val hours = mockHours[index]
                        val barFraction = (hours / 5.0f).coerceIn(0.15f, 1f)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${hours}h",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(22.dp)
                                    .height((barFraction * 80).dp)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(if (index == 3 || index == 5) IndigoPrimary else IndigoPrimary.copy(alpha = 0.4f))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Tasks Completion Rate Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Task Completion Rate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$completedTasksCount / $totalTasksCount Done",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldAccent
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val completionRatio = if (totalTasksCount > 0) completedTasksCount.toFloat() / totalTasksCount.toFloat() else 0f
                LinearProgressIndicator(
                    progress = { completionRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = EmeraldAccent,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${(completionRatio * 100).toInt()}% overall task success rate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
