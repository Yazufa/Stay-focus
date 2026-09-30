package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.model.UnlockLog
import kotlinx.coroutines.flow.Flow

@Dao
interface UnlockLogDao {
    @Query("SELECT * FROM unlock_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<UnlockLog>>

    @Query("SELECT * FROM unlock_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentLogs(): Flow<List<UnlockLog>>

    @Query("SELECT * FROM unlock_logs WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getLogsSince(sinceTimestamp: Long): Flow<List<UnlockLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: UnlockLog): Long

    @Query("SELECT COUNT(*) FROM unlock_logs WHERE timestamp >= :startOfDay AND success = 1 AND method = 'AI_REASON'")
    suspend fun getDailyBypassCount(startOfDay: Long): Int

    @Query("SELECT COUNT(*) FROM unlock_logs WHERE timestamp >= :startOfDay AND success = 1")
    suspend fun getTodayTotalUnlocks(startOfDay: Long): Int

    @Query("SELECT COUNT(*) FROM unlock_logs WHERE timestamp >= :startOfDay AND method = 'MATH_QUEST' AND success = 1")
    suspend fun getTodayMathPasses(startOfDay: Long): Int
}
