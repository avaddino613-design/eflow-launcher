package com.readerlauncher.app.apps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.readerlauncher.app.databinding.ActivityAppDrawerBinding
import com.readerlauncher.app.ui.BottomNavHelper
import com.readerlauncher.app.ui.NavTab
import com.readerlauncher.app.ui.XpWidgetHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppDrawerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppDrawerBinding
    private lateinit var repository: AppRepository
    private lateinit var adapter: AppListAdapter
    private var allApps: List<AppInfo> = emptyList()
    private var showSystemApps = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        BottomNavHelper.setup(binding.bottomNav, this, NavTab.APPS)
        XpWidgetHelper.setup(binding.xpWidget, this)

        repository = AppRepository(this)
        adapter = AppListAdapter { app -> repository.launchOrOpenSettings(this, app.packageName) }

        binding.appList.layoutManager = LinearLayoutManager(this)
        binding.appList.adapter = adapter

        binding.showSystemAppsSwitch.setOnCheckedChangeListener { _, checked ->
            showSystemApps = checked
            loadApps()
        }
        binding.searchInput.doAfterTextChanged { filterApps(it?.toString().orEmpty()) }

        loadApps()
    }

    override fun onResume() {
        super.onResume()
        XpWidgetHelper.refresh(binding.xpWidget, this)
    }

    private fun loadApps() {
        lifecycleScope.launch {
            allApps = withContext(Dispatchers.IO) { repository.getAllApps(showSystemApps) }
            filterApps(binding.searchInput.text?.toString().orEmpty())
        }
    }

    private fun filterApps(query: String) {
        val filtered = if (query.isBlank()) allApps
        else allApps.filter { it.label.contains(query, ignoreCase = true) }
        adapter.submitList(filtered)
    }
}
