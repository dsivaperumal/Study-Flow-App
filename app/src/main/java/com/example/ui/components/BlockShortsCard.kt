package com.example.ui.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.FocusSearchAccessibilityService
import com.example.ui.theme.AmberStreak
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RosePriority
import com.example.ui.viewmodel.StudyFlowViewModel
import com.example.utils.TimeUtils

/**
 * Dedicated Block Shorts feature section embedded inside Focus Mode.
 * Controls YouTube Shorts & Instagram Reels blocking, schedule windows, strict mode,
 * and system accessibility permissions.
 */
@Composable
fun BlockShortsCard(
    viewModel: StudyFlowViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val config by viewModel.blockShortsSettings.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val timerMode by viewModel.timerMode.collectAsState()
    val isActiveNow = viewModel.isBlockShortsActiveNow()

    val isAccessibilityEnabled = remember(context) {
        FocusSearchAccessibilityService.isAccessibilityServiceEnabled(context)
    }

    var showScheduleDialog by remember { mutableStateOf(false) }
    var showStrictAlert by remember { mutableStateOf(false) }

    val isStrictLocked = config.isStrictModeEnabled && isTimerRunning && timerMode == "Focus"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("block_shorts_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isActiveNow) EmeraldAccent.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Title & Master Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActiveNow) EmeraldAccent.copy(alpha = 0.15f)
                                else IndigoPrimary.copy(alpha = 0.12f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = if (isActiveNow) EmeraldAccent else IndigoPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Block Shorts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isActiveNow) EmeraldAccent.copy(alpha = 0.15f)
                                else if (config.isEnabled) IndigoPrimary.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = if (isActiveNow) "ACTIVE NOW"
                                    else if (config.isEnabled) "ARMED"
                                    else "OFF",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActiveNow) EmeraldAccent
                                    else if (config.isEnabled) IndigoPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "Shield against short-form video feeds",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Master Toggle with Strict Mode check
                Switch(
                    checked = config.isEnabled,
                    onCheckedChange = { desired ->
                        if (isStrictLocked && !desired) {
                            showStrictAlert = true
                        } else {
                            viewModel.toggleBlockShortsMaster(desired)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = IndigoPrimary
                    ),
                    modifier = Modifier.testTag("block_shorts_master_switch")
                )
            }

            // Strict Mode Warning Banner if active and locked
            if (isStrictLocked) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AmberStreak.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = AmberStreak,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Strict Mode Active: Protection locked during focus session.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AmberStreak
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = config.isEnabled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Platforms Header
                    Text(
                        text = "Supported Platforms",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // YouTube Shorts Toggle
                    PlatformToggleRow(
                        title = "YouTube Shorts",
                        description = "Blocks Shorts feed & player; keeps standard videos & search",
                        icon = Icons.Default.SmartDisplay,
                        iconTint = RosePriority,
                        isChecked = config.blockYouTubeShorts,
                        isLocked = isStrictLocked,
                        onToggle = { desired ->
                            if (isStrictLocked && !desired) {
                                showStrictAlert = true
                            } else {
                                viewModel.toggleBlockYouTubeShorts(desired)
                            }
                        },
                        testTag = "block_shorts_youtube_switch"
                    )

                    // Instagram Reels Toggle
                    PlatformToggleRow(
                        title = "Instagram Reels",
                        description = "Blocks Reels tab & clips viewer; keeps messaging & profiles",
                        icon = Icons.Default.Videocam,
                        iconTint = Color(0xFFE1306C),
                        isChecked = config.blockInstagramReels,
                        isLocked = isStrictLocked,
                        onToggle = { desired ->
                            if (isStrictLocked && !desired) {
                                showStrictAlert = true
                            } else {
                                viewModel.toggleBlockInstagramReels(desired)
                            }
                        },
                        testTag = "block_shorts_instagram_switch"
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Schedule Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = IndigoPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Scheduled Protection",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (config.isScheduleEnabled) {
                                        val start = TimeUtils.formatAmPm(
                                            if (config.scheduleStartHour % 12 == 0) 12 else config.scheduleStartHour % 12,
                                            config.scheduleStartMinute,
                                            config.scheduleStartHour >= 12
                                        )
                                        val end = TimeUtils.formatAmPm(
                                            if (config.scheduleEndHour % 12 == 0) 12 else config.scheduleEndHour % 12,
                                            config.scheduleEndMinute,
                                            config.scheduleEndHour >= 12
                                        )
                                        "$start – $end"
                                    } else "Disabled (runs during active focus)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = config.isScheduleEnabled,
                            onCheckedChange = { viewModel.toggleBlockShortsSchedule(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = IndigoPrimary
                            ),
                            modifier = Modifier.testTag("block_shorts_schedule_switch")
                        )
                    }

                    if (config.isScheduleEnabled) {
                        OutlinedButton(
                            onClick = { showScheduleDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Configure Schedule Hours")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Strict Mode Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = AmberStreak,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Strict Mode",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Cannot disable blocking during active focus",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = config.isStrictModeEnabled,
                            onCheckedChange = { desired ->
                                if (isStrictLocked && !desired) {
                                    showStrictAlert = true
                                } else {
                                    viewModel.toggleBlockShortsStrictMode(desired)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AmberStreak
                            ),
                            modifier = Modifier.testTag("block_shorts_strict_mode_switch")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // System Permissions & Transparency Section
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isAccessibilityEnabled) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if (isAccessibilityEnabled) EmeraldAccent else AmberStreak,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isAccessibilityEnabled) "Native App Blocker Active" else "Permission Required",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isAccessibilityEnabled) EmeraldAccent else AmberStreak
                                    )
                                }

                                if (!isAccessibilityEnabled) {
                                    TextButton(
                                        onClick = {
                                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                        }
                                    ) {
                                        Text("Enable", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }

                            Text(
                                text = "• Mobile System Blocker: Uses Android Accessibility Service to detect Shorts & Reels inside native apps.\n• Web Blocker: Monitors browser address bars to block short-form URLs.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // Interactive Test Trigger Button
                    OutlinedButton(
                        onClick = {
                            viewModel.testShortsBlockingScreen(context, "YouTube Shorts")
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_shorts_blocking_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Preview Shorts Blocking Screen")
                    }
                }
            }
        }
    }

    // Schedule Dialog
    if (showScheduleDialog) {
        ScheduleConfigDialog(
            currentStartHour = config.scheduleStartHour,
            currentStartMinute = config.scheduleStartMinute,
            currentEndHour = config.scheduleEndHour,
            currentEndMinute = config.scheduleEndMinute,
            onDismiss = { showScheduleDialog = false },
            onSave = { sh, sm, eh, em ->
                viewModel.setBlockShortsScheduleTime(sh, sm, eh, em)
                showScheduleDialog = false
            }
        )
    }

    // Strict Mode Alert Dialog
    if (showStrictAlert) {
        AlertDialog(
            onDismissRequest = { showStrictAlert = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = AmberStreak,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text("Strict Mode Is Active", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Block Shorts cannot be disabled while a focus session is actively running. Complete or pause your focus timer first."
                )
            },
            confirmButton = {
                Button(
                    onClick = { showStrictAlert = false },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text("Understood")
                }
            }
        )
    }
}

