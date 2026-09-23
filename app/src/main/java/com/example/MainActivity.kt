package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.db.AppDatabase
import com.example.data.repository.StudyFlowRepository
import com.example.ui.components.InAppNotificationBanner
import com.example.ui.navigation.Screen
import com.example.ui.screens.AppControlScreen
import com.example.ui.screens.BlockShortsPageScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FocusScreen
import com.example.ui.screens.FocusSearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.SupportScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.AmberStreak
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.StudyFlowTheme
import com.example.ui.viewmodel.StudyFlowViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: StudyFlowViewModel by viewModels()
    private val pendingDestination = androidx.compose.runtime.mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        pendingDestination.value = intent?.getStringExtra(EXTRA_NAVIGATE_TO)

        setContent {
            val settings by viewModel.userSettings.collectAsState()
            val systemInDark = isSystemInDarkTheme()
            val useDarkTheme = when (settings.themeMode) {
                "Dark" -> true
                "Light" -> false
                else -> systemInDark
            }

            StudyFlowTheme(
                darkTheme = useDarkTheme,
                fontSizePercentage = settings.fontSizePercentage
            ) {
                StudyFlowApp(
                    viewModel = viewModel,
                    isDarkTheme = useDarkTheme,
                    targetDestination = pendingDestination.value,
                    onDestinationHandled = { pendingDestination.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val navTo = intent.getStringExtra(EXTRA_NAVIGATE_TO)
        if (navTo != null) {
            pendingDestination.value = navTo
        }
    }

    companion object {
        const val EXTRA_NAVIGATE_TO = "NAVIGATE_TO"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyFlowApp(
    viewModel: StudyFlowViewModel,
    isDarkTheme: Boolean,
    targetDestination: String? = null,
    onDestinationHandled: () -> Unit = {}
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    androidx.compose.runtime.LaunchedEffect(targetDestination) {
        if (targetDestination == "focus") {
            navController.navigate(Screen.Focus.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
            onDestinationHandled()
        } else if (targetDestination == "block_shorts") {
            navController.navigate(Screen.BlockShorts.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
            onDestinationHandled()
        }
    }
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    val settings by viewModel.userSettings.collectAsState()
    val activeNotification by viewModel.activeNotification.collectAsState()

    val currentScreen = Screen.entries.find { it.route == currentRoute } ?: Screen.Dashboard

    // Bottom Navigation items (5 most frequent tabs)
    val bottomNavScreens = listOf(
        Screen.Dashboard,
        Screen.Tasks,
        Screen.Focus,
        Screen.Calendar,
        Screen.FocusSearch
    )

    // All Drawer Navigation items
    val drawerScreens = Screen.entries

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(310.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Drawer Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(IndigoPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Study Flow",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Plan · Focus · Work · Track",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Streak Pill
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AmberStreak.copy(alpha = 0.15f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clickable {
                                scope.launch { drawerState.close() }
                                navController.navigate(Screen.Stats.route)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = AmberStreak,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🔥 ${settings.currentStreak} Day Focus Streak",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = AmberStreak
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

                    // Drawer Navigation Items
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        drawerScreens.forEach { screen ->
                            val selected = currentRoute == screen.route
                            NavigationDrawerItem(
                                icon = {
                                    Icon(
                                        imageVector = getScreenIcon(screen),
                                        contentDescription = screen.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                selected = selected,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    if (currentRoute != screen.route) {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = IndigoPrimary.copy(alpha = 0.15f),
                                    selectedIconColor = IndigoPrimary,
                                    selectedTextColor = IndigoPrimary
                                ),
                                modifier = Modifier.testTag("drawer_item_${screen.name.lowercase()}")
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Drawer Footer
                    Text(
                        text = "Study Flow v1.0.0 · Offline SaaS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = currentScreen.title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("topbar_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Navigation Menu"
                            )
                        }
                    },
                    actions = {
                        // Quick Streak Indicator
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AmberStreak.copy(alpha = 0.15f),
                            modifier = Modifier
                                .clickable {
                                    navController.navigate(Screen.Stats.route)
                                }
                                .padding(end = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Streak",
                                    tint = AmberStreak,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${settings.currentStreak}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberStreak
                                )
                            }
                        }

                        // Theme Toggle Shortcut
                        IconButton(
                            onClick = {
                                val nextMode = if (isDarkTheme) "Light" else "Dark"
                                viewModel.updateSettings(settings.copy(themeMode = nextMode))
                            },
                            modifier = Modifier.testTag("topbar_theme_toggle")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Dark Mode"
                            )
                        }

                        // Notification alert bell
                        IconButton(
                            onClick = {
                                viewModel.showNotification(
                                    "Study Flow Alerts Active",
                                    "Daily streak: ${settings.currentStreak}d. Stay focused on your top priority!",
                                    "SYSTEM"
                                )
                            },
                            modifier = Modifier.testTag("topbar_notification_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                // Bottom navigation for easy thumb reach
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    bottomNavScreens.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = getScreenIcon(screen),
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title.split(" ").first(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = IndigoPrimary,
                                selectedTextColor = IndigoPrimary,
                                indicatorColor = IndigoPrimary.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("bottom_nav_${screen.name.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Dashboard.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(Screen.Dashboard.route) {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigate = { targetScreen ->
                                navController.navigate(targetScreen.route)
                            }
                        )
                    }
                    composable(Screen.Tasks.route) {
                        TasksScreen(viewModel = viewModel)
                    }
                    composable(Screen.Focus.route) {
                        FocusScreen(viewModel = viewModel)
                    }
                    composable(Screen.BlockShorts.route) {
                        BlockShortsPageScreen(viewModel = viewModel)
                    }
                    composable(Screen.FocusSearch.route) {
                        FocusSearchScreen(viewModel = viewModel)
                    }
                    composable(Screen.Calendar.route) {
                        CalendarScreen(viewModel = viewModel)
                    }
                    composable(Screen.AppControl.route) {
                        AppControlScreen(
                            viewModel = viewModel,
                            onNavigate = { targetScreen ->
                                navController.navigate(targetScreen.route)
                            }
                        )
                    }
                    composable(Screen.Stats.route) {
                        StatsScreen(viewModel = viewModel)
                    }
                    composable(Screen.Settings.route) {
                        SettingsScreen(viewModel = viewModel)
                    }
                    composable(Screen.Support.route) {
                        SupportScreen(viewModel = viewModel)
                    }
                }

                // In-App Toast / Floating Notification Alert
                InAppNotificationBanner(
                    notification = activeNotification,
                    onDismiss = { viewModel.dismissActiveNotification() }
                )
            }
        }
    }
}

private fun getScreenIcon(screen: Screen) = when (screen) {
    Screen.Dashboard -> Icons.Default.Dashboard
    Screen.Tasks -> Icons.Default.CheckCircle
    Screen.Focus -> Icons.Default.Timer
    Screen.BlockShorts -> Icons.Default.Block
    Screen.FocusSearch -> Icons.Default.Search
    Screen.Calendar -> Icons.Default.CalendarMonth
    Screen.AppControl -> Icons.Default.Security
    Screen.Stats -> Icons.Default.TrendingUp
    Screen.Settings -> Icons.Default.Settings
    Screen.Support -> Icons.Default.Favorite
}
