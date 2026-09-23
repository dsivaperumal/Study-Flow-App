package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FocusSearchAppEntity
import com.example.data.model.FocusSearchSiteEntity
import com.example.service.FocusSearchAccessibilityService
import com.example.ui.components.AddControlledAppModal
import com.example.ui.components.AddFocusSearchSiteModal
import com.example.ui.components.ConfirmationDialog
import com.example.ui.theme.AmberStreak
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RosePriority
import com.example.ui.viewmodel.StudyFlowViewModel
import com.example.utils.FocusSearchManager
import java.net.URLEncoder

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FocusSearchScreen(
    viewModel: StudyFlowViewModel,
    modifier: Modifier = Modifier
) {
    val sites by viewModel.focusSearchSites.collectAsState()
    val apps by viewModel.focusSearchApps.collectAsState()
    val settings by viewModel.userSettings.collectAsState()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedTargetName by remember { mutableStateOf<String?>(null) }
    var showAddSiteModal by remember { mutableStateOf(false) }
    var showAddAppModal by remember { mutableStateOf(false) }
    var siteToDelete by remember { mutableStateOf<FocusSearchSiteEntity?>(null) }
    var appToDelete by remember { mutableStateOf<FocusSearchAppEntity?>(null) }
    var searchStatusMessage by remember { mutableStateOf<String?>(null) }
    var selectedSectionTab by remember { mutableIntStateOf(0) }

    val activeSites = sites.filter { it.isEnabled }
    val activeApps = apps.filter { it.isEnabled }

    // Combined target options for the search chip bar
    val availableTargets = remember(activeSites, activeApps) {
        val list = mutableListOf<SearchTargetOption>()
        activeApps.forEach { list.add(SearchTargetOption(it.appName, isApp = true, pkgOrDomain = it.packageName, siteEntity = null, appEntity = it)) }
        activeSites.forEach { list.add(SearchTargetOption(it.name, isApp = false, pkgOrDomain = it.domain, siteEntity = it, appEntity = null)) }
        list
    }

    val currentTarget = availableTargets.find { it.name == selectedTargetName } ?: availableTargets.firstOrNull()

    fun performFocusSearch() {
        if (searchQuery.isBlank()) {
            searchStatusMessage = "Please enter a search query."
            return
        }
        val target = currentTarget
        if (target == null) {
            searchStatusMessage = "Please select or enable a target application or website."
            return
        }

        try {
            if (target.isApp && target.appEntity != null) {
                FocusSearchManager.executeSearch(context, target.appEntity.packageName, searchQuery)
                viewModel.showNotification("Focus Search Launched", "Searching directly in ${target.name} without feed distraction.", "FOCUS")
                searchStatusMessage = "Opened search for \"$searchQuery\" in ${target.name}."
            } else if (target.siteEntity != null) {
                val site = target.siteEntity
                val encodedQuery = URLEncoder.encode(searchQuery.trim(), "UTF-8")
                val targetUrl = if (site.searchUrlTemplate.contains("%s")) {
                    site.searchUrlTemplate.replace("%s", encodedQuery)
                } else {
                    "${site.searchUrlTemplate}$encodedQuery"
                }

                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
                viewModel.showNotification("Focus Search Launched", "Searching directly on ${site.name} without feed distraction.", "FOCUS")
                searchStatusMessage = "Opened search for \"$searchQuery\" on ${site.name}."
            }
        } catch (e: Exception) {
            searchStatusMessage = "Could not launch search. Please check configuration."
        }
    }

    val isAccessibilityEnabled = remember(context) {
        FocusSearchAccessibilityService.isAccessibilityServiceEnabled(context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Focus Search",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Bypass distracting social media feeds. Search only.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (settings.focusSearchEnabled) "Enabled" else "Off",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (settings.focusSearchEnabled) EmeraldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Switch(
                    checked = settings.focusSearchEnabled,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(focusSearchEnabled = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = EmeraldAccent),
                    modifier = Modifier.testTag("focus_search_global_switch")
                )
            }
        }

        // Accessibility Service Detection Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isAccessibilityEnabled) EmeraldAccent.copy(alpha = 0.1f) else AmberStreak.copy(alpha = 0.12f)
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                    if (isAccessibilityEnabled) EmeraldAccent.copy(alpha = 0.4f) else AmberStreak.copy(alpha = 0.6f)
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isAccessibilityEnabled) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isAccessibilityEnabled) EmeraldAccent else AmberStreak,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isAccessibilityEnabled) "Feed Shield Active" else "Foreground Feed Interception",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isAccessibilityEnabled) EmeraldAccent else AmberStreak
                    )
                    Text(
                        text = if (isAccessibilityEnabled) {
                            "Accessibility service is running. Controlled app launches are automatically intercepted."
                        } else {
                            "Enable Accessibility Service in Android Settings to detect when controlled apps (YouTube, Instagram) open."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!isAccessibilityEnabled) {
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.testTag("enable_accessibility_button")
                    ) {
                        Text("Enable", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- Clean Focus Search Box as specified ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("focus_search_portal_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(IndigoPrimary.copy(alpha = 0.5f))
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = IndigoPrimary.copy(alpha = 0.12f),
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Text(
                        text = "STUDY FLOW",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = IndigoPrimary,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "What do you want to search for?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // Select target chips
                Text(
                    text = "Target: ${currentTarget?.name ?: "None"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (availableTargets.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableTargets.forEach { target ->
                            FilterChip(
                                selected = (currentTarget?.name == target.name),
                                onClick = { selectedTargetName = target.name },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (target.isApp) Icons.Default.Apps else Icons.Default.Language,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                label = { Text(target.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IndigoPrimary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                // Input box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        searchStatusMessage = null
                    },
                    placeholder = { Text("Search something...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = IndigoPrimary)
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { performFocusSearch() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("focus_search_input")
                )

                // Search Button
                Button(
                    onClick = { performFocusSearch() },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("focus_search_button")
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Search without Feed", fontWeight = FontWeight.Bold)
                }

                if (searchStatusMessage != null) {
                    Text(
                        text = searchStatusMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldAccent,
                        textAlign = TextAlign.Center
                    )
                }

                Text(
                    text = "“Stay focused. Search only.”",
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Section Tabs: Controlled Apps vs Controlled Websites
        TabRow(
            selectedTabIndex = selectedSectionTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedSectionTab == 0,
                onClick = { selectedSectionTab = 0 },
                text = { Text("Controlled Apps (${apps.size})", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedSectionTab == 1,
                onClick = { selectedSectionTab = 1 },
                text = { Text("Websites (${sites.size})", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        // --- SECTION 0: Controlled Apps ---
        if (selectedSectionTab == 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Controlled Applications",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Intercept app feeds on launch & redirect directly to search.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showAddAppModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_controlled_app_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add App")
                }
            }

            if (apps.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Apps, contentDescription = null, tint = IndigoPrimary.copy(alpha = 0.5f), modifier = Modifier.size(36.dp))
                        Text("No controlled applications added yet.")
                        Text(
                            text = "Add YouTube, Instagram, Reddit, or TikTok to stop infinite feed scrolling.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = { showAddAppModal = true }) {
                            Text("+ Add App to Control")
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    apps.forEach { app ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(IndigoPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Apps,
                                        contentDescription = null,
                                        tint = if (app.isEnabled) IndigoPrimary else MaterialTheme.colorScheme.outline
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.appName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = app.packageName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (app.isEnabled) "Feed Shield Active" else "Disabled",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (app.isEnabled) EmeraldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Switch(
                                    checked = app.isEnabled,
                                    onCheckedChange = { viewModel.toggleFocusSearchApp(app) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = EmeraldAccent),
                                    modifier = Modifier.testTag("switch_app_${app.id}")
                                )

                                IconButton(
                                    onClick = { appToDelete = app },
                                    modifier = Modifier.testTag("delete_app_${app.id}")
                                ) {
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
        }

        // --- SECTION 1: Controlled Websites ---
        if (selectedSectionTab == 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Controlled Websites",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Websites configured for direct feed-free search redirection.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showAddSiteModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_website_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Site")
                }
            }

            if (sites.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("No websites are currently configured.")
                        Button(onClick = { showAddSiteModal = true }) {
                            Text("+ Add Website")
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sites.forEach { site ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = if (site.isEnabled) IndigoPrimary else MaterialTheme.colorScheme.outline
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = site.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = site.domain,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (site.isEnabled) "Focus Search Active" else "Disabled",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (site.isEnabled) EmeraldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Switch(
                                    checked = site.isEnabled,
                                    onCheckedChange = { viewModel.toggleFocusSearchSite(site) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = EmeraldAccent)
                                )

                                IconButton(onClick = { siteToDelete = site }) {
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
        }
    }

    // Modal dialogs
    if (showAddSiteModal) {
        AddFocusSearchSiteModal(
            onSave = { name, domain, template ->
                viewModel.addFocusSearchSite(name, domain, template)
                showAddSiteModal = false
            },
            onDismiss = { showAddSiteModal = false }
        )
    }

    if (showAddAppModal) {
        AddControlledAppModal(
            onSave = { appName, pkgName, actionType, template ->
                viewModel.addFocusSearchApp(appName, pkgName, actionType, template)
                showAddAppModal = false
            },
            onDismiss = { showAddAppModal = false }
        )
    }

    if (siteToDelete != null) {
        ConfirmationDialog(
            title = "Remove Website",
            message = "Remove \"${siteToDelete!!.name}\" from Focus Search?",
            confirmButtonText = "Remove",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteFocusSearchSite(siteToDelete!!)
                siteToDelete = null
            },
            onDismiss = { siteToDelete = null }
        )
    }

    if (appToDelete != null) {
        ConfirmationDialog(
            title = "Remove Controlled App",
            message = "Remove \"${appToDelete!!.appName}\" from Focus Search control?",
            confirmButtonText = "Remove",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteFocusSearchApp(appToDelete!!)
                appToDelete = null
            },
            onDismiss = { appToDelete = null }
        )
    }
}

private data class SearchTargetOption(
    val name: String,
    val isApp: Boolean,
    val pkgOrDomain: String,
    val siteEntity: FocusSearchSiteEntity?,
    val appEntity: FocusSearchAppEntity?
)
