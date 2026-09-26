package com.readerlauncher.app.apps

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.readerlauncher.app.databinding.ItemAppBinding

class AppListAdapter(private val onClick: (AppInfo) -> Unit) :
    ListAdapter<AppInfo, AppListAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = getItem(position)
        holder.binding.appIcon.setImageDrawable(app.icon)
        holder.binding.appLabel.text = app.label
        holder.binding.appMeta.text = if (app.isSystemApp) "${app.packageName} · system" else app.packageName
        holder.itemView.setOnClickListener { onClick(app) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<AppInfo>() {
            override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo) =
                oldItem.packageName == newItem.packageName
            override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo) =
                oldItem.label == newItem.label && oldItem.isSystemApp == newItem.isSystemApp
        }
    }
}
