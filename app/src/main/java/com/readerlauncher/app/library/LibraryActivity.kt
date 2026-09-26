package com.readerlauncher.app.library

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.readerlauncher.app.R
import com.readerlauncher.app.data.Book
import com.readerlauncher.app.data.LibraryStore
import com.readerlauncher.app.databinding.ActivityLibraryBinding
import com.readerlauncher.app.reader.ReaderActivity
import com.readerlauncher.app.ui.BottomNavHelper
import com.readerlauncher.app.ui.NavTab
import com.readerlauncher.app.ui.XpWidgetHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class LibraryFilter { ALL, READING, COMPLETED }

class LibraryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLibraryBinding
    private lateinit var adapter: BookAdapter
    private var allBooks: List<Book> = emptyList()
    private var activeFilter = LibraryFilter.ALL

    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { importBook(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLibraryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        BottomNavHelper.setup(binding.bottomNav, this, NavTab.LIBRARY)
        XpWidgetHelper.setup(binding.xpWidget, this)

        adapter = BookAdapter { book ->
            startActivity(Intent(this, ReaderActivity::class.java).putExtra(ReaderActivity.EXTRA_BOOK_ID, book.id))
        }
        binding.libraryList.layoutManager = LinearLayoutManager(this)
        binding.libraryList.adapter = adapter

        binding.importBookButton.setOnClickListener {
            importLauncher.launch(arrayOf("application/pdf"))
        }
        binding.searchInput.doAfterTextChanged { applyFilters() }

        binding.filterAll.setOnClickListener { setFilter(LibraryFilter.ALL) }
        binding.filterReading.setOnClickListener { setFilter(LibraryFilter.READING) }
        binding.filterCompleted.setOnClickListener { setFilter(LibraryFilter.COMPLETED) }
    }

    override fun onResume() {
        super.onResume()
        loadBooks()
        XpWidgetHelper.refresh(binding.xpWidget, this)
    }

    private fun loadBooks() {
        lifecycleScope.launch {
            allBooks = withContext(Dispatchers.IO) { LibraryStore.loadBooks(applicationContext) }
            applyFilters()
        }
    }

    private fun setFilter(filter: LibraryFilter) {
        activeFilter = filter
        val chips = listOf(
            LibraryFilter.ALL to binding.filterAll,
            LibraryFilter.READING to binding.filterReading,
            LibraryFilter.COMPLETED to binding.filterCompleted
        )
        chips.forEach { (f, chip) ->
            val active = f == filter
            chip.setBackgroundResource(if (active) R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected)
            chip.setTextColor(getColor(if (active) R.color.chip_fg_active else R.color.chip_fg_inactive))
        }
        applyFilters()
    }

    private fun applyFilters() {
        val query = binding.searchInput.text?.toString().orEmpty()
        var filtered = allBooks.filter { it.title.contains(query, ignoreCase = true) }
        filtered = when (activeFilter) {
            LibraryFilter.ALL -> filtered
            LibraryFilter.READING -> filtered.filter { it.totalPages > 0 && it.lastPage in 1 until it.totalPages }
            LibraryFilter.COMPLETED -> filtered.filter { it.totalPages > 0 && it.lastPage >= it.totalPages }
        }
        adapter.submitList(filtered)
        binding.emptyStateText.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun importBook(uri: Uri) {
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val displayName = queryDisplayName(uri) ?: "Untitled"

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                LibraryStore.addBook(
                    applicationContext,
                    Book(id = System.currentTimeMillis(), title = displayName, uri = uri.toString())
                )
            }
            loadBooks()
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) return cursor.getString(nameIndex)
        }
        return null
    }
}
