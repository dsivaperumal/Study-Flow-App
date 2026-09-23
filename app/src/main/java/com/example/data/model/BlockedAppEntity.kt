package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing an individual application or platform whose short-form content
 * can be blocked independently by the user.
 *
 * Persistence ensures that ON/OFF states and schedules survive app restarts and reloads.
 */
@Entity(tableName = "blocked_apps")
data class BlockedAppEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val url: String = "",
    val icon: String = "",
    val contentType: String = "Short-form videos",
    val enabled: Boolean = true,
    val scheduleEnabled: Boolean = false,
    val startTime: String = "18:00",
    val endTime: String = "22:00",
    val strictMode: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
