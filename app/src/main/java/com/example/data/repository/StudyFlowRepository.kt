package com.example.data.repository

import com.example.data.db.StudyFlowDao
import com.example.data.model.AppControlEntity
import com.example.data.model.BlockShortsEntity
import com.example.data.model.BlockedAppEntity
import com.example.data.model.CalendarEventEntity
import com.example.data.model.FocusSearchAppEntity
import com.example.data.model.FocusSearchSiteEntity
import com.example.data.model.FocusSessionEntity
import com.example.data.model.TaskEntity
import com.example.data.model.UserSettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate

class StudyFlowRepository(private val dao: StudyFlowDao) {

    val tasks: Flow<List<TaskEntity>> = dao.getAllTasks()
    val focusSessions: Flow<List<FocusSessionEntity>> = dao.getAllFocusSessions()
    val calendarEvents: Flow<List<CalendarEventEntity>> = dao.getAllCalendarEvents()
    val focusSearchSites: Flow<List<FocusSearchSiteEntity>> = dao.getAllFocusSearchSites()
    val focusSearchApps: Flow<List<FocusSearchAppEntity>> = dao.getAllFocusSearchApps()
    val appControls: Flow<List<AppControlEntity>> = dao.getAllAppControls()
    val userSettings: Flow<UserSettingsEntity?> = dao.getUserSettings()
    val blockShortsSettings: Flow<BlockShortsEntity?> = dao.getBlockShortsSettings()
    val blockedApps: Flow<List<BlockedAppEntity>> = dao.getAllBlockedApps()

