package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import com.example.service.FocusSearchAccessibilityService
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BlockedAppEntity
import com.example.ui.theme.AmberStreak
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RosePriority
import com.example.ui.viewmodel.StudyFlowViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BlockShortsPageScreen(viewModel: StudyFlowViewModel) {
    val context = LocalContext.current
    val blockedApps by viewModel.blockedApps.collectAsState()
    val isGlobalStrictMode by viewModel.globalStrictMode.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Blocking", "Not Blocking"

    var showAddAppDialog by remember { mutableStateOf(false) }
    var appToEdit by remember { mutableStateOf<BlockedAppEntity?>(null) }
    var strictModeConfirmApp by remember { mutableStateOf<BlockedAppEntity?>(null) }
    var appToDelete by remember { mutableStateOf<BlockedAppEntity?>(null) }

    // Dynamic counts
    val activeCount = blockedApps.count { it.enabled }
    val availableCount = blockedApps.count { !it.enabled }

    val filteredApps = blockedApps.filter { app ->
        val matchesSearch = searchQuery.isBlank() ||
                app.name.contains(searchQuery, ignoreCase = true) ||
                app.contentType.contains(searchQuery, ignoreCase = true) ||
                app.url.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "Blocking" -> app.enabled
            "Not Blocking" -> !app.enabled
            else -> true
        }

        matchesSearch && matchesFilter
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("block_shorts_page_container"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Header Section ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("block_shorts_header_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(IndigoPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = IndigoPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Block Shorts",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("block_shorts_header_title")
                            )
                            Text(
                                text = "Control short-form content and protect your focus.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.testTag("block_shorts_header_subtitle")
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = IndigoPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Turn blocking ON when you want to avoid short-form content. Turn it OFF whenever you want to access it again.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp,
                                modifier = Modifier.testTag("block_shorts_header_explanation")
                            )
                        }
                    }
                }
            }
        }

        // --- 1B. Accessibility Service Status Banner ---
        item {
            val isAccessibilityEnabled = remember(context) {
                FocusSearchAccessibilityService.isAccessibilityServiceEnabled(context)
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("accessibility_status_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAccessibilityEnabled) EmeraldAccent.copy(alpha = 0.1f) else AmberStreak.copy(alpha = 0.12f)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isAccessibilityEnabled) EmeraldAccent.copy(alpha = 0.4f) else AmberStreak.copy(alpha = 0.5f)
                    )
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isAccessibilityEnabled) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isAccessibilityEnabled) EmeraldAccent else AmberStreak,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isAccessibilityEnabled) "Auto-Block Protection: Active" else "Auto-Block Protection: Setup Needed",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isAccessibilityEnabled)
                                    "Study Flow is actively monitoring and blocking short-form content."
                                else
                                    "Enable Study Flow in Accessibility Settings to intercept short-form feeds in apps.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (!isAccessibilityEnabled) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Ignore if not supported in container
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberStreak),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("enable_accessibility_button")
                        ) {
                            Text("Enable", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- 2. Dashboard Summary & Global Strict Mode ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Blocking Active Stat Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stat_card_blocking_active"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = EmeraldAccent.copy(alpha = 0.1f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = EmeraldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Blocking Active",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldAccent
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$activeCount ${if (activeCount == 1) "app" else "apps"}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("count_blocking_active")
                        )
                    }
                }

                // Not Blocking Stat Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stat_card_not_blocking"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Not Blocking",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$availableCount ${if (availableCount == 1) "app" else "apps"}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("count_not_blocking")
                        )
                    }
                }
            }
        }

        // --- 3. Global Strict Mode & Add App Bar ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("global_strict_mode_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AmberStreak.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = AmberStreak,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Strict Mode",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Confirm before turning off an active blocker",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isGlobalStrictMode,
                            onCheckedChange = { viewModel.setGlobalStrictMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AmberStreak,
                                checkedTrackColor = AmberStreak.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("strict_mode_toggle")
                        )
                    }
                }
            }
        }

        // --- 4. Controls, Search & Filter ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Managed Apps",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Button(
                        onClick = { showAddAppDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_app_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add App", fontWeight = FontWeight.Bold)
                    }
                }

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search apps...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_apps_input")
                )

                // Filter Chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Blocking", "Not Blocking").forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) },
                            leadingIcon = if (selectedFilter == filter) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IndigoPrimary.copy(alpha = 0.15f),
                                selectedLabelColor = IndigoPrimary
                            ),
                            modifier = Modifier.testTag("filter_chip_${filter.lowercase().replace(" ", "_")}")
                        )
                    }
                }
            }
        }

        // --- 5. Empty State ---
        if (filteredApps.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                        .testTag("empty_state_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Text(
                            text = "No apps added yet.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("empty_state_title")
                        )

                        Text(
                            text = "Add the apps where you want to control short-form content.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = { showAddAppDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("empty_state_add_app_button")
                        ) {
                            Text("+ Add App")
                        }
                    }
                }
            }
        } else {
            // --- 6. Apps List ---
            items(filteredApps, key = { it.id }) { app ->
                AppCard(
                    app = app,
                    onToggle = { newEnabled ->
                        if (!newEnabled && isGlobalStrictMode) {
                            strictModeConfirmApp = app
                        } else {
                            viewModel.toggleBlockedApp(app, newEnabled)
                        }
                    },
                    onOpenSettings = { appToEdit = app },
                    onTestShield = {
                        viewModel.testShortsBlockingScreen(context, "${app.name} ${app.contentType}")
                    }
                )
            }
        }

        // --- 7. Browser & System Transparency Section ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("technical_integration_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = IndigoPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Platform Integration & Transparency",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "• Native Apps: Protected via Study Flow's Android Accessibility Service which detects feeds while keeping long videos and messaging accessible.\n• Web Content: Saved preferences are structured for instant browser content-filter extensions.\n• Privacy: All settings remain 100% offline and stored locally on your device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 19.sp
                    )

                    OutlinedButton(
                        onClick = { viewModel.testShortsBlockingScreen(context, "YouTube Shorts") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("preview_blocked_screen_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Preview Shorts Blocked Screen")
                    }
                }
            }
        }
    }

    // --- Dialogs & Bottom Sheets ---

    // Strict Mode Confirmation Dialog
    strictModeConfirmApp?.let { app ->
        AlertDialog(
            onDismissRequest = { strictModeConfirmApp = null },
            title = {
                Text(
                    text = "Disable Blocker?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Blocking is currently active for ${app.name}. Are you sure you want to disable it?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleBlockedApp(app, false)
                        strictModeConfirmApp = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RosePriority),
                    modifier = Modifier.testTag("confirm_disable_button")
                ) {
                    Text("Disable")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { strictModeConfirmApp = null },
                    modifier = Modifier.testTag("keep_blocking_button")
                ) {
                    Text("Keep Blocking")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Delete App Confirmation Dialog
    appToDelete?.let { app ->
        AlertDialog(
            onDismissRequest = { appToDelete = null },
            title = { Text("Delete App?", fontWeight = FontWeight.Bold) },
            text = { Text("Remove ${app.name} from Block Shorts? You can add it back anytime.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteBlockedApp(app)
                        appToDelete = null
                        if (appToEdit?.id == app.id) appToEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RosePriority)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { appToDelete = null }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Add App Bottom Sheet / Dialog
    if (showAddAppDialog) {
        AddAppDialog(
            onDismiss = { showAddAppDialog = false },
            onAdd = { name, url, contentType, enabled ->
                viewModel.addBlockedApp(
                    name = name,
                    url = url,
                    contentType = contentType,
                    enabled = enabled
                )
                showAddAppDialog = false
            }
        )
    }

    // App Details / Settings Bottom Sheet
    appToEdit?.let { app ->
        AppSettingsSheet(
            app = app,
            onDismiss = { appToEdit = null },
            onSave = { updated ->
                viewModel.updateBlockedApp(updated)
                appToEdit = null
            },
            onDelete = {
                appToDelete = app
            }
        )
    }
}

/**
 * Individual App Card with independent ON/OFF control and status indicator.
 */
@Composable
private fun AppCard(
    app: BlockedAppEntity,
    onToggle: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onTestShield: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_card_${app.name.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // App Identity & Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    AppPlatformIcon(appName = app.name)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = app.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = app.contentType,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Controls: Settings Cog & Toggle Switch
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("settings_button_${app.name.lowercase().replace(" ", "_")}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings for ${app.name}",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = app.enabled,
                        onCheckedChange = { onToggle(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = IndigoPrimary,
                            checkedTrackColor = IndigoPrimary.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.testTag("toggle_${app.name.lowercase().replace(" ", "_")}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clear Text Status Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (app.enabled) EmeraldAccent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (app.enabled) "BLOCKING ON" else "BLOCKING OFF",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (app.enabled) EmeraldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            .testTag("status_text_${app.name.lowercase().replace(" ", "_")}")
                    )
                }

                OutlinedButton(
                    onClick = onTestShield,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("test_shield_button_${app.name.lowercase().replace(" ", "_")}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Test Shield",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Visual Platform Icon badge helper.
 */
@Composable
private fun AppPlatformIcon(appName: String) {
    val lower = appName.lowercase()
    val (bgColor, iconColor) = when {
        lower.contains("youtube") -> Pair(Color(0xFFFF0000).copy(alpha = 0.12f), Color(0xFFFF0000))
        lower.contains("instagram") -> Pair(Color(0xFFE1306C).copy(alpha = 0.12f), Color(0xFFE1306C))
        lower.contains("facebook") -> Pair(Color(0xFF1877F2).copy(alpha = 0.12f), Color(0xFF1877F2))
        lower.contains("tiktok") -> Pair(Color(0xFF000000).copy(alpha = 0.12f), MaterialTheme.colorScheme.onSurface)
        lower.contains("reddit") -> Pair(Color(0xFFFF4500).copy(alpha = 0.12f), Color(0xFFFF4500))
        else -> Pair(IndigoPrimary.copy(alpha = 0.12f), IndigoPrimary)
    }

    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when {
                lower.contains("youtube") -> Icons.Default.PlayArrow
                lower.contains("instagram") -> Icons.Default.Videocam
                lower.contains("facebook") -> Icons.Default.Videocam
                lower.contains("tiktok") -> Icons.Default.PlayArrow
                else -> Icons.Default.Shield
            },
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Add App Dialog with fields for name, url, content type, and initial ON/OFF toggle.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddAppDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, url: String, contentType: String, enabled: Boolean) -> Unit
) {
    var appName by remember { mutableStateOf("") }
    var appUrl by remember { mutableStateOf("") }
    var contentType by remember { mutableStateOf("Short-form videos") }
    var blockEnabled by remember { mutableStateOf(true) }

    val quickSuggestions = listOf(
        Triple("YouTube", "youtube.com", "Shorts"),
        Triple("Instagram", "instagram.com", "Reels"),
        Triple("Facebook", "facebook.com", "Reels"),
        Triple("TikTok", "tiktok.com", "Short-form videos"),
        Triple("Reddit", "reddit.com", "Distraction feed"),
        Triple("Snapchat", "snapchat.com", "Spotlight"),
        Triple("Pinterest", "pinterest.com", "Watch feed")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Add App", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Quick suggestions
                Text(
                    text = "Quick Presets:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickSuggestions.forEach { (name, url, type) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                appName = name
                                appUrl = url
                                contentType = type
                            }
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = appName,
                    onValueChange = { appName = it },
                    label = { Text("App Name") },
                    placeholder = { Text("e.g. YouTube") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_app_name_input")
                )

                OutlinedTextField(
                    value = appUrl,
                    onValueChange = { appUrl = it },
                    label = { Text("Website / App URL") },
                    placeholder = { Text("e.g. youtube.com") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_app_url_input")
                )

                OutlinedTextField(
                    value = contentType,
                    onValueChange = { contentType = it },
                    label = { Text("Content Type") },
                    placeholder = { Text("e.g. Shorts / Reels / Short videos") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_app_content_type_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Block",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (blockEnabled) "ON" else "OFF",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (blockEnabled) EmeraldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = blockEnabled,
                        onCheckedChange = { blockEnabled = it },
                        modifier = Modifier.testTag("add_app_block_toggle")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (appName.isNotBlank()) {
                        onAdd(appName, appUrl, contentType, blockEnabled)
                    }
                },
                enabled = appName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                modifier = Modifier.testTag("save_add_app_button")
            ) {
                Text("Add App")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_add_app_button")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(18.dp)
    )
}

/**
 * App Details / Settings Modal Bottom Sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppSettingsSheet(
    app: BlockedAppEntity,
    onDismiss: () -> Unit,
    onSave: (BlockedAppEntity) -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var enabled by remember(app) { mutableStateOf(app.enabled) }
    var scheduleEnabled by remember(app) { mutableStateOf(app.scheduleEnabled) }
    var startTime by remember(app) { mutableStateOf(app.startTime) }
    var endTime by remember(app) { mutableStateOf(app.endTime) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .testTag("app_settings_sheet"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppPlatformIcon(appName = app.name)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Content: ${app.contentType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            // Block ON / OFF Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Block ${app.contentType}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (enabled) "Blocking Active" else "Blocking Disabled",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (enabled) EmeraldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = { enabled = it },
                    modifier = Modifier.testTag("sheet_block_toggle")
                )
            }

            // Schedule Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Schedule",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                // Schedule Mode Selector: "Always" vs "Custom Schedule"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !scheduleEnabled,
                        onClick = { scheduleEnabled = false },
                        label = { Text("Always") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("schedule_always_chip")
                    )
                    FilterChip(
                        selected = scheduleEnabled,
                        onClick = { scheduleEnabled = true },
                        label = { Text("Custom Schedule") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("schedule_custom_chip")
                    )
                }

                if (scheduleEnabled) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = startTime,
                                onValueChange = { startTime = it },
                                label = { Text("Start Time (HH:MM)") },
                                placeholder = { Text("18:00") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = endTime,
                                onValueChange = { endTime = it },
                                label = { Text("End Time (HH:MM)") },
                                placeholder = { Text("22:00") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RosePriority),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sheet_delete_button")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete App")
                }

                Button(
                    onClick = {
                        val updated = app.copy(
                            enabled = enabled,
                            scheduleEnabled = scheduleEnabled,
                            startTime = startTime,
                            endTime = endTime
                        )
                        onSave(updated)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sheet_save_button")
                ) {
                    Text("Save")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
