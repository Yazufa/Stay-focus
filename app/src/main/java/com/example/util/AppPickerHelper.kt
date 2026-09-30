package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

data class InstalledApp(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean
)

object AppPickerHelper {

    fun getLaunchableApps(context: Context): List<InstalledApp> {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = packageManager.queryIntentActivities(intent, 0)
        val selfPackage = context.packageName

        return resolveInfos.mapNotNull { resolveInfo ->
            val pkg = resolveInfo.activityInfo.packageName
            if (pkg == selfPackage || pkg == "com.android.settings") {
                null
            } else {
                val name = resolveInfo.loadLabel(packageManager).toString()
                val isSystem = (resolveInfo.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                InstalledApp(packageName = pkg, appName = name, isSystemApp = isSystem)
            }
        }.distinctBy { it.packageName }.sortedBy { it.appName.lowercase() }
    }
}
