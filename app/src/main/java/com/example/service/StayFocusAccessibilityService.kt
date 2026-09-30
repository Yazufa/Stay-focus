package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityEvent
import com.example.data.repository.FocusRepository
import com.example.ui.lockscreen.LockScreenActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class StayFocusAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var focusRepository: FocusRepository
    private var lastInterceptedPackage: String? = null
    private var lastInterceptedTime: Long = 0

    override fun onServiceConnected() {
        super.onServiceConnected()
        focusRepository = FocusRepository(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val packageName = event.packageName?.toString() ?: return
        if (packageName == applicationContext.packageName ||
            packageName == "com.android.systemui" ||
            packageName == "com.google.android.inputmethod.latin"
        ) {
            return
        }

        val now = System.currentTimeMillis()
        if (packageName == lastInterceptedPackage && (now - lastInterceptedTime) < 1200) {
            return
        }

        serviceScope.launch {
            if (focusRepository.isAppCurrentlyLocked(packageName)) {
                lastInterceptedPackage = packageName
                lastInterceptedTime = now

                val appName = try {
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: PackageManager.NameNotFoundException) {
                    packageName
                }

                // Record attempt in stats
                focusRepository.recordAttempt(packageName, appName)

                val schedule = focusRepository.getScheduleForPackage(packageName)
                val questLevel = schedule?.questLevel ?: 1

                val intent = Intent(applicationContext, LockScreenActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra(LockScreenActivity.EXTRA_PACKAGE_NAME, packageName)
                    putExtra(LockScreenActivity.EXTRA_APP_NAME, appName)
                    putExtra(LockScreenActivity.EXTRA_QUEST_LEVEL, questLevel)
                }
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {}
}
