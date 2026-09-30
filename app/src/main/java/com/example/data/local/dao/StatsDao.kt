package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.model.AppUsageStat
import kotlinx.coroutines.flow.Flow

@Dao
interface StatsDao {
    @Query("SELECT * FROM app_usage_stats ORDER BY openAttempts DESC")
    fun getAllStats(): Flow<List<AppUsageStat>>

    @Query("SELECT * FROM app_usage_stats WHERE packageName = :packageName")
    suspend fun getStatForPackage(packageName: String): AppUsageStat?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stat: AppUsageStat)

    @Query("UPDATE app_usage_stats SET openAttempts = openAttempts + 1, lastAttemptTimestamp = :timestamp WHERE packageName = :packageName")
    suspend fun incrementOpenAttempts(packageName: String, timestamp: Long)

    @Query("UPDATE app_usage_stats SET questsPassed = questsPassed + 1 WHERE packageName = :packageName")
    suspend fun incrementQuestsPassed(packageName: String)

    @Query("UPDATE app_usage_stats SET reasonsApproved = reasonsApproved + 1 WHERE packageName = :packageName")
    suspend fun incrementReasonsApproved(packageName: String)

    @Query("UPDATE app_usage_stats SET totalDurationMillis = totalDurationMillis + :duration WHERE packageName = :packageName")
    suspend fun addUsageDuration(packageName: String, duration: Long)
}
