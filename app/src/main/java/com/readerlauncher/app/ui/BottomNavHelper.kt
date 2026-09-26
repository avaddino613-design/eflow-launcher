package com.readerlauncher.app.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.ImageViewCompat
import com.readerlauncher.app.MainActivity
import com.readerlauncher.app.R
import com.readerlauncher.app.apps.AppDrawerActivity
import com.readerlauncher.app.databinding.BottomNavBarBinding
import com.readerlauncher.app.library.LibraryActivity
import com.readerlauncher.app.settings.SettingsActivity

enum class NavTab { HOME, LIBRARY, APPS, SETTINGS }

/** Wires up the shared bottom_nav_bar.xml include: click targets + active-tab styling. */
object BottomNavHelper {

    fun setup(binding: BottomNavBarBinding, activity: AppCompatActivity, current: NavTab) {
        style(binding, current, activity)
        binding.navHome.setOnClickListener { navigate(activity, MainActivity::class.java, current == NavTab.HOME) }
        binding.navLibrary.setOnClickListener { navigate(activity, LibraryActivity::class.java, current == NavTab.LIBRARY) }
        binding.navApps.setOnClickListener { navigate(activity, AppDrawerActivity::class.java, current == NavTab.APPS) }
        binding.navSettings.setOnClickListener { navigate(activity, SettingsActivity::class.java, current == NavTab.SETTINGS) }
    }

    private fun navigate(activity: AppCompatActivity, target: Class<*>, alreadyHere: Boolean) {
        if (alreadyHere) return
        val intent = Intent(activity, target).apply { flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT }
        activity.startActivity(intent)
        @Suppress("DEPRECATION")
        activity.overridePendingTransition(0, 0)
    }

    private fun style(binding: BottomNavBarBinding, current: NavTab, activity: AppCompatActivity) {
        val activeColor = activity.getColor(R.color.nav_icon_active)
        val inactiveColor = activity.getColor(R.color.nav_icon_inactive)

        val tabs = listOf(
            NavTab.HOME to Triple(binding.navHome, binding.navHomeIcon, binding.navHomeLabel),
            NavTab.LIBRARY to Triple(binding.navLibrary, binding.navLibraryIcon, binding.navLibraryLabel),
            NavTab.APPS to Triple(binding.navApps, binding.navAppsIcon, binding.navAppsLabel),
            NavTab.SETTINGS to Triple(binding.navSettings, binding.navSettingsIcon, binding.navSettingsLabel)
        )

        tabs.forEach { (tab, views) ->
            val (container, icon, label) = views
            val active = tab == current
            container.setBackgroundResource(if (active) R.drawable.bg_nav_active_pill else 0)
            label.visibility = if (active) View.VISIBLE else View.GONE
            label.setTextColor(if (active) activeColor else inactiveColor)
            ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(if (active) activeColor else inactiveColor))
        }
    }
}
