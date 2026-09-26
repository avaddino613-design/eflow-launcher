package com.readerlauncher.app.settings

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.readerlauncher.app.databinding.ActivitySettingsBinding
import com.readerlauncher.app.stats.StatsActivity
import com.readerlauncher.app.ui.BottomNavHelper
import com.readerlauncher.app.ui.NavTab
import com.readerlauncher.app.ui.XpWidgetHelper
import com.readerlauncher.app.util.PermissionUtils
import com.readerlauncher.app.util.Prefs

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        BottomNavHelper.setup(binding.bottomNav, this, NavTab.SETTINGS)
        XpWidgetHelper.setup(binding.xpWidget, this)

        val savedPref = Prefs.getDarkModePreference(this)
        binding.darkModeSwitch.isChecked = savedPref ?: isSystemInDarkTheme()
        binding.darkModeSwitch.setOnCheckedChangeListener { _, checked ->
            Prefs.setDarkModePreference(this, checked)
            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
            recreate()
        }

        binding.analyticsRow.setOnClickListener { startActivity(Intent(this, StatsActivity::class.java)) }
        binding.defaultLauncherRow.setOnClickListener { PermissionUtils.requestDefaultLauncher(this) }
    }

    override fun onResume() {
        super.onResume()
        binding.defaultLauncherStatus.text = getString(
            if (PermissionUtils.isDefaultLauncher(this))
                com.readerlauncher.app.R.string.status_enabled
            else
                com.readerlauncher.app.R.string.status_not_enabled
        )
    }

    private fun isSystemInDarkTheme(): Boolean {
        val uiMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return uiMode == Configuration.UI_MODE_NIGHT_YES
    }
}
