package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "block_shorts_settings")
data class BlockShortsEntity(
    @PrimaryKey val id: Int = 1,
    val isEnabled: Boolean = true,
    val blockYouTubeShorts: Boolean = true,
    val blockInstagramReels: Boolean = true,
    val isScheduleEnabled: Boolean = false,
    val scheduleStartHour: Int = 18,
    val scheduleStartMinute: Int = 0,
    val scheduleEndHour: Int = 22,
    val scheduleEndMinute: Int = 0,
    val isStrictModeEnabled: Boolean = false,
    val blockedAttemptsCount: Int = 0
)
