package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unlock_logs")
data class UnlockLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val method: String, // "MATH_QUEST", "AI_REASON", "REST_MODE"
    val success: Boolean,
    val note: String? = null,
    val pointsEarned: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
