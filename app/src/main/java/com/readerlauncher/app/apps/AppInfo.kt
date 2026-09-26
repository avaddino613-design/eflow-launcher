package com.readerlauncher.app.apps

import android.graphics.drawable.Drawable

data class AppInfo(
    val label: String,
    val packageName: String,
    val isSystemApp: Boolean,
    val hasLaunchIntent: Boolean,
    val icon: Drawable?
)
