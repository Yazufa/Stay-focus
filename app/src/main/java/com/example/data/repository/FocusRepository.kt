package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.model.AppUsageStat
import com.example.data.local.model.Schedule
import com.example.data.local.model.UnlockLog
import com.example.data.local.model.UserProgress
import com.example.data.math.MathQuestGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class FocusRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getInstance(context),
    private val settingsRepository: SettingsRepository = SettingsRepository(context)
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val scheduleDao = database.scheduleDao()
    private val statsDao = database.statsDao()
    private val unlockLogDao = database.unlockLogDao()
    private val progressDao = database.progressDao()

    companion object {
        private val tempUnlockedApps = ConcurrentHashMap<String, Long>()

        @Volatile var cachedActiveSchedules: List<Schedule> = emptyList()
        @Volatile var cachedMasterEnabled: Boolean = true
        @Volatile var cachedRestActive: Boolean = false
        @Volatile var cachedRestWhitelist: Set<String> = emptySet()

        fun isAppTemporarilyUnlocked(packageName: String): Boolean {
            val expiresAt = tempUnlockedApps[packageName] ?: return false
            if (System.currentTimeMillis() < expiresAt) {
                return true
            }
            tempUnlockedApps.remove(packageName)
            return false
        }

        fun grantTemporaryUnlock(packageName: String, durationMinutes: Int = 15) {
            val expiresAt = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
            tempUnlockedApps[packageName] = expiresAt
        }

        fun clearTemporaryUnlock(packageName: String) {
            tempUnlockedApps.remove(packageName)
        }
    }

    init {
        // Keep in-memory cache synchronized with Room & DataStore for instant zero-latency checks
        repositoryScope.launch {
            scheduleDao.getAllSchedules().collect { schedules ->
                cachedActiveSchedules = schedules.filter { it.isEnabled }
            }
        }
        repositoryScope.launch {
            settingsRepository.isMasterLockEnabled.collect { enabled ->
                cachedMasterEnabled = enabled
            }
        }
        repositoryScope.launch {
            settingsRepository.restModeActive.collect { active ->
                cachedRestActive = active
            }
        }
        repositoryScope.launch {
            settingsRepository.restWhitelist.collect { whitelist ->
                cachedRestWhitelist = whitelist
            }
        }
    }

    val allSchedules: Flow<List<Schedule>> = scheduleDao.getAllSchedules()
    val allStats: Flow<List<AppUsageStat>> = statsDao.getAllStats()
    val recentLogs: Flow<List<UnlockLog>> = unlockLogDao.getRecentLogs()
    val userProgress: Flow<UserProgress?> = progressDao.getProgress()

    suspend fun saveSchedule(schedule: Schedule): Long {
        val id = scheduleDao.insertSchedule(schedule)
        cachedActiveSchedules = scheduleDao.getActiveSchedules()
        return id
    }

    suspend fun updateSchedule(schedule: Schedule) {
        scheduleDao.updateSchedule(schedule)
        cachedActiveSchedules = scheduleDao.getActiveSchedules()
    }

    suspend fun deleteSchedule(scheduleId: Long) {
        scheduleDao.deleteScheduleById(scheduleId)
        cachedActiveSchedules = scheduleDao.getActiveSchedules()
    }

    suspend fun getActiveSchedules(): List<Schedule> {
        return scheduleDao.getActiveSchedules()
    }

    fun reloadActiveSchedulesSync() {
        try {
            cachedActiveSchedules = scheduleDao.getActiveSchedulesBlocking()
        } catch (e: Exception) {
            // Fallback
        }
    }

    /**
     * Ultra-fast in-memory check (< 1ms) for real-time interception in AccessibilityService
     */
    fun isAppCurrentlyLockedFast(packageName: String): Boolean {
        if (!cachedMasterEnabled) return false
        if (packageName == context.packageName || packageName == "com.android.settings") return false
        if (isAppTemporarilyUnlocked(packageName)) return false
        if (cachedRestActive && cachedRestWhitelist.contains(packageName)) return false

        var schedules = cachedActiveSchedules
        if (schedules.isEmpty()) {
            try {
                schedules = scheduleDao.getActiveSchedulesBlocking()
                cachedActiveSchedules = schedules
            } catch (e: Exception) {
                // Fallback
            }
        }
        if (schedules.isEmpty()) return false

        val calendar = Calendar.getInstance()
        val currentDay = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
        val currentMinutesOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

        for (schedule in schedules) {
            val days = schedule.getDaysList()
            if (days.isNotEmpty() && !days.contains(currentDay)) continue

            val packages = schedule.getPackagesList()
            if (!packages.contains(packageName)) continue

            val startParts = schedule.startTime.split(":")
            val endParts = schedule.endTime.split(":")
            if (startParts.size == 2 && endParts.size == 2) {
                val startMin = (startParts[0].toIntOrNull() ?: 0) * 60 + (startParts[1].toIntOrNull() ?: 0)
                val endMin = (endParts[0].toIntOrNull() ?: 24) * 60 + (endParts[1].toIntOrNull() ?: 0)

                val isInWindow = if (startMin <= endMin) {
                    currentMinutesOfDay in startMin..endMin
                } else {
                    currentMinutesOfDay >= startMin || currentMinutesOfDay <= endMin
                }

                if (isInWindow) return true
            }
        }
        return false
    }

    fun getScheduleForPackageFast(packageName: String): Schedule? {
        return cachedActiveSchedules.firstOrNull { it.getPackagesList().contains(packageName) }
    }

    suspend fun isAppCurrentlyLocked(packageName: String): Boolean {
        return isAppCurrentlyLockedFast(packageName)
    }

    suspend fun getScheduleForPackage(packageName: String): Schedule? {
        return getScheduleForPackageFast(packageName)
    }

    suspend fun recordAttempt(packageName: String, appName: String) {
        val stat = statsDao.getStatForPackage(packageName)
        if (stat == null) {
            statsDao.insertOrUpdate(
                AppUsageStat(
                    packageName = packageName,
                    appName = appName,
                    openAttempts = 1,
                    lastAttemptTimestamp = System.currentTimeMillis()
                )
            )
        } else {
            statsDao.incrementOpenAttempts(packageName, System.currentTimeMillis())
        }
    }

    suspend fun recordUnlockSuccess(
        packageName: String,
        appName: String,
        method: String, // "MATH_QUEST" or "AI_REASON"
        level: Int,
        note: String? = null
    ): Int {
        val points = if (method == "MATH_QUEST") {
            val levelInfo = MathQuestGenerator.getLevelInfo(level)
            10 + levelInfo.pointsBonus
        } else {
            5
        }

        grantTemporaryUnlock(packageName, durationMinutes = 15)

        unlockLogDao.insertLog(
            UnlockLog(
                packageName = packageName,
                appName = appName,
                method = method,
                success = true,
                note = note,
                pointsEarned = points,
                timestamp = System.currentTimeMillis()
            )
        )

        if (method == "MATH_QUEST") {
            statsDao.incrementQuestsPassed(packageName)
        } else {
            statsDao.incrementReasonsApproved(packageName)
        }

        updateProgressOnUnlock(points, isBypass = (method == "AI_REASON"))

        return points
    }

    suspend fun recordUnlockFailure(
        packageName: String,
        appName: String,
        method: String,
        note: String? = null
    ) {
        unlockLogDao.insertLog(
            UnlockLog(
                packageName = packageName,
                appName = appName,
                method = method,
                success = false,
                note = note,
                pointsEarned = 0,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    private suspend fun updateProgressOnUnlock(pointsEarned: Int, isBypass: Boolean) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val existing = progressDao.getProgressSync()

        if (existing == null) {
            progressDao.insertOrUpdate(
                UserProgress(
                    id = 1,
                    streakDays = 1,
                    totalPoints = pointsEarned,
                    lastActiveDate = todayStr,
                    usedBypassToday = isBypass,
                    bypassCountToday = if (isBypass) 1 else 0
                )
            )
        } else {
            val isNewDay = existing.lastActiveDate != todayStr
            val streak = if (isNewDay) {
                if (!existing.usedBypassToday) existing.streakDays + 1 else existing.streakDays
            } else {
                existing.streakDays
            }

            val bypassToday = if (isNewDay) isBypass else (existing.usedBypassToday || isBypass)
            val bypassCount = if (isNewDay) {
                if (isBypass) 1 else 0
            } else {
                if (isBypass) existing.bypassCountToday + 1 else existing.bypassCountToday
            }

            progressDao.insertOrUpdate(
                existing.copy(
                    streakDays = maxOf(1, streak),
                    totalPoints = existing.totalPoints + pointsEarned,
                    lastActiveDate = todayStr,
                    usedBypassToday = bypassToday,
                    bypassCountToday = bypassCount
                )
            )
        }
    }

    suspend fun getDailyBypassUsed(): Int {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return unlockLogDao.getDailyBypassCount(calendar.timeInMillis)
    }

    suspend fun startAntiUninstallCountdown(): Boolean {
        progressDao.updateAntiUninstallRequest(
            timestamp = System.currentTimeMillis(),
            passed = true
        )
        return true
    }

    suspend fun cancelAntiUninstallRequest() {
        progressDao.updateAntiUninstallRequest(0L, false)
    }

    suspend fun syncUsageStatsWithSystem() {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? android.app.usage.UsageStatsManager ?: return
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = cal.timeInMillis
        val endTime = System.currentTimeMillis()
        try {
            val statsList = usageStatsManager.queryUsageStats(android.app.usage.UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
            if (!statsList.isNullOrEmpty()) {
                val dbStats = statsDao.getAllStatsSync()
                for (stat in dbStats) {
                    val matching = statsList.find { it.packageName == stat.packageName }
                    if (matching != null && matching.totalTimeInForeground > 0) {
                        statsDao.insertOrUpdate(stat.copy(totalDurationMillis = matching.totalTimeInForeground))
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore if permission not granted
        }
    }
}
