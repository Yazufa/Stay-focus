package com.example.service

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class StayFocusDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "Proteksi Anti-Hapus stayfocus Aktif!", Toast.LENGTH_SHORT).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence? {
        val db = AppDatabase.getInstance(context)
        val progress = runBlocking(Dispatchers.IO) {
            db.progressDao().getProgressSync()
        }

        val requestedAt = progress?.antiUninstallRequestedAt ?: 0L
        val mathPassed = progress?.antiUninstallMathPassed ?: false
        val hoursPassed = if (requestedAt > 0) {
            (System.currentTimeMillis() - requestedAt) / (1000 * 60 * 60)
        } else 0L

        return if (mathPassed && hoursPassed >= 24) {
            null // Allow deactivation!
        } else {
            "Proteksi Anti-Hapus aktif! Anda harus menyelesaikan Quest Matematika Level 5 di aplikasi stayfocus dan menunggu masa jeda 24 jam sebelum dapat mencopot aplikasi."
        }
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(context)
            db.progressDao().updateAntiUninstallRequest(0L, false)
        }
        Toast.makeText(context, "Proteksi Anti-Hapus dinonaktifkan.", Toast.LENGTH_SHORT).show()
    }
}
