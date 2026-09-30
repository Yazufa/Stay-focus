package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val daysOfWeek: String, // Comma-separated: 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun
    val startTime: String,   // "HH:mm" 24h format
    val endTime: String,     // "HH:mm" 24h format
    val lockedPackages: String, // Comma-separated package names
    val questLevel: Int = 1, // 1 to 5
    val isEnabled: Boolean = true
) {
    fun getDaysList(): List<Int> {
        return if (daysOfWeek.isBlank()) emptyList()
        else daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    fun getPackagesList(): List<String> {
        return if (lockedPackages.isBlank()) emptyList()
        else lockedPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
}
