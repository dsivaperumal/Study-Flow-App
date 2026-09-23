package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.TaskCard
import com.example.ui.components.TaskModal
import com.example.ui.theme.IndigoPrimary
import com.example.ui.viewmodel.StudyFlowViewModel
import java.time.LocalDate

@Composable
fun TasksScreen(
    viewModel: StudyFlowViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsState()
    val activeFilter by viewModel.taskFilter.collectAsState()
    val searchQuery by viewModel.taskSearchQuery.collectAsState()

    var showAddModal by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskEntity?>(null) }

    val todayEpoch = remember { LocalDate.now().toEpochDay() }

    val filterOptions = listOf("All", "Today", "Upcoming", "Completed", "High Priority")

    // Filter and search
    val filteredTasks = tasks.filter { task ->
        val matchesSearch = if (searchQuery.isBlank()) true else {
            task.title.contains(searchQuery, ignoreCase = true) ||
            task.category.contains(searchQuery, ignoreCase = true) ||
            task.notes.contains(searchQuery, ignoreCase = true)
        }

        val matchesFilter = when (activeFilter) {
            "Today" -> task.dueDateEpochDay == todayEpoch
            "Upcoming" -> task.dueDateEpochDay > todayEpoch && !task.isCompleted
            "Completed" -> task.isCompleted
            "High Priority" -> task.priority == "High" && !task.isCompleted
            else -> true
        }

        matchesSearch && matchesFilter
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddModal = true },
                containerColor = IndigoPrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_task")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Screen Title & Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tasks & To-Do",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${tasks.count { !it.isCompleted }} pending · ${tasks.count { it.isCompleted }} completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("header_add_task_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Task")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setTaskSearchQuery(it) },
                placeholder = { Text("Search tasks, categories, notes...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setTaskSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_search_field")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Horizontal Scroll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { filter ->
                    FilterChip(
                        selected = activeFilter == filter,
                        onClick = { viewModel.setTaskFilter(filter) },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoPrimary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("filter_chip_$filter")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Task List or Empty State
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = IndigoPrimary.copy(alpha = 0.5f),
                                modifier = Modifier.size(54.dp)
                            )
                            Text(
                                text = "No tasks yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (searchQuery.isNotBlank()) "No tasks match \"$searchQuery\"."
                                else "Add your first task and start planning your day.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { showAddModal = true },
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                            ) {
                                Text("+ Add Task")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                            onEdit = { taskToEdit = task },
                            onDelete = { taskToDelete = task }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                }
            }
        }
    }

    // Modal dialogs
    if (showAddModal) {
        TaskModal(
            onSave = { title, notes, priority, category, epochDay, dueTime ->
                viewModel.addTask(title, notes, priority, category, epochDay, dueTime)
                showAddModal = false
            },
            onDismiss = { showAddModal = false }
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
            message = "Are you sure you want to permanently delete \"${taskToDelete!!.title}\"?",
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
