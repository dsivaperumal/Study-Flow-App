package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalendarEventEntity
import com.example.ui.components.AlignedWeekDayStrip
import com.example.ui.components.CalendarEventModal
import com.example.ui.components.ConfirmationDialog
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RosePriority
import com.example.ui.viewmodel.StudyFlowViewModel
import com.example.utils.TimeUtils
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: StudyFlowViewModel,
    modifier: Modifier = Modifier
) {
    val events by viewModel.calendarEvents.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val appControls by viewModel.appControls.collectAsState()
    val selectedDate by viewModel.selectedCalendarDate.collectAsState()
    val currentYearMonth by viewModel.currentYearMonth.collectAsState()

    var showAddEventModal by remember { mutableStateOf(false) }
    var eventToEdit by remember { mutableStateOf<CalendarEventEntity?>(null) }
    var eventToDelete by remember { mutableStateOf<CalendarEventEntity?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Month Grid & Events, 1: Full Day Timeline

    val selectedEpochDay = selectedDate.toEpochDay()
    val selectedDateEvents = events.filter { it.dateEpochDay == selectedEpochDay }
    val selectedDateTasks = tasks.filter { it.dueDateEpochDay == selectedEpochDay }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Calendar & Schedule",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Track appointments, deadlines, and study blocks.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { showAddEventModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("add_event_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Event")
            }
        }

        // View Tabs (Calendar Month vs Day Timeline)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Monthly Calendar") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Today's Schedule Timeline") }
            )
        }

        if (selectedTab == 0) {
            // Weekly Day-Date Strip for rapid alignment & selection
            AlignedWeekDayStrip(
                selectedDate = selectedDate,
                onDateSelected = { viewModel.selectCalendarDate(it) }
            )

            // Month & Year Navigation Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Month & Year Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Previous Year
                            IconButton(onClick = { viewModel.changeYear(-1) }, modifier = Modifier.size(32.dp)) {
                                Text("-1y", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }

                            // Previous Month
                            IconButton(onClick = { viewModel.previousMonth() }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month", modifier = Modifier.size(16.dp))
                            }

                            // Today Jump Button
                            TextButton(
                                onClick = { viewModel.jumpToToday() },
                                modifier = Modifier.testTag("calendar_today_button")
                            ) {
                                Text("Today")
                            }

                            // Next Month
                            IconButton(onClick = { viewModel.nextMonth() }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month", modifier = Modifier.size(16.dp))
                            }

                            // Next Year
                            IconButton(onClick = { viewModel.changeYear(1) }, modifier = Modifier.size(32.dp)) {
                                Text("+1y", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Days of Week Header - perfectly aligned with 7-column calendar matrix
                    val daysOfWeek = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        daysOfWeek.forEach { dayName ->
                            Text(
                                text = dayName,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Grid Matrix
                    val firstOfMonth = currentYearMonth.atDay(1)
                    val daysInMonth = currentYearMonth.lengthOfMonth()
                    val startOffset = (firstOfMonth.dayOfWeek.value - 1) % 7 // Monday = 0

                    val totalCells = ((startOffset + daysInMonth + 6) / 7) * 7

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (row in 0 until totalCells / 7) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                for (col in 0 until 7) {
                                    val cellIndex = row * 7 + col
                                    val dayOfMonth = cellIndex - startOffset + 1

                                    if (dayOfMonth in 1..daysInMonth) {
                                        val cellDate = currentYearMonth.atDay(dayOfMonth)
                                        val isSelected = cellDate == selectedDate
                                        val isToday = cellDate == LocalDate.now()
                                        val cellEpoch = cellDate.toEpochDay()
                                        val hasEvents = events.any { it.dateEpochDay == cellEpoch }
                                        val hasTasks = tasks.any { it.dueDateEpochDay == cellEpoch }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    when {
                                                        isSelected -> IndigoPrimary
                                                        isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                                        else -> Color.Transparent
                                                    }
                                                )
                                                .clickable { viewModel.selectCalendarDate(cellDate) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = "$dayOfMonth",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                    color = when {
                                                        isSelected -> Color.White
                                                        isToday -> MaterialTheme.colorScheme.primary
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    }
                                                )

                                                if (hasEvents || hasTasks) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    ) {
                                                        if (hasEvents) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(4.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (isSelected) Color.White else IndigoPrimary)
                                                            )
                                                        }
                                                        if (hasTasks) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(4.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (isSelected) Color.White else EmeraldAccent)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Events for Selected Date
            val selectedDateStr = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Events for $selectedDateStr",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { showAddEventModal = true }) {
                    Text("+ Add")
                }
            }

            if (selectedDateEvents.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No events scheduled on this date.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { showAddEventModal = true }) {
                            Text("Add Event on ${selectedDate.format(DateTimeFormatter.ofPattern("MMM d"))}")
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    selectedDateEvents.forEach { event ->
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
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = TimeUtils.formatToAmPm(event.startTime),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = TimeUtils.formatToAmPm(event.endTime),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = event.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (event.description.isNotBlank()) {
                                        Text(
                                            text = event.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Text(
                                            text = event.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                IconButton(onClick = { eventToEdit = event }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(onClick = { eventToDelete = event }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = RosePriority.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Weekly Day-Date Strip
            AlignedWeekDayStrip(
                selectedDate = selectedDate,
                onDateSelected = { viewModel.selectCalendarDate(it) }
            )

            // --- Today's Schedule Timeline View (Fully Editable) ---
            val todayEpoch = remember { LocalDate.now().toEpochDay() }
            val todayEvents = events.filter { it.dateEpochDay == todayEpoch }
                .sortedBy { TimeUtils.timeToMinutesOfDay(it.startTime) }
            val todayTasks = tasks.filter { it.dueDateEpochDay == todayEpoch }
                .sortedBy { TimeUtils.timeToMinutesOfDay(it.dueTime) }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card with Add Schedule action
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Today's Schedule Timeline",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Button(
                                onClick = { showAddEventModal = true },
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("add_schedule_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Schedule", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(
                            text = "Tap any schedule item to edit its title, times, or category. Tap the trash icon to delete.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Schedule Items List
                if (todayEvents.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = IndigoPrimary.copy(alpha = 0.5f),
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "No schedule items for today",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Plan your routine: College, Lunch, Deep Study, Work, or Dinner.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { showAddEventModal = true },
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                            ) {
                                Text("+ Add Schedule Item")
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        todayEvents.forEach { event ->
                            val startFormatted = TimeUtils.formatToAmPm(event.startTime)
                            val endFormatted = TimeUtils.formatToAmPm(event.endTime)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { eventToEdit = event }
                                    .testTag("schedule_item_${event.id}"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Time Indicator Box
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = IndigoPrimary.copy(alpha = 0.12f),
                                        modifier = Modifier.width(96.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = startFormatted,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = IndigoPrimary
                                            )
                                            Text(
                                                text = "to $endFormatted",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Content
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "→",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = IndigoPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = event.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        if (event.description.isNotBlank()) {
                                            Text(
                                                text = event.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Text(
                                                text = event.category,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    // Action Buttons
                                    IconButton(
                                        onClick = { eventToEdit = event },
                                        modifier = Modifier.testTag("edit_schedule_${event.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Schedule Item",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { eventToDelete = event },
                                        modifier = Modifier.testTag("delete_schedule_${event.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Schedule Item",
                                            tint = RosePriority.copy(alpha = 0.85f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Optional Section: Today's Tasks Due
                if (todayTasks.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Tasks Due Today",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldAccent
                            )

                            todayTasks.forEach { task ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = task.isCompleted,
                                        onCheckedChange = { viewModel.toggleTaskCompletion(task) },
                                        colors = CheckboxDefaults.colors(checkedColor = EmeraldAccent)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = task.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                        )
                                        Text(
                                            text = "Due ${TimeUtils.formatToAmPm(task.dueTime)} • ${task.category}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialogs
    if (showAddEventModal) {
        CalendarEventModal(
            defaultEpochDay = selectedEpochDay,
            onSave = { title, description, dateEpochDay, startTime, endTime, category ->
                viewModel.addCalendarEvent(title, description, dateEpochDay, startTime, endTime, category)
                showAddEventModal = false
            },
            onDismiss = { showAddEventModal = false }
        )
    }

    if (eventToEdit != null) {
        CalendarEventModal(
            eventToEdit = eventToEdit,
            onSave = { title, description, dateEpochDay, startTime, endTime, category ->
                viewModel.updateCalendarEvent(
                    eventToEdit!!.copy(
                        title = title,
                        description = description,
                        dateEpochDay = dateEpochDay,
                        startTime = startTime,
                        endTime = endTime,
                        category = category
                    )
                )
                eventToEdit = null
            },
            onDismiss = { eventToEdit = null }
        )
    }

    if (eventToDelete != null) {
        ConfirmationDialog(
            title = "Delete Event",
            message = "Are you sure you want to remove \"${eventToDelete!!.title}\"?",
            confirmButtonText = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteCalendarEvent(eventToDelete!!)
                eventToDelete = null
            },
            onDismiss = { eventToDelete = null }
        )
    }
}

private data class TimelineEntry(
    val time: String,
    val title: String,
    val subtitle: String,
    val type: String,
    val color: Color
)
