package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AppControlEntity
import com.example.data.model.BlockShortsEntity
import com.example.data.model.BlockedAppEntity
import com.example.data.model.CalendarEventEntity
import com.example.data.model.FocusSearchAppEntity
import com.example.data.model.FocusSearchSiteEntity
import com.example.data.model.FocusSessionEntity
import com.example.data.model.TaskEntity
import com.example.data.model.UserSettingsEntity
import com.example.data.repository.StudyFlowRepository
import com.example.service.FocusSearchAccessibilityService
import com.example.service.blockshorts.BlockShortsManager
import com.example.utils.SoundAndVibrationUtil
import com.example.utils.TimeUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class NotificationItem(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "INFO" // FOCUS, BREAK, TASK, CONTROL
)

class StudyFlowViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyFlowRepository

    // Database states
    private val _tasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    val tasks: StateFlow<List<TaskEntity>> = _tasks.asStateFlow()

    private val _focusSessions = MutableStateFlow<List<FocusSessionEntity>>(emptyList())
    val focusSessions: StateFlow<List<FocusSessionEntity>> = _focusSessions.asStateFlow()

    private val _calendarEvents = MutableStateFlow<List<CalendarEventEntity>>(emptyList())
    val calendarEvents: StateFlow<List<CalendarEventEntity>> = _calendarEvents.asStateFlow()

    private val _focusSearchSites = MutableStateFlow<List<FocusSearchSiteEntity>>(emptyList())
    val focusSearchSites: StateFlow<List<FocusSearchSiteEntity>> = _focusSearchSites.asStateFlow()

    private val _focusSearchApps = MutableStateFlow<List<FocusSearchAppEntity>>(emptyList())
    val focusSearchApps: StateFlow<List<FocusSearchAppEntity>> = _focusSearchApps.asStateFlow()

    private val _appControls = MutableStateFlow<List<AppControlEntity>>(emptyList())
    val appControls: StateFlow<List<AppControlEntity>> = _appControls.asStateFlow()

    private val _userSettings = MutableStateFlow(
        UserSettingsEntity(
            id = 1,
            themeMode = "SYSTEM",
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
            lastActiveDateEpochDay = LocalDate.now().toEpochDay()
        )
    )
    val userSettings: StateFlow<UserSettingsEntity> = _userSettings.asStateFlow()

    // Block Shorts State
    private val _blockShortsSettings = MutableStateFlow(
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
    val blockShortsSettings: StateFlow<BlockShortsEntity> = _blockShortsSettings.asStateFlow()

    // Standalone Blocked Apps State (User Controlled Blocker per platform)
    private val _blockedApps = MutableStateFlow<List<BlockedAppEntity>>(emptyList())
    val blockedApps: StateFlow<List<BlockedAppEntity>> = _blockedApps.asStateFlow()

    private val _globalStrictMode = MutableStateFlow(false)
    val globalStrictMode: StateFlow<Boolean> = _globalStrictMode.asStateFlow()

    // Timer States
    private val _timerMode = MutableStateFlow("Focus") // "Focus", "Short Break", "Long Break"
    val timerMode: StateFlow<String> = _timerMode.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(25 * 60)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _totalSecondsForMode = MutableStateFlow(25 * 60)
    val totalSecondsForMode: StateFlow<Int> = _totalSecondsForMode.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _completedSessionsToday = MutableStateFlow(3)
    val completedSessionsToday: StateFlow<Int> = _completedSessionsToday.asStateFlow()

    private val _timerCompletionDialog = MutableStateFlow<String?>(null)
    val timerCompletionDialog: StateFlow<String?> = _timerCompletionDialog.asStateFlow()

    // Task Filter & Search
    private val _taskFilter = MutableStateFlow("All") // "All", "Today", "Upcoming", "Completed", "High Priority"
    val taskFilter: StateFlow<String> = _taskFilter.asStateFlow()

    private val _taskSearchQuery = MutableStateFlow("")
    val taskSearchQuery: StateFlow<String> = _taskSearchQuery.asStateFlow()

    // Calendar
    private val _selectedCalendarDate = MutableStateFlow(LocalDate.now())
    val selectedCalendarDate: StateFlow<LocalDate> = _selectedCalendarDate.asStateFlow()

    private val _currentYearMonth = MutableStateFlow(YearMonth.now())
    val currentYearMonth: StateFlow<YearMonth> = _currentYearMonth.asStateFlow()

    // Notifications
    private val _activeNotification = MutableStateFlow<NotificationItem?>(null)
    val activeNotification: StateFlow<NotificationItem?> = _activeNotification.asStateFlow()

    private val _notificationHistory = MutableStateFlow<List<NotificationItem>>(
        listOf(
            NotificationItem(
                title = "Welcome to Study Flow",
                message = "Plan your tasks, set your focus sessions, and master your day.",
                type = "INFO"
            ),
            NotificationItem(
                title = "Study Session Completed",
                message = "Great work! 25 minutes logged toward your daily streak.",
                type = "FOCUS"
            )
        )
    )
    val notificationHistory: StateFlow<List<NotificationItem>> = _notificationHistory.asStateFlow()

    // App Control Block Screen
    private val _blockedAppAttempt = MutableStateFlow<String?>(null)
    val blockedAppAttempt: StateFlow<String?> = _blockedAppAttempt.asStateFlow()

    private var timerJob: Job? = null

    init {
        val database = AppDatabase.getDatabase(application)
        repository = StudyFlowRepository(database.studyFlowDao())

        viewModelScope.launch {
            // Ensure all defaults (tasks, apps, controls, settings) are seeded
            repository.ensureAllDefaultsSeeded()

            // Observe tasks
            launch {
                repository.tasks.collect { _tasks.value = it }
            }
            // Observe focus sessions
            launch {
                repository.focusSessions.collect { sessions ->
                    _focusSessions.value = sessions
                    val today = LocalDate.now().toEpochDay()
                    val todaySessions = sessions.filter { it.epochDay == today && it.mode == "Focus" }
                    _completedSessionsToday.value = todaySessions.size
                }
            }
            // Observe calendar
            launch {
                repository.calendarEvents.collect { _calendarEvents.value = it }
            }
            // Observe focus search sites
            launch {
                repository.focusSearchSites.collect { _focusSearchSites.value = it }
            }
            // Observe focus search apps
            launch {
                repository.focusSearchApps.collect { apps ->
                    _focusSearchApps.value = apps
                    val activeMap = apps.filter { it.isEnabled }.associate { it.packageName to it.appName }
                    FocusSearchAccessibilityService.updateActivePackages(activeMap)
                }
            }
            // Observe app controls
            launch {
                repository.appControls.collect { _appControls.value = it }
            }
            // Observe settings
            launch {
                repository.userSettings.collect { settings ->
                    if (settings != null) {
                        _userSettings.value = settings
                        // If timer is not running, adjust timer target
                        if (!_isTimerRunning.value) {
                            applyModeDuration(_timerMode.value, settings)
                        }
                    }
                }
            }
            // Observe block shorts settings
            launch {
                repository.blockShortsSettings.collect { settings ->
                    if (settings != null) {
                        _blockShortsSettings.value = settings
                        BlockShortsManager.currentConfig = settings
                    }
                }
            }
            // Observe blocked apps (User Controlled Blocker per platform)
            launch {
                repository.blockedApps.collect { apps ->
                    _blockedApps.value = apps
                    BlockShortsManager.blockedApps = apps
                }
            }
        }
    }

    private fun applyModeDuration(mode: String, settings: UserSettingsEntity = _userSettings.value) {
        val minutes = when (mode) {
            "Short Break" -> settings.shortBreakMinutes
            "Long Break" -> settings.longBreakMinutes
            else -> settings.focusDurationMinutes
        }
        val totalSec = minutes * 60
        _totalSecondsForMode.value = totalSec
        _remainingSeconds.value = totalSec
    }

    // --- Task Actions ---
    fun setTaskFilter(filter: String) {
        _taskFilter.value = filter
    }

    fun setTaskSearchQuery(query: String) {
        _taskSearchQuery.value = query
    }

    fun addTask(
        title: String,
        notes: String,
        priority: String,
        category: String,
        dueDateEpochDay: Long,
        dueTime: String
    ) {
        viewModelScope.launch {
            val formattedTime = TimeUtils.formatToAmPm(dueTime)
            val task = TaskEntity(
                title = title.trim(),
                notes = notes.trim(),
                priority = priority,
                category = category,
                dueDateEpochDay = dueDateEpochDay,
                dueTime = formattedTime,
                isCompleted = false
            )
            repository.addTask(task)
            showNotification("Task Added", "Scheduled: \"$title\" due at $formattedTime", "TASK")
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(dueTime = TimeUtils.formatToAmPm(task.dueTime)))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
            if (!task.isCompleted) {
                // Was not completed, now completed! Trigger streak update and alert
                incrementStreakIfEligible()
                SoundAndVibrationUtil.playCompletionChime(_userSettings.value.soundEnabled)
                SoundAndVibrationUtil.vibrate(getApplication(), _userSettings.value.vibrationEnabled)
                showNotification(
                    "Task Completed! 🎉",
                    "Completed: \"${task.title}\". Productivity streak boosted!",
                    "TASK"
                )
            }
        }
    }

    // --- Focus Timer Actions ---
    fun selectTimerMode(mode: String) {
        if (_isTimerRunning.value) {
            pauseTimer()
        }
        _timerMode.value = mode
        applyModeDuration(mode)
    }

    fun startOrResumeTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true
        if (_timerMode.value == "Focus") {
            BlockShortsManager.isFocusTimerRunning = true
        }
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_remainingSeconds.value > 0 && _isTimerRunning.value) {
                delay(1000L)
                _remainingSeconds.value -= 1
            }
            if (_remainingSeconds.value <= 0 && _isTimerRunning.value) {
                onTimerFinished()
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        BlockShortsManager.isFocusTimerRunning = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        applyModeDuration(_timerMode.value)
    }

    fun skipTimer() {
        pauseTimer()
        when (_timerMode.value) {
            "Focus" -> selectTimerMode("Short Break")
            "Short Break" -> selectTimerMode("Focus")
            "Long Break" -> selectTimerMode("Focus")
        }
    }

    private fun onTimerFinished() {
        _isTimerRunning.value = false
        BlockShortsManager.isFocusTimerRunning = false
        timerJob?.cancel()

        val mode = _timerMode.value
        val settings = _userSettings.value
        val durationMinutes = _totalSecondsForMode.value / 60

        // Play sounds and vibration
        SoundAndVibrationUtil.playAlertSound(settings.soundEnabled)
        SoundAndVibrationUtil.vibrate(getApplication(), settings.vibrationEnabled)

        if (mode == "Focus") {
            // Save focus session
            viewModelScope.launch {
                repository.addFocusSession(
                    FocusSessionEntity(
                        mode = "Focus",
                        durationMinutes = durationMinutes,
                        epochDay = LocalDate.now().toEpochDay(),
                        isCompleted = true
                    )
                )
                incrementStreakIfEligible()
            }
            val message = "🎉 Focus session completed!\nGreat work. Take a short break."
            _timerCompletionDialog.value = message
            showNotification("Focus Session Completed", "Great work! Time for a well-deserved break.", "FOCUS")

            if (settings.autoStartBreaks) {
                selectTimerMode("Short Break")
                startOrResumeTimer()
            }
        } else {
            val message = "🔔 Break finished. Ready to focus again?"
            _timerCompletionDialog.value = message
            showNotification("Break Completed", "Break is over. Ready to start your next focus block?", "BREAK")

            if (settings.autoStartFocus) {
                selectTimerMode("Focus")
                startOrResumeTimer()
            }
        }
    }

    fun dismissCompletionDialog() {
        _timerCompletionDialog.value = null
    }

    // --- Calendar Actions ---
    fun selectCalendarDate(date: LocalDate) {
        _selectedCalendarDate.value = date
    }

    fun previousMonth() {
        _currentYearMonth.value = _currentYearMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _currentYearMonth.value = _currentYearMonth.value.plusMonths(1)
    }

    fun jumpToToday() {
        _selectedCalendarDate.value = LocalDate.now()
        _currentYearMonth.value = YearMonth.now()
    }

    fun changeYear(delta: Long) {
        _currentYearMonth.value = _currentYearMonth.value.plusYears(delta)
    }

    fun addCalendarEvent(
        title: String,
        description: String,
        dateEpochDay: Long,
        startTime: String,
        endTime: String,
        category: String
    ) {
        viewModelScope.launch {
            val formattedStart = TimeUtils.formatToAmPm(startTime)
            val formattedEnd = TimeUtils.formatToAmPm(endTime)
            val event = CalendarEventEntity(
                title = title.trim(),
                description = description.trim(),
                dateEpochDay = dateEpochDay,
                startTime = formattedStart,
                endTime = formattedEnd,
                category = category
            )
            repository.addCalendarEvent(event)
            showNotification("Schedule Added", "\"$title\" at $formattedStart", "EVENT")
        }
    }

    fun updateCalendarEvent(event: CalendarEventEntity) {
        viewModelScope.launch {
            val formatted = event.copy(
                startTime = TimeUtils.formatToAmPm(event.startTime),
                endTime = TimeUtils.formatToAmPm(event.endTime)
            )
            repository.updateCalendarEvent(formatted)
            showNotification("Schedule Updated", "\"${event.title}\" updated", "EVENT")
        }
    }

    fun deleteCalendarEvent(event: CalendarEventEntity) {
        viewModelScope.launch {
            repository.deleteCalendarEvent(event)
            showNotification("Schedule Removed", "\"${event.title}\" removed", "EVENT")
        }
    }

    // --- Focus Search Apps Actions ---
    fun toggleFocusSearchApp(app: FocusSearchAppEntity) {
        viewModelScope.launch {
            repository.toggleFocusSearchApp(app)
            val newState = if (!app.isEnabled) "ON" else "OFF"
            showNotification("Focus Search $newState", "${app.appName} is now $newState", "CONTROL")
        }
    }

    fun addFocusSearchApp(
        appName: String,
        packageName: String,
        searchActionType: String = "SEARCH_INTENT",
        searchUrlTemplate: String = ""
    ) {
        viewModelScope.launch {
            repository.addFocusSearchApp(
                FocusSearchAppEntity(
                    appName = appName.trim(),
                    packageName = packageName.trim(),
                    searchActionType = searchActionType,
                    searchUrlTemplate = searchUrlTemplate.trim(),
                    isEnabled = true
                )
            )
            showNotification("Controlled App Added", "$appName added to Focus Search", "CONTROL")
        }
    }

    fun deleteFocusSearchApp(app: FocusSearchAppEntity) {
        viewModelScope.launch {
            repository.deleteFocusSearchApp(app)
            showNotification("App Removed", "${app.appName} removed from Focus Search", "CONTROL")
        }
    }

    fun updateFocusSearchApp(app: FocusSearchAppEntity) {
        viewModelScope.launch {
            repository.updateFocusSearchApp(app)
        }
    }

    // --- Focus Search Actions ---
    fun toggleFocusSearchSite(site: FocusSearchSiteEntity) {
        viewModelScope.launch {
            repository.toggleFocusSearchSite(site)
        }
    }

    fun addFocusSearchSite(name: String, domain: String, searchUrlTemplate: String) {
        viewModelScope.launch {
            val template = if (searchUrlTemplate.isBlank()) {
                "https://$domain/search?q=%s"
            } else {
                searchUrlTemplate.trim()
            }
            repository.addFocusSearchSite(
                FocusSearchSiteEntity(
                    name = name.trim(),
                    domain = domain.trim(),
                    searchUrlTemplate = template,
                    isEnabled = true
                )
            )
        }
    }

    fun deleteFocusSearchSite(site: FocusSearchSiteEntity) {
        viewModelScope.launch {
            repository.deleteFocusSearchSite(site)
        }
    }

    // --- App Control Actions ---
    fun toggleAppControl(appControl: AppControlEntity) {
        viewModelScope.launch {
            repository.toggleAppControl(appControl)
        }
    }

    fun toggleAppControl(appControl: AppControlEntity, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleAppControl(appControl, enabled)
            val stateText = if (enabled) "Control ON" else "Control OFF"
            showNotification(
                title = "${appControl.appName}: $stateText",
                message = if (enabled) "Study Flow restrictions enabled for ${appControl.appName}" else "Restrictions disabled for ${appControl.appName}",
                type = "CONTROL"
            )
        }
    }

    fun addAppControl(
        appName: String,
        domain: String,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int
    ) {
        viewModelScope.launch {
            repository.addAppControl(
                AppControlEntity(
                    appName = appName.trim(),
                    targetDomain = domain.trim(),
                    startHour = startHour,
                    startMinute = startMinute,
                    endHour = endHour,
                    endMinute = endMinute,
                    isEnabled = true
                )
            )
            showNotification(
                "Control Schedule Created",
                "$appName blocked between ${String.format("%02d:%02d", startHour, startMinute)} - ${String.format("%02d:%02d", endHour, endMinute)}",
                "CONTROL"
            )
        }
    }

    fun deleteAppControl(appControl: AppControlEntity) {
        viewModelScope.launch {
            repository.deleteAppControl(appControl)
        }
    }

    fun isAppControlActiveNow(control: AppControlEntity): Boolean {
        if (!control.isEnabled) return false
        val now = LocalTime.now()
        val currentMinutes = now.hour * 60 + now.minute
        val startMinutes = control.startHour * 60 + control.startMinute
        val endMinutes = control.endHour * 60 + control.endMinute

        return if (startMinutes <= endMinutes) {
            currentMinutes in startMinutes until endMinutes
        } else {
            // Over midnight
            currentMinutes >= startMinutes || currentMinutes < endMinutes
        }
    }

    fun triggerBlockedAppAttempt(appName: String) {
        _blockedAppAttempt.value = appName
    }

    fun dismissBlockedAppAttempt() {
        _blockedAppAttempt.value = null
    }

    // --- Block Shorts Actions ---
    fun updateBlockShortsConfig(newConfig: BlockShortsEntity): Boolean {
        val current = _blockShortsSettings.value
        val isLocked = current.isStrictModeEnabled && _isTimerRunning.value && _timerMode.value == "Focus"
        if (isLocked) {
            val tryingToDisableMaster = !newConfig.isEnabled && current.isEnabled
            val tryingToDisableStrict = !newConfig.isStrictModeEnabled && current.isStrictModeEnabled
            val tryingToDisableYouTube = !newConfig.blockYouTubeShorts && current.blockYouTubeShorts
            val tryingToDisableInstagram = !newConfig.blockInstagramReels && current.blockInstagramReels

            if (tryingToDisableMaster || tryingToDisableStrict || tryingToDisableYouTube || tryingToDisableInstagram) {
                showNotification(
                    "Strict Mode Active",
                    "Block Shorts protection cannot be disabled while a focus session is actively running.",
                    "CONTROL"
                )
                return false
            }
        }

        viewModelScope.launch {
            repository.saveBlockShortsSettings(newConfig)
            _blockShortsSettings.value = newConfig
            BlockShortsManager.currentConfig = newConfig
        }
        return true
    }

    fun toggleBlockShortsMaster(enabled: Boolean): Boolean {
        val current = _blockShortsSettings.value
        return updateBlockShortsConfig(current.copy(isEnabled = enabled))
    }

    fun toggleBlockYouTubeShorts(enabled: Boolean): Boolean {
        val current = _blockShortsSettings.value
        return updateBlockShortsConfig(current.copy(blockYouTubeShorts = enabled))
    }

    fun toggleBlockInstagramReels(enabled: Boolean): Boolean {
        val current = _blockShortsSettings.value
        return updateBlockShortsConfig(current.copy(blockInstagramReels = enabled))
    }

    fun toggleBlockShortsSchedule(enabled: Boolean): Boolean {
        val current = _blockShortsSettings.value
        return updateBlockShortsConfig(current.copy(isScheduleEnabled = enabled))
    }

    fun setBlockShortsScheduleTime(
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int
    ): Boolean {
        val current = _blockShortsSettings.value
        return updateBlockShortsConfig(
            current.copy(
                scheduleStartHour = startHour,
                scheduleStartMinute = startMinute,
                scheduleEndHour = endHour,
                scheduleEndMinute = endMinute
            )
        )
    }

    fun toggleBlockShortsStrictMode(enabled: Boolean): Boolean {
        val current = _blockShortsSettings.value
        return updateBlockShortsConfig(current.copy(isStrictModeEnabled = enabled))
    }

    fun isBlockShortsActiveNow(): Boolean {
        return BlockShortsManager.isBlockShortsActive()
    }

    fun testShortsBlockingScreen(context: android.content.Context, platformName: String = "YouTube Shorts") {
        BlockShortsManager.triggerBlock(context, platformName, "Interactive feature test")
    }

    // --- Standalone Blocked Apps (User Controlled Blocker per platform) ---
    fun setGlobalStrictMode(enabled: Boolean) {
        _globalStrictMode.value = enabled
        BlockShortsManager.isGlobalStrictMode = enabled
    }

    fun toggleBlockedApp(app: BlockedAppEntity, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleBlockedApp(app, enabled)
            val action = if (enabled) "Blocking Active" else "Blocking Disabled"
            showNotification(
                title = "${app.name} $action",
                message = if (enabled)
                    "${app.name} ${app.contentType} is now protected and blocked."
                else
                    "${app.name} blocker is turned off. Access restored.",
                type = "SHIELD"
            )
        }
    }

    fun addBlockedApp(
        name: String,
        url: String,
        contentType: String,
        enabled: Boolean,
        scheduleEnabled: Boolean = false,
        startTime: String = "18:00",
        endTime: String = "22:00"
    ) {
        viewModelScope.launch {
            val app = BlockedAppEntity(
                name = name.trim(),
                url = url.trim(),
                icon = name.lowercase().trim(),
                contentType = contentType.ifBlank { "Short-form videos" }.trim(),
                enabled = enabled,
                scheduleEnabled = scheduleEnabled,
                startTime = startTime.ifBlank { "18:00" },
                endTime = endTime.ifBlank { "22:00" },
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.addBlockedApp(app)
            showNotification(
                title = "${app.name} Added",
                message = "Configured ${app.contentType} blocking for ${app.name}.",
                type = "SHIELD"
            )
        }
    }

    fun updateBlockedApp(app: BlockedAppEntity) {
        viewModelScope.launch {
            repository.updateBlockedApp(app.copy(updatedAt = System.currentTimeMillis()))
            showNotification(
                title = "${app.name} Settings Saved",
                message = "Blocker configuration updated for ${app.name}.",
                type = "SHIELD"
            )
        }
    }

    fun deleteBlockedApp(app: BlockedAppEntity) {
        viewModelScope.launch {
            repository.deleteBlockedApp(app)
            showNotification(
                title = "${app.name} Removed",
                message = "Removed ${app.name} from Block Shorts manager.",
                type = "INFO"
            )
        }
    }

    // --- Settings & Data Management ---
    fun updateSettings(updated: UserSettingsEntity) {
        viewModelScope.launch {
            repository.saveSettings(updated)
            _userSettings.value = updated
            if (!_isTimerRunning.value) {
                applyModeDuration(_timerMode.value, updated)
            }
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            repository.clearAllData()
            repository.seedInitialDataIfEmpty()
            showNotification("Sample Data Loaded", "Refreshed tasks and schedule with clean starter productivity templates.", "INFO")
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            pauseTimer()
            repository.clearAllData()
            // Reseed clean default
            repository.seedInitialDataIfEmpty()
            showNotification("Data Reset", "Database cleared and reinitialized with clean state.", "INFO")
        }
    }

    fun exportDataAsJson(): String {
        val taskCount = _tasks.value.size
        val sessionCount = _focusSessions.value.size
        val eventCount = _calendarEvents.value.size
        val streak = _userSettings.value.currentStreak
        return """
        {
          "application": "Study Flow",
          "version": "1.0",
          "exportDate": "${LocalDate.now()}",
          "streakDays": $streak,
          "totalTasks": $taskCount,
          "focusSessions": $sessionCount,
          "calendarEvents": $eventCount,
          "settings": {
             "focusDuration": ${_userSettings.value.focusDurationMinutes},
             "shortBreak": ${_userSettings.value.shortBreakMinutes},
             "longBreak": ${_userSettings.value.longBreakMinutes},
             "sound": ${_userSettings.value.soundEnabled}
          }
        }
        """.trimIndent()
    }

    // --- Streak & Notifications ---
    private fun incrementStreakIfEligible() {
        val today = LocalDate.now().toEpochDay()
        val current = _userSettings.value
        if (current.lastActiveDateEpochDay != today) {
            val newStreak = current.currentStreak + 1
            val newBest = maxOf(newStreak, current.bestStreak)
            val updated = current.copy(
                currentStreak = newStreak,
                bestStreak = newBest,
                lastActiveDateEpochDay = today
            )
            updateSettings(updated)
        }
    }

    fun showNotification(title: String, message: String, type: String = "INFO") {
        if (!_userSettings.value.notificationsEnabled) return
        val item = NotificationItem(title = title, message = message, type = type)
        _activeNotification.value = item
        _notificationHistory.value = listOf(item) + _notificationHistory.value.take(20)
    }

    fun dismissActiveNotification() {
        _activeNotification.value = null
    }
}
