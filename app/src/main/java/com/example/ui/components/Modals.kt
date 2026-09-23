package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalendarEventEntity
import com.example.data.model.TaskEntity
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RosePriority
import com.example.utils.TimeUtils
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun AmPmTimePickerModal(
    initialTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val (initHour12, initMinute, initIsPm) = TimeUtils.parseTo12HourMinute(initialTime)
    var hour12 by remember { mutableIntStateOf(initHour12) }
    var minute by remember { mutableIntStateOf(initMinute) }
    var isPm by remember { mutableStateOf(initIsPm) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Select Time (AM/PM)", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Digital clock display
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = String.format("%02d : %02d", hour12, minute),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                text = if (isPm) "PM" else "AM",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // AM / PM Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    FilterChip(
                        selected = !isPm,
                        onClick = { isPm = false },
                        label = { Text("AM (Morning)", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    FilterChip(
                        selected = isPm,
                        onClick = { isPm = true },
                        label = { Text("PM (Afternoon/Night)", fontWeight = FontWeight.Bold) }
                    )
                }

                // Hour selector (1..12)
                Text("Select Hour", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (1..12).forEach { h ->
                        FilterChip(
                            selected = (hour12 == h),
                            onClick = { hour12 = h },
                            label = { Text("$h") },
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }

                // Minute selector
                Text("Select Minute", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                val minuteOptions = listOf(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 59)
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    minuteOptions.forEach { m ->
                        FilterChip(
                            selected = (minute == m),
                            onClick = { minute = m },
                            label = { Text(String.format("%02d", m)) },
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }

                // Quick presets
                Text("Common Presets", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("09:00 AM", "01:00 PM", "05:30 PM", "07:30 PM").forEach { preset ->
                        OutlinedButton(
                            onClick = {
                                val (h12, min, pm) = TimeUtils.parseTo12HourMinute(preset)
                                hour12 = h12
                                minute = min
                                isPm = pm
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 2.dp)
                        ) {
                            Text(preset.replace(" ", "\n"), fontSize = 10.sp, textAlign = TextAlign.Center, lineHeight = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val result = TimeUtils.formatAmPm(hour12, minute, isPm)
                    onTimeSelected(result)
                    onDismiss()
                }
            ) {
                Text("Set Time")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CustomDatePickerModal(
    initialEpochDay: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val initialDate = remember { LocalDate.ofEpochDay(initialEpochDay) }
    var currentYearMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }
    var selectedDate by remember { mutableStateOf(initialDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Select Due Date", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header with month navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row {
                        IconButton(onClick = { currentYearMonth = currentYearMonth.minusMonths(1) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                        }
                        IconButton(onClick = { currentYearMonth = currentYearMonth.plusMonths(1) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                        }
                    }
                }

                // Days of week header
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Calendar grid
                val firstOfMonth = currentYearMonth.atDay(1)
                val daysInMonth = currentYearMonth.lengthOfMonth()
                val startOffset = (firstOfMonth.dayOfWeek.value - 1) % 7
                val totalCells = ((startOffset + daysInMonth + 6) / 7) * 7

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (row in 0 until totalCells / 7) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (col in 0 until 7) {
                                val cellIndex = row * 7 + col
                                val dayOfMonth = cellIndex - startOffset + 1
                                if (dayOfMonth in 1..daysInMonth) {
                                    val cellDate = currentYearMonth.atDay(dayOfMonth)
                                    val isSelected = cellDate == selectedDate
                                    val isToday = cellDate == LocalDate.now()

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when {
                                                    isSelected -> IndigoPrimary
                                                    isToday -> MaterialTheme.colorScheme.primaryContainer
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .clickable { selectedDate = cellDate }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$dayOfMonth",
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // Selected date readout
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Chosen: ${selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"))}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDateSelected(selectedDate.toEpochDay())
                    onDismiss()
                }
            ) {
                Text("Confirm Date")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskModal(
    taskToEdit: TaskEntity? = null,
    onSave: (title: String, notes: String, priority: String, category: String, epochDay: Long, dueTime: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var notes by remember { mutableStateOf(taskToEdit?.notes ?: "") }
    var priority by remember { mutableStateOf(taskToEdit?.priority ?: "Medium") }
    var category by remember { mutableStateOf(taskToEdit?.category ?: "Study") }
    val initialTimeFormatted = remember { TimeUtils.formatToAmPm(taskToEdit?.dueTime ?: "03:00 PM") }
    var dueTime by remember { mutableStateOf(initialTimeFormatted) }

    val todayEpoch = remember { LocalDate.now().toEpochDay() }
    var selectedEpochDay by remember { mutableStateOf(taskToEdit?.dueDateEpochDay ?: todayEpoch) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val categories = listOf("Study", "Assignment", "Project", "Personal", "Work", "Other")
    val priorities = listOf("Low", "Medium", "High")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (taskToEdit == null) "Add New Task" else "Edit Task",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (errorMessage != null) errorMessage = null
                    },
                    label = { Text("Task Name *") },
                    placeholder = { Text("e.g. Complete Java DSA Practice") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input"),
                    isError = errorMessage != null
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = RosePriority,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Description") },
                    placeholder = { Text("Add key details or references...") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_notes_input")
                )

                // Priority Selection
                Text("Priority", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    priorities.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (p) {
                                    "High" -> RosePriority
                                    "Medium" -> MaterialTheme.colorScheme.tertiary
                                    else -> EmeraldAccent
                                },
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("priority_chip_$p")
                        )
                    }
                }

                // Category Selection
                Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.forEach { c ->
                        FilterChip(
                            selected = category == c,
                            onClick = { category = c },
                            label = { Text(c) },
                            modifier = Modifier.testTag("category_chip_$c")
                        )
                    }
                }

                // Due Date Selection (Today, Tomorrow, and Custom Date Picker)
                Text("Due Date", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedEpochDay == todayEpoch,
                        onClick = { selectedEpochDay = todayEpoch },
                        label = { Text("Today") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedEpochDay == todayEpoch + 1,
                        onClick = { selectedEpochDay = todayEpoch + 1 },
                        label = { Text("Tomorrow") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("select_custom_date_button")
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Date 📅")
                }

                // Visible Chosen Date Readout
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Due: " + LocalDate.ofEpochDay(selectedEpochDay).format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy")),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        textAlign = TextAlign.Center
                    )
                }

                // Task Time — AM/PM Format
                Text("Due Time", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_time_input")
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Time: $dueTime ⏰", fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Please enter a task name"
                    } else {
                        onSave(title, notes, priority, category, selectedEpochDay, dueTime)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("task_save_button")
            ) {
                Text(if (taskToEdit == null) "Add Task" else "Save Changes")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showDatePicker) {
        CustomDatePickerModal(
            initialEpochDay = selectedEpochDay,
            onDateSelected = { selectedEpochDay = it },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showTimePicker) {
        AmPmTimePickerModal(
            initialTime = dueTime,
            onTimeSelected = { dueTime = it },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
fun CalendarEventModal(
    eventToEdit: CalendarEventEntity? = null,
    defaultEpochDay: Long = LocalDate.now().toEpochDay(),
    onSave: (title: String, description: String, dateEpochDay: Long, startTime: String, endTime: String, category: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(eventToEdit?.title ?: "") }
    var description by remember { mutableStateOf(eventToEdit?.description ?: "") }
    var selectedEpochDay by remember { mutableStateOf(eventToEdit?.dateEpochDay ?: defaultEpochDay) }
    var startTime by remember { mutableStateOf(TimeUtils.formatToAmPm(eventToEdit?.startTime ?: "09:00 AM")) }
    var endTime by remember { mutableStateOf(TimeUtils.formatToAmPm(eventToEdit?.endTime ?: "10:30 AM")) }
    var category by remember { mutableStateOf(eventToEdit?.category ?: "Study") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val todayEpoch = remember { LocalDate.now().toEpochDay() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (eventToEdit == null) "Add Schedule Item" else "Edit Schedule Item",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (errorMessage != null) errorMessage = null
                    },
                    label = { Text("Schedule Title *") },
                    placeholder = { Text("e.g. College, Lunch, Study, Dinner") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("event_title_input"),
                    isError = errorMessage != null
                )

                if (errorMessage != null) {
                    Text(text = errorMessage ?: "", color = RosePriority, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Location (Optional)") },
                    placeholder = { Text("e.g. Hall 302 or Deep work on DSA") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // Date Selection
                Text("Schedule Date", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedEpochDay == todayEpoch,
                        onClick = { selectedEpochDay = todayEpoch },
                        label = { Text("Today") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedEpochDay == todayEpoch + 1,
                        onClick = { selectedEpochDay = todayEpoch + 1 },
                        label = { Text("Tomorrow") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1.2f),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("Pick Date 📅", fontSize = 12.sp)
                    }
                }

                // Date Display
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Date: ${LocalDate.ofEpochDay(selectedEpochDay).format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy"))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(6.dp),
                        textAlign = TextAlign.Center
                    )
                }

                // Times (Start and End) in AM/PM
                Text("Schedule Times (AM/PM)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showStartTimePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Start Time", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(startTime, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    OutlinedButton(
                        onClick = { showEndTimePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("End Time", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(endTime, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Study", "Personal", "Assignment", "Project", "Work").forEach { c ->
                        FilterChip(
                            selected = category == c,
                            onClick = { category = c },
                            label = { Text(c) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Please enter a schedule title"
                    } else {
                        onSave(title, description, selectedEpochDay, startTime, endTime, category)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_event_button")
            ) {
                Text(if (eventToEdit == null) "Add Schedule" else "Save Changes")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showDatePicker) {
        CustomDatePickerModal(
            initialEpochDay = selectedEpochDay,
            onDateSelected = { selectedEpochDay = it },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showStartTimePicker) {
        AmPmTimePickerModal(
            initialTime = startTime,
            onTimeSelected = { startTime = it },
            onDismiss = { showStartTimePicker = false }
        )
    }

    if (showEndTimePicker) {
        AmPmTimePickerModal(
            initialTime = endTime,
            onTimeSelected = { endTime = it },
            onDismiss = { showEndTimePicker = false }
        )
    }
}

@Composable
fun AddControlledAppModal(
    onSave: (appName: String, packageName: String, searchActionType: String, searchUrlTemplate: String) -> Unit,
    onDismiss: () -> Unit
) {
    var appName by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var searchActionType by remember { mutableStateOf("SEARCH_INTENT") }
    var searchUrlTemplate by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val presetApps = listOf(
        Triple("YouTube", "com.google.android.youtube", "https://www.youtube.com/results?search_query=%s"),
        Triple("Instagram", "com.instagram.android", "https://www.instagram.com/explore/tags/%s"),
        Triple("Reddit", "com.reddit.frontpage", "https://www.reddit.com/search/?q=%s"),
        Triple("TikTok", "com.zhiliaoapp.musically", "https://www.tiktok.com/search?q=%s"),
        Triple("X (Twitter)", "com.twitter.android", "https://twitter.com/search?q=%s"),
        Triple("Facebook", "com.facebook.katana", "https://www.facebook.com/search/top/?q=%s"),
        Triple("Twitch", "tv.twitch.android.app", "https://www.twitch.tv/search?term=%s"),
        Triple("Discord", "com.discord", "https://discord.com")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add App to Focus Search", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Put an application under Focus Search control so that opening it presents a focused academic search instead of feed recommendations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text("Quick Preset Apps", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    presetApps.forEach { (name, pkg, template) ->
                        FilterChip(
                            selected = packageName == pkg,
                            onClick = {
                                appName = name
                                packageName = pkg
                                searchUrlTemplate = template
                                searchActionType = when (name) {
                                    "YouTube" -> "YOUTUBE"
                                    "Instagram" -> "INSTAGRAM"
                                    "Reddit" -> "REDDIT"
                                    "TikTok" -> "TIKTOK"
                                    "X (Twitter)" -> "TWITTER"
                                    "Facebook" -> "FACEBOOK"
                                    else -> "SEARCH_INTENT"
                                }
                            },
                            label = { Text(name) }
                        )
                    }
                }

                OutlinedTextField(
                    value = appName,
                    onValueChange = {
                        appName = it
                        if (errorMessage != null) errorMessage = null
                    },
                    label = { Text("Application Name *") },
                    placeholder = { Text("e.g. YouTube or Reddit") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("app_name_input")
                )

                OutlinedTextField(
                    value = packageName,
                    onValueChange = {
                        packageName = it
                        if (errorMessage != null) errorMessage = null
                    },
                    label = { Text("Android Package Name *") },
                    placeholder = { Text("e.g. com.google.android.youtube") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("app_package_input")
                )

                OutlinedTextField(
                    value = searchUrlTemplate,
                    onValueChange = { searchUrlTemplate = it },
                    label = { Text("Search URL Template (Fallback)") },
                    placeholder = { Text("https://example.com/search?q=%s") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(text = errorMessage ?: "", color = RosePriority, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (appName.isBlank() || packageName.isBlank()) {
                        errorMessage = "Please enter application and package name"
                    } else {
                        onSave(appName, packageName, searchActionType, searchUrlTemplate)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_controlled_app_button")
            ) {
                Text("Add App")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddFocusSearchSiteModal(
    onSave: (name: String, domain: String, searchTemplate: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var domain by remember { mutableStateOf("") }
    var template by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Focus Search Website", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Configure a website to restrict feed browsing and enable search-only access.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (domain.isBlank() && it.isNotBlank()) {
                            domain = it.lowercase().replace(" ", "") + ".com"
                        }
                    },
                    label = { Text("Website Name *") },
                    placeholder = { Text("e.g. Coursera or StackOverflow") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("site_name_input")
                )
                OutlinedTextField(
                    value = domain,
                    onValueChange = { domain = it },
                    label = { Text("Domain *") },
                    placeholder = { Text("e.g. stackoverflow.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("site_domain_input")
                )
                OutlinedTextField(
                    value = template,
                    onValueChange = { template = it },
                    label = { Text("Search URL Template (Optional)") },
                    placeholder = { Text("https://domain.com/search?q=%s") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMessage != null) {
                    Text(text = errorMessage ?: "", color = RosePriority, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || domain.isBlank()) {
                        errorMessage = "Please enter website name and domain"
                    } else {
                        onSave(name, domain, template)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_site_button")
            ) {
                Text("Add Website")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddAppControlModal(
    onSave: (appName: String, domain: String, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var appName by remember { mutableStateOf("") }
    var domain by remember { mutableStateOf("") }
    var startHour by remember { mutableIntStateOf(9) }
    var startMinute by remember { mutableIntStateOf(0) }
    var isStartPm by remember { mutableStateOf(false) } // 9:00 AM default
    var endHour by remember { mutableIntStateOf(5) }
    var endMinute by remember { mutableIntStateOf(0) }
    var isEndPm by remember { mutableStateOf(true) } // 5:00 PM default
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule App / Site Control", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Specify hours when this distracting application or website will be strictly blocked.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = appName,
                    onValueChange = {
                        appName = it
                        if (domain.isBlank() && it.isNotBlank()) {
                            domain = it.lowercase().replace(" ", "") + ".com"
                        }
                    },
                    label = { Text("Application / Site Name *") },
                    placeholder = { Text("e.g. TikTok or YouTube") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("control_app_name_input")
                )
                OutlinedTextField(
                    value = domain,
                    onValueChange = { domain = it },
                    label = { Text("Domain or Package Name") },
                    placeholder = { Text("e.g. youtube.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Control Window (12-Hour AM/PM)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)

                // Start Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Start:", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(36.dp))
                    OutlinedTextField(
                        value = "$startHour",
                        onValueChange = { it.toIntOrNull()?.let { h -> if (h in 1..12) startHour = h } },
                        label = { Text("Hr (1-12)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = String.format("%02d", startMinute),
                        onValueChange = { it.toIntOrNull()?.let { m -> if (m in 0..59) startMinute = m } },
                        label = { Text("Min") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !isStartPm,
                        onClick = { isStartPm = false },
                        label = { Text("AM") }
                    )
                    FilterChip(
                        selected = isStartPm,
                        onClick = { isStartPm = true },
                        label = { Text("PM") }
                    )
                }

                // End Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("End:", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(36.dp))
                    OutlinedTextField(
                        value = "$endHour",
                        onValueChange = { it.toIntOrNull()?.let { h -> if (h in 1..12) endHour = h } },
                        label = { Text("Hr (1-12)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = String.format("%02d", endMinute),
                        onValueChange = { it.toIntOrNull()?.let { m -> if (m in 0..59) endMinute = m } },
                        label = { Text("Min") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !isEndPm,
                        onClick = { isEndPm = false },
                        label = { Text("AM") }
                    )
                    FilterChip(
                        selected = isEndPm,
                        onClick = { isEndPm = true },
                        label = { Text("PM") }
                    )
                }

                if (errorMessage != null) {
                    Text(text = errorMessage ?: "", color = RosePriority, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (appName.isBlank()) {
                        errorMessage = "Please enter an application or website name"
                    } else {
                        val h24Start = if (isStartPm) {
                            if (startHour == 12) 12 else startHour + 12
                        } else {
                            if (startHour == 12) 0 else startHour
                        }
                        val h24End = if (isEndPm) {
                            if (endHour == 12) 12 else endHour + 12
                        } else {
                            if (endHour == 12) 0 else endHour
                        }
                        onSave(appName, domain, h24Start, startMinute, h24End, endMinute)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_control_button")
            ) {
                Text("Schedule Control")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AppShieldBlockDialog(
    appName: String,
    onDismiss: () -> Unit,
    onOpenFocus: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = RosePriority,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "Focus Shield Active 🛡️",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "\"$appName\" is currently restricted by your active Study Flow schedule.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "“Discipline is choosing between what you want now and what you want most.”",
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onOpenFocus()
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                Text("Start Focus Session")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close Shield")
            }
        }
    )
}
