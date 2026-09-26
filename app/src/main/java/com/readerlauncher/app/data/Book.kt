package com.readerlauncher.app.data

data class Book(
    val id: Long,
    val title: String,
    val uri: String,           // persisted content:// URI (SAF)
    val lastPage: Int = 0,
    val totalPages: Int = 0,
    val dateAdded: Long = System.currentTimeMillis()
)
