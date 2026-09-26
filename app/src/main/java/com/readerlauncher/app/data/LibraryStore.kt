package com.readerlauncher.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * All persistence for the app, in one small JSON file under the app's
 * private storage. Deliberately not Room: Room needs an annotation
 * processor (KSP or kapt) whose version has to be kept in exact lockstep
 * with the Kotlin compiler version, which is what broke two builds in a
 * row. A library this small doesn't need a real database - plain
 * org.json (built into the Android platform, no extra dependency at all)
 * is enough, and removes that entire category of build failure.
 *
 * All methods here do blocking file I/O and must be called from a
 * background thread (e.g. Dispatchers.IO), which every call site in this
 * app already does.
 */
object LibraryStore {

    private const val FILE_NAME = "library.json"

    @Synchronized
    fun loadBooks(context: Context): List<Book> {
        val array = readRoot(context).optJSONArray("books") ?: JSONArray()
        val books = mutableListOf<Book>()
        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            books.add(
                Book(
                    id = o.getLong("id"),
                    title = o.getString("title"),
                    uri = o.getString("uri"),
                    lastPage = o.optInt("lastPage", 0),
                    totalPages = o.optInt("totalPages", 0),
                    dateAdded = o.optLong("dateAdded", System.currentTimeMillis())
                )
            )
        }
        return books.sortedByDescending { it.dateAdded }
    }

    @Synchronized
    fun getBook(context: Context, bookId: Long): Book? =
        loadBooks(context).firstOrNull { it.id == bookId }

    @Synchronized
    fun addBook(context: Context, book: Book) {
        saveBooks(context, listOf(book) + loadBooks(context))
    }

    @Synchronized
    fun updateProgress(context: Context, bookId: Long, page: Int, totalPages: Int) {
        val updated = loadBooks(context).map {
            if (it.id == bookId) it.copy(lastPage = page, totalPages = totalPages) else it
        }
        saveBooks(context, updated)
    }

    @Synchronized
    fun addSession(context: Context, startMillis: Long, endMillis: Long) {
        val root = readRoot(context)
        val array = root.optJSONArray("sessions") ?: JSONArray()
        array.put(JSONObject().apply {
            put("start", startMillis)
            put("end", endMillis)
        })
        root.put("sessions", array)
        writeRoot(context, root)
    }

    @Synchronized
    fun loadSessions(context: Context): List<ReadingSession> {
        val array = readRoot(context).optJSONArray("sessions") ?: JSONArray()
        val sessions = mutableListOf<ReadingSession>()
        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            sessions.add(ReadingSession(o.getLong("start"), o.getLong("end")))
        }
        return sessions
    }

    private fun saveBooks(context: Context, books: List<Book>) {
        val root = readRoot(context)
        val array = JSONArray()
        books.forEach { book ->
            array.put(JSONObject().apply {
                put("id", book.id)
                put("title", book.title)
                put("uri", book.uri)
                put("lastPage", book.lastPage)
                put("totalPages", book.totalPages)
                put("dateAdded", book.dateAdded)
            })
        }
        root.put("books", array)
        writeRoot(context, root)
    }

    private fun file(context: Context) = File(context.filesDir, FILE_NAME)

    private fun readRoot(context: Context): JSONObject {
        val f = file(context)
        if (!f.exists()) return JSONObject()
        return try {
            JSONObject(f.readText())
        } catch (e: Exception) {
            JSONObject()
        }
    }

    private fun writeRoot(context: Context, root: JSONObject) {
        file(context).writeText(root.toString())
    }
}
