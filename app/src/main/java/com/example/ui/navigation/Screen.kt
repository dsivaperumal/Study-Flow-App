package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    Dashboard("dashboard", "Dashboard", Icons.Default.Dashboard),
    Tasks("tasks", "Tasks", Icons.Default.CheckCircle),
    Focus("focus", "Focus Timer", Icons.Default.Timer),
    BlockShorts("block_shorts", "Block Shorts", Icons.Default.Block),
    FocusSearch("focus_search", "Focus Search", Icons.Default.Search),
    Calendar("calendar", "Calendar", Icons.Default.CalendarMonth),
    AppControl("app_control", "App Control", Icons.Default.Security),
    Stats("stats", "Productivity", Icons.Default.BarChart),
    Settings("settings", "Settings", Icons.Default.Settings),
    Support("support", "Support Us", Icons.Default.Favorite)
}