    // Blocked Apps operations (User Controlled Blocker per platform)
    suspend fun addBlockedApp(app: BlockedAppEntity): Long = dao.insertBlockedApp(app)
    suspend fun updateBlockedApp(app: BlockedAppEntity) = dao.updateBlockedApp(app)
    suspend fun deleteBlockedApp(app: BlockedAppEntity) = dao.deleteBlockedApp(app)
    suspend fun deleteBlockedAppById(id: Long) = dao.deleteBlockedAppById(id)
    suspend fun toggleBlockedApp(app: BlockedAppEntity, enabled: Boolean) {
        val updated = app.copy(
            enabled = enabled,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateBlockedApp(updated)
    }

    // Tasks operations
    suspend fun addTask(task: TaskEntity): Long = dao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = dao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = dao.deleteTask(task)
    suspend fun deleteTaskById(id: Long) = dao.deleteTaskById(id)
    suspend fun toggleTaskCompletion(task: TaskEntity) {
        val updated = task.copy(
            isCompleted = !task.isCompleted,
            completedAt = if (!task.isCompleted) System.currentTimeMillis() else null
        )
        dao.updateTask(updated)
    }

    // Focus Sessions operations
    suspend fun addFocusSession(session: FocusSessionEntity): Long = dao.insertFocusSession(session)

    // Calendar Events operations
    suspend fun addCalendarEvent(event: CalendarEventEntity): Long = dao.insertCalendarEvent(event)
    suspend fun updateCalendarEvent(event: CalendarEventEntity) = dao.updateCalendarEvent(event)
    suspend fun deleteCalendarEvent(event: CalendarEventEntity) = dao.deleteCalendarEvent(event)
    suspend fun deleteCalendarEventById(id: Long) = dao.deleteCalendarEventById(id)

    // Focus Search Sites operations
    suspend fun addFocusSearchSite(site: FocusSearchSiteEntity): Long = dao.insertFocusSearchSite(site)
    suspend fun updateFocusSearchSite(site: FocusSearchSiteEntity) = dao.updateFocusSearchSite(site)
    suspend fun deleteFocusSearchSite(site: FocusSearchSiteEntity) = dao.deleteFocusSearchSite(site)
    suspend fun deleteFocusSearchSiteById(id: Long) = dao.deleteFocusSearchSiteById(id)
    suspend fun toggleFocusSearchSite(site: FocusSearchSiteEntity) {
        dao.updateFocusSearchSite(site.copy(isEnabled = !site.isEnabled))
    }

    // Focus Search Controlled Apps operations
    suspend fun addFocusSearchApp(app: FocusSearchAppEntity): Long = dao.insertFocusSearchApp(app)
    suspend fun updateFocusSearchApp(app: FocusSearchAppEntity) = dao.updateFocusSearchApp(app)
    suspend fun deleteFocusSearchApp(app: FocusSearchAppEntity) = dao.deleteFocusSearchApp(app)
    suspend fun deleteFocusSearchAppById(id: Long) = dao.deleteFocusSearchAppById(id)
    suspend fun toggleFocusSearchApp(app: FocusSearchAppEntity) {
        dao.updateFocusSearchApp(app.copy(isEnabled = !app.isEnabled))
    }

    // App Control operations
    suspend fun addAppControl(appControl: AppControlEntity): Long = dao.insertAppControl(appControl)
    suspend fun updateAppControl(appControl: AppControlEntity) = dao.updateAppControl(appControl)
    suspend fun deleteAppControl(appControl: AppControlEntity) = dao.deleteAppControl(appControl)
    suspend fun deleteAppControlById(id: Long) = dao.deleteAppControlById(id)
    suspend fun toggleAppControl(appControl: AppControlEntity) {
        dao.updateAppControl(appControl.copy(isEnabled = !appControl.isEnabled))
    }
    suspend fun toggleAppControl(appControl: AppControlEntity, enabled: Boolean) {
        dao.updateAppControl(appControl.copy(isEnabled = enabled))
    }

    // Settings
    suspend fun saveSettings(settings: UserSettingsEntity) = dao.insertOrUpdateSettings(settings)

    // Block Shorts Settings
    suspend fun saveBlockShortsSettings(settings: BlockShortsEntity) = dao.insertOrUpdateBlockShorts(settings)

    // Clear all
    suspend fun clearAllData() {
        dao.clearAllTasks()
        dao.clearAllFocusSessions()
        dao.clearAllCalendarEvents()
        dao.clearAllFocusSearchSites()
        dao.clearAllFocusSearchApps()
        dao.clearAllAppControls()
        dao.clearUserSettings()
        dao.clearBlockShortsSettings()
    }

    // Seed defaults
    suspend fun seedInitialDataIfEmpty() {
        val todayEpochDay = LocalDate.now().toEpochDay()

        val defaultSettings = UserSettingsEntity(
            id = 1,
            themeMode = "SYSTEM",
            fontSizePercentage = 100,
            focusDurationMinutes = 25,
            shortBreakMinutes = 5,
            longBreakMinutes = 15,
            autoStartBreaks = false,
            autoStartFocus = false,
            soundEnabled = true,
            vibrationEnabled = true,
            notificationsEnabled = true,
            focusSearchEnabled = true,
            currentStreak = 7,
            bestStreak = 14,
            lastActiveDateEpochDay = todayEpochDay
        )
        dao.insertOrUpdateSettings(defaultSettings)

        val defaultTasks = listOf(
            TaskEntity(
                title = "Complete Java DSA Practice",
                notes = "Binary Trees and Dynamic Programming questions",
                priority = "High",
                category = "Study",
                dueDateEpochDay = todayEpochDay,
                dueTime = "02:00 PM",
                isCompleted = false
            ),
            TaskEntity(
                title = "Finish Project Documentation",
                notes = "Architecture diagram and system specification doc",
                priority = "Medium",
                category = "Project",
                dueDateEpochDay = todayEpochDay,
                dueTime = "04:30 PM",
                isCompleted = false
            ),
            TaskEntity(
                title = "Read DBMS Notes: Normalization",
                notes = "3NF and BCNF concepts with examples",
                priority = "Low",
                category = "Study",
                dueDateEpochDay = todayEpochDay,
                dueTime = "11:00 AM",
                isCompleted = true,
                completedAt = System.currentTimeMillis() - 7200000
            ),
            TaskEntity(
                title = "Submit Software Engineering Assignment",
                notes = "Upload PDF to student portal",
                priority = "High",
                category = "Assignment",
                dueDateEpochDay = todayEpochDay + 1,
                dueTime = "11:59 PM",
                isCompleted = false
            ),
            TaskEntity(
                title = "Prepare Presentation Slides",
                notes = "Sprint demo slides for team review",
                priority = "Medium",
                category = "Work",
                dueDateEpochDay = todayEpochDay + 2,
                dueTime = "03:00 PM",
                isCompleted = false
            ),
            TaskEntity(
                title = "Review Discrete Mathematics Proofs",
                notes = "Mathematical induction practice set",
                priority = "Low",
                category = "Study",
                dueDateEpochDay = todayEpochDay,
                dueTime = "09:00 AM",
                isCompleted = true,
                completedAt = System.currentTimeMillis() - 14400000
            )
        )
        dao.insertAllTasks(defaultTasks)

        val defaultFocusSessions = listOf(
            FocusSessionEntity(
                mode = "Focus",
                durationMinutes = 25,
                timestamp = System.currentTimeMillis() - 10800000,
                epochDay = todayEpochDay,
                isCompleted = true
            ),
            FocusSessionEntity(
                mode = "Focus",
                durationMinutes = 25,
                timestamp = System.currentTimeMillis() - 7200000,
                epochDay = todayEpochDay,
                isCompleted = true
            ),
            FocusSessionEntity(
                mode = "Focus",
                durationMinutes = 25,
                timestamp = System.currentTimeMillis() - 3600000,
                epochDay = todayEpochDay,
                isCompleted = true
            ),
            FocusSessionEntity(
                mode = "Focus",
                durationMinutes = 30,
                timestamp = System.currentTimeMillis() - 1800000,
                epochDay = todayEpochDay,
                isCompleted = true
            ),
            // Previous days for weekly stats
            FocusSessionEntity(
                mode = "Focus",
                durationMinutes = 50,
                timestamp = System.currentTimeMillis() - 86400000,
                epochDay = todayEpochDay - 1,
                isCompleted = true
            ),
            FocusSessionEntity(
                mode = "Focus",
                durationMinutes = 75,
                timestamp = System.currentTimeMillis() - (86400000 * 2),
                epochDay = todayEpochDay - 2,
                isCompleted = true
            ),
            FocusSessionEntity(
                mode = "Focus",
                durationMinutes = 60,
                timestamp = System.currentTimeMillis() - (86400000 * 3),
                epochDay = todayEpochDay - 3,
                isCompleted = true
            )
        )
        dao.insertAllFocusSessions(defaultFocusSessions)

        val defaultEvents = listOf(
            CalendarEventEntity(
                title = "College",
                description = "Morning lecture and laboratory sessions",
                dateEpochDay = todayEpochDay,
                startTime = "09:00 AM",
                endTime = "12:00 PM",
                category = "Study"
            ),
            CalendarEventEntity(
                title = "Lunch",
                description = "Nutritious meal & rest break",
                dateEpochDay = todayEpochDay,
                startTime = "01:00 PM",
                endTime = "02:00 PM",
                category = "Personal"
            ),
            CalendarEventEntity(
                title = "Study",
                description = "Deep work: Algorithms & system design",
                dateEpochDay = todayEpochDay,
                startTime = "05:30 PM",
                endTime = "07:00 PM",
                category = "Study"
            ),
            CalendarEventEntity(
                title = "Dinner",
                description = "Evening meal & unwind",
                dateEpochDay = todayEpochDay,
                startTime = "07:30 PM",
                endTime = "08:30 PM",
                category = "Personal"
            ),
            CalendarEventEntity(
                title = "Capstone Project Review",
                description = "Faculty mentor review",
                dateEpochDay = todayEpochDay + 1,
                startTime = "03:00 PM",
                endTime = "04:00 PM",
                category = "Project"
            )
        )
        dao.insertAllCalendarEvents(defaultEvents)

        // Seed Focus Search Controlled Apps
        val defaultApps = listOf(
            FocusSearchAppEntity(
                appName = "YouTube",
                packageName = "com.google.android.youtube",
                searchActionType = "YOUTUBE",
                searchUrlTemplate = "https://www.youtube.com/results?search_query=%s",
                isEnabled = true,
                iconKey = "youtube"
            ),
            FocusSearchAppEntity(
                appName = "Instagram",
                packageName = "com.instagram.android",
                searchActionType = "INSTAGRAM",
                searchUrlTemplate = "https://www.instagram.com/explore/tags/%s",
                isEnabled = true,
                iconKey = "instagram"
            ),
            FocusSearchAppEntity(
                appName = "Reddit",
                packageName = "com.reddit.frontpage",
                searchActionType = "REDDIT",
                searchUrlTemplate = "https://www.reddit.com/search/?q=%s",
                isEnabled = true,
                iconKey = "reddit"
            ),
            FocusSearchAppEntity(
                appName = "Facebook",
                packageName = "com.facebook.katana",
                searchActionType = "FACEBOOK",
                searchUrlTemplate = "https://www.facebook.com/search/top/?q=%s",
                isEnabled = false,
                iconKey = "facebook"
            ),
            FocusSearchAppEntity(
                appName = "X (Twitter)",
                packageName = "com.twitter.android",
                searchActionType = "TWITTER",
                searchUrlTemplate = "https://twitter.com/search?q=%s",
                isEnabled = false,
                iconKey = "twitter"
            )
        )
        dao.insertAllFocusSearchApps(defaultApps)

        val defaultSites = listOf(
            FocusSearchSiteEntity(
                name = "YouTube",
                domain = "youtube.com",
                searchUrlTemplate = "https://www.youtube.com/results?search_query=%s",
                isEnabled = true
            ),
            FocusSearchSiteEntity(
                name = "Reddit",
                domain = "reddit.com",
                searchUrlTemplate = "https://www.reddit.com/search/?q=%s",
                isEnabled = true
            ),
            FocusSearchSiteEntity(
                name = "Wikipedia",
                domain = "wikipedia.org",
                searchUrlTemplate = "https://en.wikipedia.org/w/index.php?search=%s",
                isEnabled = true
            ),
            FocusSearchSiteEntity(
                name = "Google Scholar",
                domain = "scholar.google.com",
                searchUrlTemplate = "https://scholar.google.com/scholar?q=%s",
                isEnabled = true
            ),
            FocusSearchSiteEntity(
                name = "GitHub",
                domain = "github.com",
                searchUrlTemplate = "https://github.com/search?q=%s",
                isEnabled = true
            ),
            FocusSearchSiteEntity(
                name = "Instagram",
                domain = "instagram.com",
                searchUrlTemplate = "https://www.instagram.com/explore/tags/%s",
                isEnabled = true
            ),
            FocusSearchSiteEntity(
                name = "X (Twitter)",
                domain = "x.com",
                searchUrlTemplate = "https://x.com/search?q=%s",
                isEnabled = true
            )
        )
        dao.insertAllFocusSearchSites(defaultSites)

        val defaultAppControls = listOf(
            AppControlEntity(
                appName = "YouTube",
                targetDomain = "youtube.com",
                startHour = 18,
                startMinute = 0,
                endHour = 20,
                endMinute = 0,
                isEnabled = true
            ),
            AppControlEntity(
                appName = "Instagram",
                targetDomain = "instagram.com",
                startHour = 9,
                startMinute = 0,
                endHour = 17,
                endMinute = 0,
                isEnabled = false
            ),
            AppControlEntity(
                appName = "Facebook",
                targetDomain = "facebook.com",
                startHour = 10,
                startMinute = 0,
                endHour = 18,
                endMinute = 0,
                isEnabled = true
            ),
            AppControlEntity(
                appName = "TikTok",
                targetDomain = "tiktok.com",
                startHour = 12,
                startMinute = 0,
                endHour = 20,
                endMinute = 0,
                isEnabled = false
            ),
            AppControlEntity(
                appName = "Reddit",
                targetDomain = "reddit.com",
                startHour = 13,
                startMinute = 0,
                endHour = 15,
                endMinute = 0,
                isEnabled = false
            )
        )
        dao.insertAllAppControls(defaultAppControls)

        // Seed default Block Shorts configuration
        dao.insertOrUpdateBlockShorts(
            BlockShortsEntity(
                id = 1,
                isEnabled = true,
                blockYouTubeShorts = true,
                blockInstagramReels = true,
                isScheduleEnabled = false,
                scheduleStartHour = 18,
                scheduleStartMinute = 0,
                scheduleEndHour = 22,
                scheduleEndMinute = 0,
                isStrictModeEnabled = false,
                blockedAttemptsCount = 0
            )
        )

        // Seed default standalone Blocked Apps (User Controlled Blocker per platform)
        val defaultBlockedApps = listOf(
            BlockedAppEntity(
                name = "YouTube",
                url = "youtube.com",
                icon = "youtube",
                contentType = "Shorts",
                enabled = true,
                scheduleEnabled = false,
                startTime = "18:00",
                endTime = "22:00"
            ),
            BlockedAppEntity(
                name = "Instagram",
                url = "instagram.com",
                icon = "instagram",
                contentType = "Reels",
                enabled = true,
                scheduleEnabled = false,
                startTime = "18:00",
                endTime = "22:00"
            ),
            BlockedAppEntity(
                name = "Facebook",
                url = "facebook.com",
                icon = "facebook",
                contentType = "Reels",
                enabled = false,
                scheduleEnabled = false,
                startTime = "18:00",
                endTime = "22:00"
            ),
            BlockedAppEntity(
                name = "TikTok",
                url = "tiktok.com",
                icon = "tiktok",
                contentType = "Short-form videos",
                enabled = false,
                scheduleEnabled = false,
                startTime = "18:00",
                endTime = "22:00"
            ),
            BlockedAppEntity(
                name = "Reddit",
                url = "reddit.com",
                icon = "reddit",
                contentType = "Short-form / distraction feed",
                enabled = false,
                scheduleEnabled = false,
                startTime = "18:00",
                endTime = "22:00"
            )
        )
        dao.insertAllBlockedApps(defaultBlockedApps)
    }

    suspend fun ensureAllDefaultsSeeded() {
        val existingTasks = dao.getAllTasks().firstOrNull()
        if (existingTasks.isNullOrEmpty()) {
            seedInitialDataIfEmpty()
            return
        }

        // Verify and seed Blocked Apps if empty
        val blockedAppsList = dao.getAllBlockedApps().firstOrNull()
        if (blockedAppsList.isNullOrEmpty()) {
            val defaultBlockedApps = listOf(
                BlockedAppEntity(
                    name = "YouTube",
                    url = "youtube.com",
                    icon = "youtube",
                    contentType = "Shorts",
                    enabled = true,
                    scheduleEnabled = false,
                    startTime = "18:00",
                    endTime = "22:00"
                ),
                BlockedAppEntity(
                    name = "Instagram",
                    url = "instagram.com",
                    icon = "instagram",
                    contentType = "Reels",
                    enabled = true,
                    scheduleEnabled = false,
                    startTime = "18:00",
                    endTime = "22:00"
                ),
                BlockedAppEntity(
                    name = "Facebook",
                    url = "facebook.com",
                    icon = "facebook",
                    contentType = "Reels",
                    enabled = false,
                    scheduleEnabled = false,
                    startTime = "18:00",
                    endTime = "22:00"
                ),
                BlockedAppEntity(
                    name = "TikTok",
                    url = "tiktok.com",
                    icon = "tiktok",
                    contentType = "Short-form videos",
                    enabled = false,
                    scheduleEnabled = false,
                    startTime = "18:00",
                    endTime = "22:00"
                ),
                BlockedAppEntity(
                    name = "Reddit",
                    url = "reddit.com",
                    icon = "reddit",
                    contentType = "Short-form / distraction feed",
                    enabled = false,
                    scheduleEnabled = false,
                    startTime = "18:00",
                    endTime = "22:00"
                )
            )
            dao.insertAllBlockedApps(defaultBlockedApps)
        }

        // Verify and seed BlockShorts configuration if missing
        val blockShorts = dao.getBlockShortsSettings().firstOrNull()
        if (blockShorts == null) {
            dao.insertOrUpdateBlockShorts(
                BlockShortsEntity(
                    id = 1,
                    isEnabled = true,
                    blockYouTubeShorts = true,
                    blockInstagramReels = true,
                    isScheduleEnabled = false,
                    scheduleStartHour = 18,
                    scheduleStartMinute = 0,
                    scheduleEndHour = 22,
                    scheduleEndMinute = 0,
                    isStrictModeEnabled = false,
                    blockedAttemptsCount = 0
                )
            )
        }

        // Verify and seed App Controls if empty
        val appControls = dao.getAllAppControls().firstOrNull()
        if (appControls.isNullOrEmpty()) {
            val defaultAppControls = listOf(
                AppControlEntity(
                    appName = "YouTube",
                    targetDomain = "youtube.com",
                    startHour = 18,
                    startMinute = 0,
                    endHour = 20,
                    endMinute = 0,
                    isEnabled = true
                ),
                AppControlEntity(
                    appName = "Instagram",
                    targetDomain = "instagram.com",
                    startHour = 9,
                    startMinute = 0,
                    endHour = 17,
                    endMinute = 0,
                    isEnabled = false
                ),
                AppControlEntity(
                    appName = "Facebook",
                    targetDomain = "facebook.com",
                    startHour = 10,
                    startMinute = 0,
                    endHour = 18,
                    endMinute = 0,
                    isEnabled = true
                ),
                AppControlEntity(
                    appName = "TikTok",
                    targetDomain = "tiktok.com",
                    startHour = 12,
                    startMinute = 0,
                    endHour = 20,
                    endMinute = 0,
                    isEnabled = false
                ),
                AppControlEntity(
                    appName = "Reddit",
                    targetDomain = "reddit.com",
                    startHour = 13,
                    startMinute = 0,
                    endHour = 15,
                    endMinute = 0,
                    isEnabled = false
                )
            )
            dao.insertAllAppControls(defaultAppControls)
        }
    }
}
