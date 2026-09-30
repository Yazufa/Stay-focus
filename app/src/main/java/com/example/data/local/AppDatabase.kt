package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ProgressDao
import com.example.data.local.dao.ScheduleDao
import com.example.data.local.dao.StatsDao
import com.example.data.local.dao.UnlockLogDao
import com.example.data.local.model.AppUsageStat
import com.example.data.local.model.Schedule
import com.example.data.local.model.UnlockLog
import com.example.data.local.model.UserProgress

@Database(
    entities = [
        Schedule::class,
        AppUsageStat::class,
        UnlockLog::class,
        UserProgress::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao
    abstract fun statsDao(): StatsDao
    abstract fun unlockLogDao(): UnlockLogDao
    abstract fun progressDao(): ProgressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stayfocus_database"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
