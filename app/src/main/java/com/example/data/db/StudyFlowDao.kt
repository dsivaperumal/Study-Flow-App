package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

@Dao
interface StudyFlowDao {

    // Tasks
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()

    // Focus Sessions
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllFocusSessions(): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFocusSessions(sessions: List<FocusSessionEntity>)

    @Query("DELETE FROM focus_sessions")
    suspend fun clearAllFocusSessions()

    // Calendar Events
    @Query("SELECT * FROM calendar_events ORDER BY dateEpochDay ASC, startTime ASC")
    fun getAllCalendarEvents(): Flow<List<CalendarEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendarEvent(event: CalendarEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCalendarEvents(events: List<CalendarEventEntity>)

    @Update
    suspend fun updateCalendarEvent(event: CalendarEventEntity)

    @Delete
    suspend fun deleteCalendarEvent(event: CalendarEventEntity)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteCalendarEventById(id: Long)

    @Query("DELETE FROM calendar_events")
    suspend fun clearAllCalendarEvents()

    // Focus Search Sites
    @Query("SELECT * FROM focus_search_sites ORDER BY id ASC")
    fun getAllFocusSearchSites(): Flow<List<FocusSearchSiteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSearchSite(site: FocusSearchSiteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFocusSearchSites(sites: List<FocusSearchSiteEntity>)

    @Update
    suspend fun updateFocusSearchSite(site: FocusSearchSiteEntity)

    @Delete
    suspend fun deleteFocusSearchSite(site: FocusSearchSiteEntity)

    @Query("DELETE FROM focus_search_sites WHERE id = :id")
    suspend fun deleteFocusSearchSiteById(id: Long)

    @Query("DELETE FROM focus_search_sites")
    suspend fun clearAllFocusSearchSites()

    // Focus Search Controlled Apps
    @Query("SELECT * FROM focus_search_apps ORDER BY id ASC")
    fun getAllFocusSearchApps(): Flow<List<FocusSearchAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSearchApp(app: FocusSearchAppEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFocusSearchApps(apps: List<FocusSearchAppEntity>)

    @Update
    suspend fun updateFocusSearchApp(app: FocusSearchAppEntity)

    @Delete
    suspend fun deleteFocusSearchApp(app: FocusSearchAppEntity)

    @Query("DELETE FROM focus_search_apps WHERE id = :id")
    suspend fun deleteFocusSearchAppById(id: Long)

    @Query("DELETE FROM focus_search_apps")
    suspend fun clearAllFocusSearchApps()

    // App Controls
    @Query("SELECT * FROM app_controls ORDER BY id ASC")
    fun getAllAppControls(): Flow<List<AppControlEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppControl(appControl: AppControlEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAppControls(controls: List<AppControlEntity>)

    @Update
    suspend fun updateAppControl(appControl: AppControlEntity)

    @Delete
    suspend fun deleteAppControl(appControl: AppControlEntity)

    @Query("DELETE FROM app_controls WHERE id = :id")
    suspend fun deleteAppControlById(id: Long)

    @Query("DELETE FROM app_controls")
    suspend fun clearAllAppControls()

    // User Settings
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getUserSettings(): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: UserSettingsEntity)

    @Query("DELETE FROM user_settings")
    suspend fun clearUserSettings()

    // Block Shorts Settings
    @Query("SELECT * FROM block_shorts_settings WHERE id = 1 LIMIT 1")
    fun getBlockShortsSettings(): Flow<BlockShortsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBlockShorts(settings: BlockShortsEntity)

    @Query("DELETE FROM block_shorts_settings")
    suspend fun clearBlockShortsSettings()

    // Blocked Apps (User Controlled Blocker per platform)
    @Query("SELECT * FROM blocked_apps ORDER BY id ASC")
    fun getAllBlockedApps(): Flow<List<BlockedAppEntity>>

    @Query("SELECT * FROM blocked_apps WHERE id = :id LIMIT 1")
    suspend fun getBlockedAppById(id: Long): BlockedAppEntity?

    @Query("SELECT * FROM blocked_apps WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getBlockedAppByName(name: String): BlockedAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedApp(app: BlockedAppEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBlockedApps(apps: List<BlockedAppEntity>)

    @Update
    suspend fun updateBlockedApp(app: BlockedAppEntity)

    @Delete
    suspend fun deleteBlockedApp(app: BlockedAppEntity)

    @Query("DELETE FROM blocked_apps WHERE id = :id")
    suspend fun deleteBlockedAppById(id: Long)

    @Query("DELETE FROM blocked_apps")
    suspend fun clearAllBlockedApps()
}
