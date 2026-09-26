package com.readerlauncher.app.stats

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.readerlauncher.app.databinding.ActivityStatsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }

        lifecycleScope.launch {
            val stats = withContext(Dispatchers.IO) { StatsManager.computeStats(applicationContext) }
            binding.streakValue.text = stats.currentStreakDays.toString()
            binding.minutesValue.text = stats.totalMinutesLast7Days.toString()
            binding.xpValue.text = stats.xp.toString()
        }
    }
}
