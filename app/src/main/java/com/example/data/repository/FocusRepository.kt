package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.model.AppUsageStat
import com.example.data.local.model.Schedule
import com.example.data.local.model.UnlockLog
import com.example.data.local.model.UserProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
    private val scheduleDao = database.scheduleDao()
    private val statsDao = database.statsDao()
    private val unlockLogDao = database.unlockLogDao()
    private val progressDao = database.progressDao()

    // In-memory temporary unlock session cache: packageName -> expiresAt timestamp
    companion object {
        private val tempUnlockedApps = ConcurrentHashMap<String, Long>()

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

    val allSchedules: Flow<List<Schedule>> = scheduleDao.getAllSchedules()
    val allStats: Flow<List<AppUsageStat>> = statsDao.getAllStats()
    val recentLogs: Flow<List<UnlockLog>> = unlockLogDao.getRecentLogs()
    val userProgress: Flow<UserProgress?> = progressDao.getProgress()

    suspend fun saveSchedule(schedule: Schedule): Long {
        return scheduleDao.insertSchedule(schedule)
    }

    suspend fun updateSchedule(schedule: Schedule) {
        scheduleDao.updateSchedule(schedule)
    }

    suspend fun deleteSchedule(scheduleId: Long) {
        scheduleDao.deleteScheduleById(scheduleId)
    }

    suspend fun getActiveSchedules(): List<Schedule> {
        return scheduleDao.getActiveSchedules()
    }

    suspend fun isAppCurrentlyLocked(packageName: String): Boolean {
        // 1. If master lock is disabled, not locked
        val masterEnabled = settingsRepository.isMasterLockEnabled.first()
        if (!masterEnabled) return false

        // 2. If app is our own app or launcher/system essential, never lock
        if (packageName == context.packageName || packageName == "com.android.settings") {
            return false
        }

        // 3. If temporarily unlocked by passed quest or reason, not locked
        if (isAppTemporarilyUnlocked(packageName)) {
            return false
        }

        // 4. If rest mode is active and app is whitelisted, not locked
        val isRestActive = settingsRepository.restModeActive.first()
        if (isRestActive) {
            val whitelist = settingsRepository.restWhitelist.first()
            if (whitelist.contains(packageName)) {
                return false
            }
        }

        // 5. Check active schedules matching current day and time
        val schedules = scheduleDao.getActiveSchedules()
        if (schedules.isEmpty()) return false

        val calendar = Calendar.getInstance()
        // Calendar: Sun=1, Mon=2, Tue=3, Wed=4, Thu=5, Fri=6, Sat=7
        // In our model: 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun
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

        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentMinutesOfDay = currentHour * 60 + currentMinute

        for (schedule in schedules) {
            val days = schedule.getDaysList()
            if (days.isNotEmpty() && !days.contains(currentDay)) {
                continue
            }

            val packages = schedule.getPackagesList()
            if (!packages.contains(packageName)) {
                continue
            }

            // Check time bounds
            val startParts = schedule.startTime.split(":")
            val endParts = schedule.endTime.split(":")
            if (startParts.size == 2 && endParts.size == 2) {
                val startMin = (startParts[0].toIntOrNull() ?: 0) * 60 + (startParts[1].toIntOrNull() ?: 0)
                val endMin = (endParts[0].toIntOrNull() ?: 24) * 60 + (endParts[1].toIntOrNull() ?: 0)

                val isInWindow = if (startMin <= endMin) {
                    currentMinutesOfDay in startMin..endMin
                } else {
                    // Over midnight e.g. 22:00 to 06:00
                    currentMinutesOfDay >= startMin || currentMinutesOfDay <= endMin
                }

                if (isInWindow) {
                    return true
                }
            }
        }

        return false
    }

    suspend fun getScheduleForPackage(packageName: String): Schedule? {
        val schedules = scheduleDao.getActiveSchedules()
        return schedules.firstOrNull { it.getPackagesList().contains(packageName) }
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
            10 + (level * 10) // e.g. Level 1: 20 pts, Level 5: 60 pts
        } else {
            5 // Reason unlock gives less points
        }

        grantTemporaryUnlock(packageName, durationMinutes = 15)

        // Log entry
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

        // Stats
        if (method == "MATH_QUEST") {
            statsDao.incrementQuestsPassed(packageName)
        } else {
            statsDao.incrementReasonsApproved(packageName)
        }

        // User progress
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
                // If yesterday was active and didn't use bypass, streak increments
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
        // Sets math passed = true and records timestamp
        progressDao.updateAntiUninstallRequest(
            timestamp = System.currentTimeMillis(),
            passed = true
        )
        return true
    }

    suspend fun cancelAntiUninstallRequest() {
        progressDao.updateAntiUninstallRequest(0L, false)
    }
}
