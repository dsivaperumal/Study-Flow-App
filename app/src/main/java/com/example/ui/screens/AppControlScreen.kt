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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppControlEntity
import com.example.ui.components.AddAppControlModal
import com.example.ui.components.AppShieldBlockDialog
import com.example.ui.components.ConfirmationDialog
import com.example.ui.navigation.Screen
import com.example.ui.theme.AmberStreak
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RosePriority
import com.example.ui.viewmodel.StudyFlowViewModel

@Composable
fun AppControlScreen(
    viewModel: StudyFlowViewModel,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val controls by viewModel.appControls.collectAsState()
    val blockedApp by viewModel.blockedAppAttempt.collectAsState()

    var showAddModal by remember { mutableStateOf(false) }
    var controlToDelete by remember { mutableStateOf<AppControlEntity?>(null) }

    val activeControlsCount = controls.count { viewModel.isAppControlActiveNow(it) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "App & Site Control",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Automated schedules that block distracting apps and sites.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { showAddModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("add_schedule_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Schedule")
            }
        }

        // Active Status Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (activeControlsCount > 0) {
                    RosePriority.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                    if (activeControlsCount > 0) RosePriority.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (activeControlsCount > 0) RosePriority.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (activeControlsCount > 0) Icons.Default.Lock else Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (activeControlsCount > 0) RosePriority else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (activeControlsCount > 0) "Focus Shield: $activeControlsCount Active Now" else "All Scheduled Controls Standing By",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (activeControlsCount > 0) RosePriority else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (activeControlsCount > 0) "Distracting sites/apps are restricted right now."
                        else "No apps currently restricted. Waiting for configured time slots.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Schedule List
        Text(
            text = "Configured Schedules (${controls.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (controls.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("No app control schedules yet.")
                    Button(onClick = { showAddModal = true }) {
                        Text("+ Add Schedule")
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                controls.forEach { control ->
                    val isActiveNow = viewModel.isAppControlActiveNow(control)
                    val statusText = when {
                        !control.isEnabled -> "Disabled"
                        isActiveNow -> "Active Now"
                        else -> "Scheduled"
                    }
                    val statusColor = when {
                        !control.isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant
                        isActiveNow -> RosePriority
                        else -> IndigoPrimary
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (isActiveNow) RosePriority.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(if (control.isEnabled) IndigoPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (control.isEnabled) Icons.Default.Block else Icons.Default.Security,
                                            contentDescription = null,
                                            tint = if (control.isEnabled) IndigoPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = control.appName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = control.targetDomain,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Independent Toggle Switch
                                    Switch(
                                        checked = control.isEnabled,
                                        onCheckedChange = { isChecked ->
                                            viewModel.toggleAppControl(control, isChecked)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = IndigoPrimary,
                                            checkedTrackColor = IndigoPrimary.copy(alpha = 0.35f)
                                        ),
                                        modifier = Modifier.testTag("app_control_toggle_${control.appName.lowercase().replace(" ", "_")}")
                                    )

                                    IconButton(
                                        onClick = { controlToDelete = control },
                                        modifier = Modifier.testTag("delete_control_${control.appName.lowercase().replace(" ", "_")}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove ${control.appName}",
                                            tint = RosePriority.copy(alpha = 0.8f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (control.isEnabled) EmeraldAccent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = if (control.isEnabled) "CONTROL ON" else "CONTROL OFF",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (control.isEnabled) EmeraldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                            .testTag("app_control_status_${control.appName.lowercase().replace(" ", "_")}")
                                    )
                                }

                                Text(
                                    text = "${String.format("%02d:%02d", control.startHour, control.startMinute)} – ${String.format("%02d:%02d", control.endHour, control.endMinute)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Focus Shield Simulator Trigger Button
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { viewModel.triggerBlockedAppAttempt(control.appName) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test Focus Shield on ${control.appName}")
                            }
                        }
                    }
                }

                // Centered "+ Add App" Button as requested in prompt
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { showAddModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("add_app_bottom_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("+ Add App", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Modal dialogs
    if (showAddModal) {
        AddAppControlModal(
            onSave = { appName, domain, startHour, startMinute, endHour, endMinute ->
                viewModel.addAppControl(appName, domain, startHour, startMinute, endHour, endMinute)
                showAddModal = false
            },
            onDismiss = { showAddModal = false }
        )
    }

    if (controlToDelete != null) {
        ConfirmationDialog(
            title = "Delete Control Schedule",
            message = "Remove schedule for \"${controlToDelete!!.appName}\"?",
            confirmButtonText = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteAppControl(controlToDelete!!)
                controlToDelete = null
            },
            onDismiss = { controlToDelete = null }
        )
    }

    if (blockedApp != null) {
        AppShieldBlockDialog(
            appName = blockedApp ?: "",
            onDismiss = { viewModel.dismissBlockedAppAttempt() },
            onOpenFocus = {
                viewModel.dismissBlockedAppAttempt()
                onNavigate(Screen.Focus)
            }
        )
    }
}
