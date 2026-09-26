package com.readerlauncher.app.stats

import android.content.Context
import com.readerlauncher.app.data.LibraryStore

data class ReadingStats(
    val totalMinutesLast7Days: Long,
    val currentStreakDays: Int,
    val xp: Long
)

object StatsManager {

    fun computeStats(context: Context): ReadingStats {
        val sessions = LibraryStore.loadSessions(context)

        val sevenDaysAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        val minutes = sessions.filter { it.startMillis >= sevenDaysAgo }
            .sumOf { it.endMillis - it.startMillis } / 60000

        val dayBuckets = sessions.map { it.startMillis / 86400000 }.toSet()
        val todayBucket = System.currentTimeMillis() / 86400000
        var streak = 0
        var cursor = todayBucket
        while (dayBuckets.contains(cursor)) {
            streak++
            cursor--
        }

        // Simple, transparent XP formula: 1 XP per minute read this week,
        // plus a 10 XP bonus per day of the current streak.
        val xp = minutes + (streak * 10)

        return ReadingStats(minutes, streak, xp)
    }
}
