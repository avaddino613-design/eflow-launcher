package com.readerlauncher.app.util

import android.content.Context

/** Single place for every SharedPreferences key this app uses. */
object Prefs {
    private const val FILE = "pageflow_prefs"
    private fun store(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    private const val KEY_DARK_MODE = "dark_mode_enabled"
    private const val KEY_FAVORITE_APPS = "favorite_apps"

    fun isOnboardingComplete(context: Context) = store(context).getBoolean(KEY_ONBOARDING_COMPLETE, false)
    fun setOnboardingComplete(context: Context, value: Boolean) =
        store(context).edit().putBoolean(KEY_ONBOARDING_COMPLETE, value).apply()

    /** null means "no explicit choice yet - follow the system setting". */
    fun getDarkModePreference(context: Context): Boolean? {
        val prefs = store(context)
        return if (prefs.contains(KEY_DARK_MODE)) prefs.getBoolean(KEY_DARK_MODE, false) else null
    }

    fun setDarkModePreference(context: Context, value: Boolean) =
        store(context).edit().putBoolean(KEY_DARK_MODE, value).apply()

    fun getFavoriteApps(context: Context): List<String> =
        store(context).getString(KEY_FAVORITE_APPS, "")
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?: emptyList()

    fun setFavoriteApps(context: Context, packages: List<String>) =
        store(context).edit().putString(KEY_FAVORITE_APPS, packages.joinToString(",")).apply()
}
