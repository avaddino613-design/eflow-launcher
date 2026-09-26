package com.readerlauncher.app.library

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.readerlauncher.app.data.Book
import com.readerlauncher.app.databinding.ItemBookBinding

class BookAdapter(private val onClick: (Book) -> Unit) :
    ListAdapter<Book, BookAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(val binding: ItemBookBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = getItem(position)
        holder.binding.bookTitle.text = book.title
        val percent = if (book.totalPages > 0) (book.lastPage * 100) / book.totalPages else 0
        holder.binding.bookMeta.text = "PDF · $percent% read"
        holder.itemView.setOnClickListener { onClick(book) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Book>() {
            override fun areItemsTheSame(oldItem: Book, newItem: Book) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Book, newItem: Book) = oldItem == newItem
        }
    }
}
