package com.readerlauncher.app.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.readerlauncher.app.databinding.XpWidgetBinding
import com.readerlauncher.app.stats.StatsActivity
import com.readerlauncher.app.stats.StatsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Wires up the shared xp_widget.xml include: tap-to-open-stats + refreshing its numbers. */
object XpWidgetHelper {

    fun setup(binding: XpWidgetBinding, activity: AppCompatActivity) {
        binding.xpWidgetCard.setOnClickListener {
            activity.startActivity(Intent(activity, StatsActivity::class.java))
        }
        refresh(binding, activity)
    }

    fun refresh(binding: XpWidgetBinding, activity: AppCompatActivity) {
        activity.lifecycleScope.launch {
            val stats = withContext(Dispatchers.IO) { StatsManager.computeStats(activity.applicationContext) }
            val level = (stats.xp / 100) + 1
            val xpIntoLevel = (stats.xp % 100).toInt()
            binding.xpLevelText.text = "Level $level Reader"
            binding.xpProgressBar.progress = xpIntoLevel
            binding.xpValueText.text = "$xpIntoLevel/100"
        }
    }
}
