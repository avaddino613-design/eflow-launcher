package com.readerlauncher.app.apps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.readerlauncher.app.R
import com.readerlauncher.app.databinding.ActivityFavoriteAppsPickerBinding
import com.readerlauncher.app.util.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoriteAppsPickerActivity : AppCompatActivity() {

    companion object {
        const val MAX_FAVORITES = 5
    }

    private lateinit var binding: ActivityFavoriteAppsPickerBinding
    private lateinit var repository: AppRepository
    private lateinit var adapter: FavoriteAppsAdapter
    private var allApps: List<AppInfo> = emptyList()
    private val selected = linkedSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteAppsPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = AppRepository(this)
        selected.addAll(Prefs.getFavoriteApps(this))

        adapter = FavoriteAppsAdapter(selected) { app ->
            if (selected.contains(app.packageName)) {
                selected.remove(app.packageName)
            } else if (selected.size < MAX_FAVORITES) {
                selected.add(app.packageName)
            }
            updateCountLabel()
            adapter.notifyDataSetChanged()
        }
        binding.pickerList.layoutManager = LinearLayoutManager(this)
        binding.pickerList.adapter = adapter

        binding.searchInput.doAfterTextChanged { filter(it?.toString().orEmpty()) }
        binding.doneButton.setOnClickListener {
            Prefs.setFavoriteApps(this, selected.toList())
            finish()
        }

        lifecycleScope.launch {
            allApps = withContext(Dispatchers.IO) { repository.getAllApps(includeSystemApps = false) }
            adapter.submitList(allApps)
            updateCountLabel()
        }
    }

    private fun filter(query: String) {
        val filtered = if (query.isBlank()) allApps else allApps.filter { it.label.contains(query, ignoreCase = true) }
        adapter.submitList(filtered)
    }

    private fun updateCountLabel() {
        binding.selectionCount.text = getString(R.string.favorites_selected_count, selected.size, MAX_FAVORITES)
    }
}
