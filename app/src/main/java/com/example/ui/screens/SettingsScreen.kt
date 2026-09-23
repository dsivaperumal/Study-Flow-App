package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.ConfirmationDialog
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RosePriority
import com.example.ui.viewmodel.StudyFlowViewModel

@Composable
fun SettingsScreen(
    viewModel: StudyFlowViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.userSettings.collectAsState()
    val context = LocalContext.current

    var showClearDataModal by remember { mutableStateOf(false) }
    var showResetSampleDataModal by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Preferences & Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Configure appearance, focus timer lengths, and system alerts.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // --- Theme & Appearance ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SettingsBrightness, contentDescription = null, tint = IndigoPrimary)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = "Appearance & Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Light", "Dark", "System").forEach { themeMode ->
                        FilterChip(
                            selected = settings.themeMode == themeMode,
                            onClick = {
                                viewModel.updateSettings(settings.copy(themeMode = themeMode))
                            },
                            label = { Text(themeMode) },
                            leadingIcon = {
                                when (themeMode) {
                                    "Light" -> Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(16.dp))
                                    "Dark" -> Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(16.dp))
                                    else -> Icon(Icons.Default.SettingsBrightness, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IndigoPrimary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_chip_${themeMode.lowercase()}")
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                )

                // Font Size Accessibility Control (Global font scaling)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Font Size (Accessibility)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${settings.fontSizePercentage}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPrimary
                        )
                    }

                    Text(
                        text = "Scales all text, controls, and elements across the entire app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    val fontSizes = listOf(
                        90 to "90%",
                        100 to "100%",
                        110 to "110%",
                        120 to "120%"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        fontSizes.forEach { (percent, label) ->
                            val isSelected = settings.fontSizePercentage == percent
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.updateSettings(settings.copy(fontSizePercentage = percent))
                                },
                                label = {
                                    Text(
                                        text = if (percent == 100) "$label (Default)" else label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IndigoPrimary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("font_size_$percent")
                            )
                        }
                    }
                }
            }
        }

        // --- Focus Timer Durations ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = IndigoPrimary)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = "Focus Timer Durations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                // Focus duration slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Focus Session Length", style = MaterialTheme.typography.bodyMedium)
                        Text("${settings.focusDurationMinutes} min", fontWeight = FontWeight.Bold, color = IndigoPrimary)
                    }
                    Slider(
                        value = settings.focusDurationMinutes.toFloat(),
                        onValueChange = { viewModel.updateSettings(settings.copy(focusDurationMinutes = it.toInt())) },
                        valueRange = 10f..60f,
                        steps = 9
                    )
                }

                // Short break slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Short Break Length", style = MaterialTheme.typography.bodyMedium)
                        Text("${settings.shortBreakMinutes} min", fontWeight = FontWeight.Bold, color = EmeraldAccent)
                    }
                    Slider(
                        value = settings.shortBreakMinutes.toFloat(),
                        onValueChange = { viewModel.updateSettings(settings.copy(shortBreakMinutes = it.toInt())) },
                        valueRange = 2f..15f,
                        steps = 12
                    )
                }

                // Long break slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Long Break Length", style = MaterialTheme.typography.bodyMedium)
                        Text("${settings.longBreakMinutes} min", fontWeight = FontWeight.Bold, color = IndigoPrimary)
                    }
                    Slider(
                        value = settings.longBreakMinutes.toFloat(),
                        onValueChange = { viewModel.updateSettings(settings.copy(longBreakMinutes = it.toInt())) },
                        valueRange = 10f..30f,
                        steps = 3
                    )
                }

                // Automation Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-start Breaks", fontWeight = FontWeight.Medium)
                        Text("Start break timer automatically after focus block", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = settings.autoStartBreaks,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(autoStartBreaks = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldAccent)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-start Focus", fontWeight = FontWeight.Medium)
                        Text("Begin focus timer automatically when break finishes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = settings.autoStartFocus,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(autoStartFocus = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldAccent)
                    )
                }
            }
        }

        // --- Notifications & Feedback ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = IndigoPrimary)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = "Notifications & Audio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("System & In-App Alerts", fontWeight = FontWeight.Medium)
                        Text("Receive session and task milestone reminders", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = settings.notificationsEnabled,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(notificationsEnabled = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldAccent)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Audio Alert Chime", fontWeight = FontWeight.Medium)
                        Text("Sound tone on timer completion", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = settings.soundEnabled,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(soundEnabled = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldAccent)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Haptic Vibration", fontWeight = FontWeight.Medium)
                        Text("Gentle buzz when focus cycles change", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = settings.vibrationEnabled,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(vibrationEnabled = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldAccent)
                    )
                }
            }
        }

        // --- Data Management & Backup ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Local Data Storage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "Study Flow data is persisted locally in an offline Room database.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = {
                        val exportedJson = viewModel.exportDataAsJson()
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Study Flow Backup", exportedJson)
                        clipboard.setPrimaryClip(clip)
                        viewModel.showNotification("Data Exported", "Study Flow JSON backup copied to clipboard.", "DATA")
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("export_data_button")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Export Data (Copy JSON to Clipboard)")
                }

                OutlinedButton(
                    onClick = { showResetSampleDataModal = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Reload Default Sample Tasks & Schedule")
                }

                Button(
                    onClick = { showClearDataModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = RosePriority),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("clear_data_button")
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Clear All Local Data")
                }
            }
        }
    }

    if (showClearDataModal) {
        ConfirmationDialog(
            title = "Clear All Local Data",
            message = "This will permanently erase all tasks, calendar events, focus history, and app control rules. Continue?",
            confirmButtonText = "Erase All",
            isDestructive = true,
            onConfirm = {
                viewModel.clearAllData()
                showClearDataModal = false
            },
            onDismiss = { showClearDataModal = false }
        )
    }

    if (showResetSampleDataModal) {
        ConfirmationDialog(
            title = "Reload Sample Data",
            message = "This will refresh your tasks and schedule with clean starter productivity templates. Continue?",
            confirmButtonText = "Reload Sample Data",
            isDestructive = false,
            onConfirm = {
                viewModel.seedSampleData()
                showResetSampleDataModal = false
            },
            onDismiss = { showResetSampleDataModal = false }
        )
    }
}
