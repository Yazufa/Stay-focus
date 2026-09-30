package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey
    val id: Int = 1,
    val streakDays: Int = 0,
    val totalPoints: Int = 0,
    val lastActiveDate: String = "", // "YYYY-MM-DD"
    val usedBypassToday: Boolean = false,
    val bypassCountToday: Int = 0,
    val antiUninstallRequestedAt: Long = 0L, // timestamp when user started deactivation
    val antiUninstallMathPassed: Boolean = false
)
