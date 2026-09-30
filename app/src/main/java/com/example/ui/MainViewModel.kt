package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.model.AppUsageStat
import com.example.data.local.model.Schedule
import com.example.data.local.model.UnlockLog
import com.example.data.local.model.UserProgress
import com.example.data.repository.FocusRepository
import com.example.data.repository.SecureStorage
import com.example.data.repository.SettingsRepository
import com.example.util.AppPickerHelper
import com.example.util.InstalledApp
import com.example.util.PermissionHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PermissionsState(
    val hasOverlay: Boolean,
    val hasUsageStats: Boolean,
    val hasAccessibility: Boolean,
    val isDeviceAdmin: Boolean,
    val isBatteryIgnored: Boolean
) {
    val isAllGranted: Boolean
        get() = hasOverlay && hasUsageStats && hasAccessibility && isDeviceAdmin && isBatteryIgnored
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val focusRepository = FocusRepository(context)
    private val settingsRepository = SettingsRepository(context)
    private val secureStorage = SecureStorage(context)

    val schedules: StateFlow<List<Schedule>> = focusRepository.allSchedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats: StateFlow<List<AppUsageStat>> = focusRepository.allStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<UnlockLog>> = focusRepository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProgress: StateFlow<UserProgress?> = focusRepository.userProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isMasterLockEnabled: StateFlow<Boolean> = settingsRepository.isMasterLockEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isRestModeActive: StateFlow<Boolean> = settingsRepository.restModeActive
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val restModeEndTimestamp: StateFlow<Long> = settingsRepository.restModeEndTimestamp
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val questionsPerQuest: StateFlow<Int> = settingsRepository.questionsPerQuest
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3)

    val dailyBypassLimit: StateFlow<Int> = settingsRepository.dailyBypassLimit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3)

    val restDurationMinutes: StateFlow<Int> = settingsRepository.restDurationMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10)

    val restWhitelist: StateFlow<Set<String>> = settingsRepository.restWhitelist
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val simulateAllPermissions: StateFlow<Boolean> = settingsRepository.simulateAllPermissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val simulateDeviceAdmin: StateFlow<Boolean> = settingsRepository.simulateDeviceAdmin
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _permissionsState = MutableStateFlow(checkPermissions())
    val permissionsState: StateFlow<PermissionsState> = _permissionsState.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps.asStateFlow()

    private val _customApiKey = MutableStateFlow(secureStorage.getApiKey() ?: "")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _currentTimeMillis = MutableStateFlow(System.currentTimeMillis())
    val currentTimeMillis: StateFlow<Long> = _currentTimeMillis.asStateFlow()

    init {
        loadInstalledApps()
        startLiveTimer()
        observePermissionSimulations()
    }

    private fun observePermissionSimulations() {
        viewModelScope.launch {
            settingsRepository.simulateAllPermissions.collect {
                refreshPermissions()
            }
        }
        viewModelScope.launch {
            settingsRepository.simulateDeviceAdmin.collect {
                refreshPermissions()
            }
        }
    }

    private fun startLiveTimer() {
        viewModelScope.launch {
            while (isActive) {
                _currentTimeMillis.value = System.currentTimeMillis()
                delay(1000)
            }
        }
    }

    fun refreshPermissions() {
        _permissionsState.value = checkPermissions()
        viewModelScope.launch {
            focusRepository.syncUsageStatsWithSystem()
        }
    }

    fun setSimulateAllPermissions(simulate: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSimulateAllPermissions(simulate)
            refreshPermissions()
        }
    }

    fun setSimulateDeviceAdmin(simulate: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSimulateDeviceAdmin(simulate)
            refreshPermissions()
        }
    }

    private fun checkPermissions(): PermissionsState {
        val simAll = simulateAllPermissions.value
        val simAdmin = simulateDeviceAdmin.value
        return PermissionsState(
            hasOverlay = simAll || PermissionHelper.hasOverlayPermission(context),
            hasUsageStats = simAll || PermissionHelper.hasUsageStatsPermission(context),
            hasAccessibility = simAll || PermissionHelper.hasAccessibilityPermission(context),
            isDeviceAdmin = simAll || simAdmin || PermissionHelper.isDeviceAdminActive(context),
            isBatteryIgnored = simAll || PermissionHelper.isIgnoringBatteryOptimizations(context)
        )
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _installedApps.value = AppPickerHelper.getLaunchableApps(context)
        }
    }

    fun setMasterLock(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setMasterLockEnabled(enabled)
        }
    }

    fun toggleRestMode(durationMinutes: Int) {
        viewModelScope.launch {
            if (isRestModeActive.value) {
                settingsRepository.stopRestMode()
            } else {
                settingsRepository.startRestMode(durationMinutes)
            }
        }
    }

    fun saveSchedule(schedule: Schedule) {
        viewModelScope.launch {
            focusRepository.saveSchedule(schedule)
        }
    }

    fun updateSchedule(schedule: Schedule) {
        viewModelScope.launch {
            focusRepository.updateSchedule(schedule)
        }
    }

    fun deleteSchedule(scheduleId: Long) {
        viewModelScope.launch {
            focusRepository.deleteSchedule(scheduleId)
        }
    }

    fun toggleSchedule(schedule: Schedule) {
        viewModelScope.launch {
            focusRepository.updateSchedule(schedule.copy(isEnabled = !schedule.isEnabled))
        }
    }

    fun setQuestionsPerQuest(count: Int) {
        viewModelScope.launch {
            settingsRepository.setQuestionsPerQuest(count)
        }
    }

    fun setDailyBypassLimit(limit: Int) {
        viewModelScope.launch {
            settingsRepository.setDailyBypassLimit(limit)
        }
    }

    fun setRestDurationMinutes(mins: Int) {
        viewModelScope.launch {
            settingsRepository.setRestDurationMinutes(mins)
        }
    }

    fun setRestWhitelist(packages: Set<String>) {
        viewModelScope.launch {
            settingsRepository.setRestWhitelist(packages)
        }
    }

    fun saveCustomApiKey(key: String) {
        _customApiKey.value = key
        if (key.isBlank()) {
            secureStorage.clearApiKey()
        } else {
            secureStorage.saveApiKey(key)
        }
    }

    fun completeAntiUninstallMathQuest() {
        viewModelScope.launch {
            focusRepository.startAntiUninstallCountdown()
        }
    }

    fun cancelAntiUninstall() {
        viewModelScope.launch {
            focusRepository.cancelAntiUninstallRequest()
        }
    }
}
