package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.repository.FocusRepository
import com.example.data.repository.SettingsRepository
import com.example.ui.lockscreen.LockScreenActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AppMonitorService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var monitorJob: Job? = null
    private lateinit var focusRepository: FocusRepository
    private lateinit var settingsRepository: SettingsRepository

    private var lastCheckedPackage: String? = null
    private var lastCheckedTime: Long = 0L

    override fun onCreate() {
        super.onCreate()
        focusRepository = FocusRepository(applicationContext)
        settingsRepository = SettingsRepository(applicationContext)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Perlindungan fokus aktif"))
        startMonitoringLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startMonitoringLoop() {
        monitorJob?.cancel()
        monitorJob = serviceScope.launch {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager

            while (isActive) {
                try {
                    // Check Rest Mode countdown expiration
                    val isRestActive = settingsRepository.restModeActive.first()
                    val restEnd = settingsRepository.restModeEndTimestamp.first()
                    if (isRestActive && restEnd > 0) {
                        val remainingSec = (restEnd - System.currentTimeMillis()) / 1000
                        if (remainingSec <= 0) {
                            settingsRepository.stopRestMode()
                            updateNotification("Mode Istirahat Selesai • Aplikasi dikunci kembali")
                        } else {
                            val min = remainingSec / 60
                            val sec = remainingSec % 60
                            updateNotification("Mode Istirahat: %02d:%02d tersisa".format(min, sec))
                        }
                    } else {
                        updateNotification("stayfocus aktif • Fokus terjaga")
                    }

                    // Fallback polling for foreground app via UsageStats if permission granted
                    if (usageStatsManager != null) {
                        val foregroundPkg = getForegroundApp(usageStatsManager)
                        if (foregroundPkg != null &&
                            foregroundPkg != packageName &&
                            foregroundPkg != "com.android.systemui"
                        ) {
                            val now = System.currentTimeMillis()
                            if (foregroundPkg != lastCheckedPackage || (now - lastCheckedTime) > 2000) {
                                if (focusRepository.isAppCurrentlyLocked(foregroundPkg)) {
                                    lastCheckedPackage = foregroundPkg
                                    lastCheckedTime = now

                                    val appName = try {
                                        val appInfo = packageManager.getApplicationInfo(foregroundPkg, 0)
                                        packageManager.getApplicationLabel(appInfo).toString()
                                    } catch (e: PackageManager.NameNotFoundException) {
                                        foregroundPkg
                                    }

                                    focusRepository.recordAttempt(foregroundPkg, appName)
                                    val schedule = focusRepository.getScheduleForPackage(foregroundPkg)
                                    val level = schedule?.questLevel ?: 1

                                    val lockIntent = Intent(applicationContext, LockScreenActivity::class.java).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                        putExtra(LockScreenActivity.EXTRA_PACKAGE_NAME, foregroundPkg)
                                        putExtra(LockScreenActivity.EXTRA_APP_NAME, appName)
                                        putExtra(LockScreenActivity.EXTRA_QUEST_LEVEL, level)
                                    }
                                    startActivity(lockIntent)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Silent failover
                }
                delay(1500)
            }
        }
    }

    private fun getForegroundApp(usageStatsManager: UsageStatsManager): String? {
        val time = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(time - 10000, time)
        val event = UsageEvents.Event()
        var lastPkg: String? = null

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                lastPkg = event.packageName
            }
        }
        return lastPkg
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(statusText: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("stayfocus")
            .setContentText(statusText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(statusText: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(statusText))
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        private const val CHANNEL_ID = "stayfocus_monitor_channel"
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, AppMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