@Composable
private fun PlatformToggleRow(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    isChecked: Boolean,
    isLocked: Boolean,
    onToggle: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = IndigoPrimary
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun ScheduleConfigDialog(
    currentStartHour: Int,
    currentStartMinute: Int,
    currentEndHour: Int,
    currentEndMinute: Int,
    onDismiss: () -> Unit,
    onSave: (Int, Int, Int, Int) -> Unit
) {
    var startHour by remember { mutableIntStateOf(currentStartHour) }
    var startMinute by remember { mutableIntStateOf(currentStartMinute) }
    var endHour by remember { mutableIntStateOf(currentEndHour) }
    var endMinute by remember { mutableIntStateOf(currentEndMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Configure Block Schedule", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Select the daily time window when short-form videos should be blocked automatically:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Quick Presets
                Text(
                    text = "Quick Presets",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            startHour = 18; startMinute = 0; endHour = 22; endMinute = 0
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("6 PM – 10 PM", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = {
                            startHour = 9; startMinute = 0; endHour = 17; endMinute = 0
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("9 AM – 5 PM", fontSize = 11.sp)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val startStr = TimeUtils.formatAmPm(
                            if (startHour % 12 == 0) 12 else startHour % 12,
                            startMinute,
                            startHour >= 12
                        )
                        val endStr = TimeUtils.formatAmPm(
                            if (endHour % 12 == 0) 12 else endHour % 12,
                            endMinute,
                            endHour >= 12
                        )
                        Text(
                            text = "Selected Window: $startStr – $endStr",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = IndigoPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(startHour, startMinute, endHour, endMinute) },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                Text("Save Schedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
