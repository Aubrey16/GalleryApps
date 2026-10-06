package com.example.galleryapps.archive

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.galleryapps.databinding.ItemViewerImageBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ArchivePagerAdapter(
    private val scope: CoroutineScope,
    private val archive: LoadedArchive,
    private val entries: List<ArchiveEntry>,
    private val targetSize: Int
) : RecyclerView.Adapter<ArchivePagerAdapter.PageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val binding = ItemViewerImageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PageViewHolder(binding, scope, archive, entries, targetSize)
    }

    override fun getItemCount(): Int = entries.size

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.bind(entries[position])
    }

    override fun onViewRecycled(holder: PageViewHolder) {
        super.onViewRecycled(holder)
        holder.cancel()
    }

    class PageViewHolder(
        private val binding: ItemViewerImageBinding,
        private val scope: CoroutineScope,
        private val archive: LoadedArchive,
        private val entries: List<ArchiveEntry>,
        private val targetSize: Int
    ) : RecyclerView.ViewHolder(binding.root) {

        private var job: Job? = null

        fun bind(entry: ArchiveEntry) {
            binding.imageView.setImageBitmap(null)
            job?.cancel()
            job = scope.launch {
                val bitmap = archive.decode(entry.index, targetSize)
                binding.imageView.setImageBitmap(bitmap)
            }
        }

        fun cancel() {
            job?.cancel()
            job = null
        }
    }
}