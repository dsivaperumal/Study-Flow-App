package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val priority: String = "Medium", // Low, Medium, High
    val category: String = "Study", // Study, Assignment, Project, Personal, Work, Other
    val dueDateEpochDay: Long = 0L,
    val dueTime: String = "12:00",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: String = "Focus", // Focus, Short Break, Long Break
    val durationMinutes: Int = 25,
    val timestamp: Long = System.currentTimeMillis(),
    val epochDay: Long = 0L,
    val isCompleted: Boolean = true
)

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val dateEpochDay: Long = 0L,
    val startTime: String = "10:00",
    val endTime: String = "11:00",
    val category: String = "Study"
)

@Entity(tableName = "focus_search_sites")
data class FocusSearchSiteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val domain: String,
    val searchUrlTemplate: String, // e.g. "https://www.youtube.com/results?search_query="
    val isEnabled: Boolean = true
)

@Entity(tableName = "focus_search_apps")
data class FocusSearchAppEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val appName: String,
    val packageName: String,
    val searchActionType: String = "SEARCH_INTENT", // YOUTUBE, INSTAGRAM, FACEBOOK, REDDIT, TWITTER, TIKTOK, LINKEDIN, WEB, GENERIC
    val searchUrlTemplate: String = "",
    val isEnabled: Boolean = true,
    val iconKey: String = "apps"
)

@Entity(tableName = "app_controls")
data class AppControlEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val appName: String,
    val targetDomain: String,
    val startHour: Int = 18,
    val startMinute: Int = 0,
    val endHour: Int = 20,
    val endMinute: Int = 0,
    val isEnabled: Boolean = true
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val themeMode: String = "SYSTEM", // LIGHT, DARK, SYSTEM
    val fontSizePercentage: Int = 100, // 90, 100, 110, 120
    val focusDurationMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val autoStartBreaks: Boolean = false,
    val autoStartFocus: Boolean = false,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val focusSearchEnabled: Boolean = true,
    val currentStreak: Int = 7,
    val bestStreak: Int = 14,
    val lastActiveDateEpochDay: Long = 0L
)
