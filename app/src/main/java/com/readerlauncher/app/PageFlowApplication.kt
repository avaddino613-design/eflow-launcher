package com.readerlauncher.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.readerlauncher.app.util.Prefs

/**
 * Applies the saved dark-mode preference before any Activity is created.
 * Until the user explicitly toggles it in Settings, this follows the
 * system's own light/dark setting.
 */
class PageFlowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val mode = when (Prefs.getDarkModePreference(this)) {
            true -> AppCompatDelegate.MODE_NIGHT_YES
            false -> AppCompatDelegate.MODE_NIGHT_NO
            null -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
