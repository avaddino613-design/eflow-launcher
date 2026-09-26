package com.readerlauncher.app.reader

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.readerlauncher.app.data.LibraryStore
import com.readerlauncher.app.databinding.ActivityReaderBinding
import com.readerlauncher.app.util.AppScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReaderActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BOOK_ID = "book_id"
    }

    private lateinit var binding: ActivityReaderBinding
    private var bookId: Long = -1
    private var sessionStart = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookId = intent.getLongExtra(EXTRA_BOOK_ID, -1)
        if (bookId == -1L) {
            finish()
            return
        }
        sessionStart = System.currentTimeMillis()
        loadBook()
    }

    private fun loadBook() = lifecycleScope.launch {
        val book = withContext(Dispatchers.IO) { LibraryStore.getBook(applicationContext, bookId) }
            ?: return@launch finish()
        title = book.title
        val uri = Uri.parse(book.uri)
        val pfd = contentResolver.openFileDescriptor(uri, "r") ?: return@launch finish()

        val fragment = PdfReaderFragment()
        fragment.setOnPageChangedListener { page, total ->
            AppScope.io.launch { LibraryStore.updateProgress(applicationContext, bookId, page, total) }
        }
        supportFragmentManager.beginTransaction()
            .replace(binding.readerContainer.id, fragment)
            .commitNow()
        fragment.open(pfd, book.lastPage)
    }

    override fun onDestroy() {
        super.onDestroy()
        val sessionEnd = System.currentTimeMillis()
        val start = sessionStart
        // App-lifetime scope, not lifecycleScope: the Activity's lifecycle
        // is already ending here, so lifecycleScope would be cancelled
        // before this write completes.
        if (sessionEnd - start > 3000) {
            AppScope.io.launch { LibraryStore.addSession(applicationContext, start, sessionEnd) }
        }
    }
}
