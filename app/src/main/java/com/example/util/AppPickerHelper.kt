package com.example.util

import android.content.Context
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
        val selfPackage = context.packageName

        val appsList = mutableListOf<InstalledApp>()
        try {
            // Using getInstalledPackages with QUERY_ALL_PACKAGES permission
            val packages = packageManager.getInstalledPackages(0)
            for (pkgInfo in packages) {
                val pkgName = pkgInfo.packageName
                if (pkgName == selfPackage) continue

                val appInfo = pkgInfo.applicationInfo ?: continue
                // Exclude core system critical packages that must never be locked
                if (pkgName == "android" ||
                    pkgName == "com.android.systemui" ||
                    pkgName == "com.google.android.inputmethod.latin" ||
                    pkgName == "com.google.android.packageinstaller" ||
                    pkgName == "com.android.packageinstaller"
                ) {
                    continue
                }

                val appName = try {
                    appInfo.loadLabel(packageManager).toString().trim()
                } catch (e: Exception) {
                    pkgName
                }

                val displayName = if (appName.isBlank()) pkgName else appName
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                appsList.add(
                    InstalledApp(
                        packageName = pkgName,
                        appName = displayName,
                        isSystemApp = isSystem
                    )
                )
            }
        } catch (e: Exception) {
            // Fallback to launcher activities if any exception occurs
            val intent = android.content.Intent(android.content.Intent.ACTION_MAIN, null).apply {
                addCategory(android.content.Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = packageManager.queryIntentActivities(intent, 0)
            for (ri in resolveInfos) {
                val pkg = ri.activityInfo.packageName
                if (pkg == selfPackage) continue
                val name = ri.loadLabel(packageManager).toString()
                val isSystem = (ri.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                appsList.add(InstalledApp(packageName = pkg, appName = name, isSystemApp = isSystem))
            }
        }

        return appsList.distinctBy { it.packageName }.sortedBy { it.appName.lowercase() }
    }
}
