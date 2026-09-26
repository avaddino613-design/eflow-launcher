package com.readerlauncher.app.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * A scope that outlives any single Activity/Fragment lifecycle. Use this
 * (instead of lifecycleScope) for writes that must complete even as the
 * screen that triggered them is being destroyed - e.g. saving a reading
 * session when the reader closes.
 */
object AppScope {
    val io: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}
