package com.example.galleryapps.archive

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.galleryapps.databinding.ItemImageBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ArchiveEntryAdapter(
    private val scope: CoroutineScope,
    private val loader: ArchiveThumbLoader,
    private val onClick: (ArchiveEntry) -> Unit
) : ListAdapter<ArchiveEntry, ArchiveEntryAdapter.EntryViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val binding = ItemImageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EntryViewHolder(binding, scope, loader, onClick)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: EntryViewHolder) {
        super.onViewRecycled(holder)
        holder.cancel()
    }

    class EntryViewHolder(
        private val binding: ItemImageBinding,
        private val scope: CoroutineScope,
        private val loader: ArchiveThumbLoader,
        private val onClick: (ArchiveEntry) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var job: Job? = null

        fun bind(entry: ArchiveEntry) {
            binding.textView.text = entry.name
            binding.imageView.setImageBitmap(null)

            job?.cancel()
            job = scope.launch {
                val target = binding.imageView.width.takeIf { it > 0 } ?: 256
                val bitmap = loader.load(entry.index, target)
                if (bitmap != null) {
                    binding.imageView.setImageBitmap(bitmap)
                }
            }
            binding.root.setOnClickListener { onClick(entry) }
        }

        fun cancel() {
            job?.cancel()
            job = null
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ArchiveEntry>() {
            override fun areItemsTheSame(oldItem: ArchiveEntry, newItem: ArchiveEntry) =
                oldItem.index == newItem.index

            override fun areContentsTheSame(oldItem: ArchiveEntry, newItem: ArchiveEntry) =
                oldItem == newItem
        }
    }
}