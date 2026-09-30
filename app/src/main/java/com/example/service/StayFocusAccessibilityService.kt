package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityEvent
import com.example.data.local.AppDatabase
import com.example.data.repository.FocusRepository
import com.example.ui.lockscreen.LockScreenActivity
import com.example.util.PermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class StayFocusAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var focusRepository: FocusRepository
    private lateinit var database: AppDatabase
    private var lastInterceptedPackage: String? = null
    private var lastInterceptedTime: Long = 0

    override fun onServiceConnected() {
        super.onServiceConnected()
        focusRepository = FocusRepository(applicationContext)
        database = AppDatabase.getInstance(applicationContext)
        focusRepository.reloadActiveSchedulesSync()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val eventType = event.eventType
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED
        ) {
            return
        }

        val packageName = event.packageName?.toString() ?: return
        val selfPackage = applicationContext.packageName

        // Never intercept self or system navigation bar
        if (packageName == selfPackage || packageName == "com.android.systemui") {
            return
        }

        val now = System.currentTimeMillis()

        // 1. Anti-Uninstall Tamper Prevention check
        if (isAttemptingUninstallOrSettingsTamper(packageName, event)) {
            val isAdmin = PermissionHelper.isDeviceAdminActive(applicationContext)
            if (isAdmin) {
                val progress = runBlocking(Dispatchers.IO) {
                    database.progressDao().getProgressSync()
                }
                val requestedAt = progress?.antiUninstallRequestedAt ?: 0L
                val mathPassed = progress?.antiUninstallMathPassed ?: false
                val isUnlocked = mathPassed && requestedAt > 0 && (now - requestedAt) >= (24 * 60 * 60 * 1000L)

                if (!isUnlocked) {
                    // Close settings/uninstaller immediately
                    performGlobalAction(GLOBAL_ACTION_HOME)

                    if (now - lastInterceptedTime > 1000) {
                        lastInterceptedTime = now
                        val lockIntent = Intent(applicationContext, LockScreenActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            putExtra(LockScreenActivity.EXTRA_PACKAGE_NAME, packageName)
                            putExtra(LockScreenActivity.EXTRA_APP_NAME, "Proteksi Anti-Hapus stayfocus")
                            putExtra(LockScreenActivity.EXTRA_QUEST_LEVEL, 6) // Level Legenda
                            putExtra(LockScreenActivity.EXTRA_IS_ANTI_UNINSTALL, true)
                        }
                        startActivity(lockIntent)
                    }
                    return
                }
            }
        }

        // Debounce repeated events for the same package within 800ms
        if (packageName == lastInterceptedPackage && (now - lastInterceptedTime) < 800) {
            return
        }

        // 2. Fast synchronous in-memory check (< 1ms)
        if (focusRepository.isAppCurrentlyLockedFast(packageName)) {
            lastInterceptedPackage = packageName
            lastInterceptedTime = now

            // Immediately send the user HOME so the blocked app is closed on the spot!
            performGlobalAction(GLOBAL_ACTION_HOME)

            val appName = try {
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (e: PackageManager.NameNotFoundException) {
                packageName
            }

            val schedule = focusRepository.getScheduleForPackageFast(packageName)
            val questLevel = schedule?.questLevel ?: 1

            // Launch Lock Screen without delay
            val intent = Intent(applicationContext, LockScreenActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(LockScreenActivity.EXTRA_PACKAGE_NAME, packageName)
                putExtra(LockScreenActivity.EXTRA_APP_NAME, appName)
                putExtra(LockScreenActivity.EXTRA_QUEST_LEVEL, questLevel)
                putExtra(LockScreenActivity.EXTRA_IS_ANTI_UNINSTALL, false)
            }
            startActivity(intent)

            // Record attempt in background DB
            serviceScope.launch {
                focusRepository.recordAttempt(packageName, appName)
            }
        }
    }

    private fun isAttemptingUninstallOrSettingsTamper(pkgName: String, event: AccessibilityEvent): Boolean {
        val sensitivePackages = setOf(
            "com.android.settings",
            "com.google.android.packageinstaller",
            "com.android.packageinstaller",
            "com.miui.securitycenter",
            "com.coloros.safecenter",
            "com.samsung.android.lool",
            "com.samsung.android.sm_cn"
        )
        if (!sensitivePackages.contains(pkgName)) return false

        val eventTexts = event.text.joinToString(" ").lowercase()
        val selfName = "stayfocus"
        val selfPkg = applicationContext.packageName.lowercase()

        val mentionsApp = eventTexts.contains(selfName) || eventTexts.contains(selfPkg)
        if (mentionsApp) {
            val keywords = listOf("uninstall", "copot", "hapus", "force stop", "paksa berhenti", "clear data", "hapus data", "nonaktifkan", "disable")
            if (keywords.any { eventTexts.contains(it) }) return true
        }

        try {
            val rootNode = rootInActiveWindow ?: return mentionsApp
            val foundSelf = rootNode.findAccessibilityNodeInfosByText("stayfocus")
            val foundPkg = rootNode.findAccessibilityNodeInfosByText(applicationContext.packageName)
            if (foundSelf.isNotEmpty() || foundPkg.isNotEmpty()) {
                val tamperKeywords = listOf("uninstall", "copot", "hapus", "force stop", "paksa berhenti", "clear data", "hapus data", "device admin", "admin perangkat")
                for (kw in tamperKeywords) {
                    if (rootNode.findAccessibilityNodeInfosByText(kw).isNotEmpty()) {
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            // Ignored
        }

        return false
    }

    override fun onInterrupt() {}
}
