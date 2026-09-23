package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity
import com.example.ui.components.AlignedWeekDayStrip
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.StatCard
import com.example.ui.components.TaskCard
import com.example.ui.components.TaskModal
import com.example.ui.navigation.Screen
import com.example.ui.theme.AmberStreak
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.IndigoPrimary
import com.example.ui.viewmodel.StudyFlowViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: StudyFlowViewModel,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsState()
    val focusSessions by viewModel.focusSessions.collectAsState()
    val settings by viewModel.userSettings.collectAsState()
    val calendarEvents by viewModel.calendarEvents.collectAsState()

    val todayEpoch = remember { LocalDate.now().toEpochDay() }
    val todayTasks = tasks.filter { it.dueDateEpochDay == todayEpoch }
    val completedTodayTasks = todayTasks.filter { it.isCompleted }

    // Focus time today
    val todayFocusMinutes = focusSessions
        .filter { it.epochDay == todayEpoch && it.mode == "Focus" }
        .sumOf { it.durationMinutes }
    val focusHours = todayFocusMinutes / 60
    val focusMins = todayFocusMinutes % 60
    val focusTimeString = if (focusHours > 0) "${focusHours}h ${focusMins}m" else "${focusMins}m"

    var showAddTaskModal by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskEntity?>(null) }

    // Greeting calculation
    val currentHour = LocalTime.now().hour
    val greeting = when {
        currentHour < 12 -> "Good Morning 👋"
        currentHour < 17 -> "Good Afternoon 👋"
        else -> "Good Evening 👋"
    }

    val formattedDate = remember {
        val now = LocalDate.now()
        now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault()))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // --- Header Section ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "“Plan → Focus → Work → Track → Improve”",
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = { onNavigate(Screen.Settings) },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("dashboard_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // --- 4 Statistics Cards (Grid/Row) ---
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Today's Tasks",
                    value = "${todayTasks.size}",
                    subtitle = "${todayTasks.size - completedTodayTasks.size} pending",
                    icon = Icons.Default.CheckCircle,
                    iconColor = IndigoPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Completed",
                    value = "${completedTodayTasks.size}",
                    subtitle = if (todayTasks.isNotEmpty()) "${(completedTodayTasks.size * 100) / todayTasks.size}% done" else "0%",
                    icon = Icons.Default.CheckCircle,
                    iconColor = EmeraldAccent,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Focus Time",
                    value = focusTimeString,
                    subtitle = "Target: 2h 00m",
                    icon = Icons.Default.Timer,
                    iconColor = IndigoPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Current Streak",
                    value = "🔥 ${settings.currentStreak}",
                    subtitle = "Best: ${settings.bestStreak} days",
                    icon = Icons.Default.LocalFireDepartment,
                    iconColor = AmberStreak,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // --- Quick Actions ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showAddTaskModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("quick_action_add_task")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Task")
                    }

                    Button(
                        onClick = { onNavigate(Screen.Focus) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("quick_action_start_focus")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Focus")
                    }

                    OutlinedButton(
                        onClick = { onNavigate(Screen.Calendar) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("quick_action_open_calendar")
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Calendar")
                    }

                    OutlinedButton(
                        onClick = { onNavigate(Screen.FocusSearch) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("quick_action_focus_search")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Focus Search")
                    }

                    OutlinedButton(
                        onClick = { onNavigate(Screen.BlockShorts) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("quick_action_block_shorts")
                    ) {
                        Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Block Shorts")
                    }
                }
            }
        }

        // --- Focus Banner Section ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigate(Screen.Focus) }
                .testTag("dashboard_focus_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(IndigoPrimary.copy(alpha = 0.3f))
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Focus Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Today's Focus: $focusTimeString",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Next 25m Pomodoro block ready",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { onNavigate(Screen.Focus) },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Start Focus")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }

        // --- Weekly Day-Date Strip (MON 15, TUE 16, etc.) ---
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly Schedule",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onNavigate(Screen.Calendar) }) {
                    Text("Open Calendar")
                }
            }
            AlignedWeekDayStrip(
                selectedDate = LocalDate.now(),
                onDateSelected = { onNavigate(Screen.Calendar) }
            )
        }

        // --- Today's Tasks Section ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Tasks (${todayTasks.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onNavigate(Screen.Tasks) }) {
                    Text("View All")
                }
            }

            if (todayTasks.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No tasks for today yet",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Add your first task and start planning your day.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { showAddTaskModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                        ) {
                            Text("+ Add Task")
                        }
                    }
                }
            } else {
                todayTasks.forEach { task ->
                    TaskCard(
                        task = task,
                        onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                        onEdit = { taskToEdit = task },
                        onDelete = { taskToDelete = task }
                    )
                }
            }
        }

        // --- Calendar Upcoming Events Preview ---
        val todayEvents = calendarEvents.filter { it.dateEpochDay == todayEpoch }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming Schedule",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onNavigate(Screen.Calendar) }) {
                    Text("Calendar")
                }
            }

            if (todayEvents.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = "No more events scheduled for today.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                todayEvents.forEach { event ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Text(
                                    text = event.startTime,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (event.description.isNotBlank()) {
                                    Text(
                                        text = event.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Modals ---
    if (showAddTaskModal) {
        TaskModal(
            onSave = { title, notes, priority, category, epochDay, dueTime ->
                viewModel.addTask(title, notes, priority, category, epochDay, dueTime)
                showAddTaskModal = false
            },
            onDismiss = { showAddTaskModal = false }
        )
    }

    if (taskToEdit != null) {
        TaskModal(
            taskToEdit = taskToEdit,
            onSave = { title, notes, priority, category, epochDay, dueTime ->
                viewModel.updateTask(
                    taskToEdit!!.copy(
                        title = title,
                        notes = notes,
                        priority = priority,
                        category = category,
                        dueDateEpochDay = epochDay,
                        dueTime = dueTime
                    )
                )
                taskToEdit = null
            },
            onDismiss = { taskToEdit = null }
        )
    }

    if (taskToDelete != null) {
        ConfirmationDialog(
            title = "Delete Task",
            message = "Are you sure you want to delete \"${taskToDelete!!.title}\"?",
            confirmButtonText = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteTask(taskToDelete!!)
                taskToDelete = null
            },
            onDismiss = { taskToDelete = null }
        )
    }
}
