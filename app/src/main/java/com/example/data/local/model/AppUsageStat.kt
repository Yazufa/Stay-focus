package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_usage_stats")
data class AppUsageStat(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val openAttempts: Int = 0,
    val questsPassed: Int = 0,
    val reasonsApproved: Int = 0,
    val totalDurationMillis: Long = 0L,
    val lastAttemptTimestamp: Long = System.currentTimeMillis()
)
