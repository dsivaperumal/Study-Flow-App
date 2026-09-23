package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AppControlEntity
import com.example.data.model.BlockShortsEntity
import com.example.data.model.BlockedAppEntity
import com.example.data.model.CalendarEventEntity
import com.example.data.model.FocusSearchAppEntity
import com.example.data.model.FocusSearchSiteEntity
import com.example.data.model.FocusSessionEntity
import com.example.data.model.TaskEntity
import com.example.data.model.UserSettingsEntity

@Database(
    entities = [
        TaskEntity::class,
        FocusSessionEntity::class,
        CalendarEventEntity::class,
        FocusSearchSiteEntity::class,
        FocusSearchAppEntity::class,
        AppControlEntity::class,
        UserSettingsEntity::class,
        BlockShortsEntity::class,
        BlockedAppEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studyFlowDao(): StudyFlowDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "study_flow_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
