package com.readerlauncher.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.readerlauncher.app.apps.AppRepository
import com.readerlauncher.app.apps.FavoriteAppsPickerActivity
import com.readerlauncher.app.data.LibraryStore
import com.readerlauncher.app.databinding.ActivityHomeBinding
import com.readerlauncher.app.databinding.ItemFavoriteAppBinding
import com.readerlauncher.app.onboarding.OnboardingActivity
import com.readerlauncher.app.reader.ReaderActivity
import com.readerlauncher.app.ui.BottomNavHelper
import com.readerlauncher.app.ui.NavTab
import com.readerlauncher.app.ui.XpWidgetHelper
import com.readerlauncher.app.util.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var appRepository: AppRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Prefs.isOnboardingComplete(this)) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appRepository = AppRepository(this)

        BottomNavHelper.setup(binding.bottomNav, this, NavTab.HOME)
        XpWidgetHelper.setup(binding.xpWidget, this)

        binding.configureAppsButton.setOnClickListener {
            startActivity(Intent(this, FavoriteAppsPickerActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadCurrentBook()
        loadFavorites()
        XpWidgetHelper.refresh(binding.xpWidget, this)
    }

    private fun loadCurrentBook() = lifecycleScope.launch {
        val book = withContext(Dispatchers.IO) { LibraryStore.loadBooks(applicationContext).firstOrNull() }
        if (book == null) {
            binding.continueReadingCard.visibility = View.GONE
        } else {
            binding.continueReadingCard.visibility = View.VISIBLE
            val percent = if (book.totalPages > 0) (book.lastPage * 100) / book.totalPages else 0
            binding.currentBookTitle.text = book.title
            binding.currentBookProgress.text = getString(R.string.progress_percent, percent)
            binding.continueReadingRow.setOnClickListener {
                startActivity(Intent(this@MainActivity, ReaderActivity::class.java).putExtra(ReaderActivity.EXTRA_BOOK_ID, book.id))
            }
        }
    }

    private fun loadFavorites() {
        val favoritePackages = Prefs.getFavoriteApps(this)
        binding.favoritesRow.removeAllViews()

        if (favoritePackages.isEmpty()) {
            binding.favoritesEmptyState.visibility = View.VISIBLE
            binding.favoritesRow.visibility = View.GONE
            return
        }

        lifecycleScope.launch {
            val allApps = withContext(Dispatchers.IO) { appRepository.getAllApps(includeSystemApps = true) }
            val byPackage = allApps.associateBy { it.packageName }

            binding.favoritesRow.removeAllViews()
            var addedAny = false
            favoritePackages.forEach { pkg ->
                val app = byPackage[pkg] ?: return@forEach
                addedAny = true
                val itemBinding = ItemFavoriteAppBinding.inflate(LayoutInflater.from(this@MainActivity), binding.favoritesRow, false)
                itemBinding.favoriteIcon.setImageDrawable(app.icon)
                itemBinding.favoriteLabel.text = app.label
                itemBinding.root.setOnClickListener { appRepository.launchOrOpenSettings(this@MainActivity, app.packageName) }
                binding.favoritesRow.addView(itemBinding.root)
            }

            binding.favoritesEmptyState.visibility = if (addedAny) View.GONE else View.VISIBLE
            binding.favoritesRow.visibility = if (addedAny) View.VISIBLE else View.GONE
        }
    }
}
