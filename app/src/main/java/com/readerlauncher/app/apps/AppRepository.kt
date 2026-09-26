package com.readerlauncher.app.apps

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings

class AppRepository(private val context: Context) {

    private val pm: PackageManager = context.packageManager

    /**
     * Returns every app on the device, not just the subset a normal app
     * would see.
     *
     * Two things make this possible here where it wouldn't be in a regular
     * app:
     *  1) On Android 11+, apps normally only see a filtered list of other
     *     packages ("package visibility"). Apps that hold the HOME role
     *     (i.e. are set as the default launcher) are automatically exempted
     *     from that filtering by the OS, so once PageFlow is the default
     *     launcher, these PackageManager calls return every installed
     *     package - no QUERY_ALL_PACKAGES permission needed.
     *  2) getInstalledApplications() additionally pulls in apps that never
     *     expose a launcher icon at all (many pre-installed system apps),
     *     so toggling "show system apps" really does show everything.
     *
     * @param includeSystemApps when false, only apps with a normal launcher
     *   icon are returned (what most people expect from an app drawer).
     *   When true, every installed package is included, flagged so the UI
     *   can badge them.
     */
    fun getAllApps(includeSystemApps: Boolean): List<AppInfo> {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val launchablePackages = pm.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)
            .map { it.activityInfo.packageName }
            .toSet()

        val candidatePackages: Set<String> = if (includeSystemApps) {
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .map { it.packageName }
                .toSet() + launchablePackages
        } else {
            launchablePackages
        }

        return candidatePackages.mapNotNull { pkg ->
            try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                if (!includeSystemApps && isSystem && pkg !in launchablePackages) {
                    return@mapNotNull null
                }
                AppInfo(
                    label = pm.getApplicationLabel(appInfo).toString(),
                    packageName = pkg,
                    isSystemApp = isSystem,
                    hasLaunchIntent = pkg in launchablePackages,
                    icon = pm.getApplicationIcon(appInfo)
                )
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
        }.sortedBy { it.label.lowercase() }
    }

    /**
     * Launches the app normally. If it has no launcher activity (common for
     * background/system apps), falls back to that app's system "App info"
     * screen so the tap still does something useful instead of silently
     * failing.
     */
    fun launchOrOpenSettings(context: Context, packageName: String) {
        val launchIntent = pm.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(settingsIntent)
        }
    }
}
