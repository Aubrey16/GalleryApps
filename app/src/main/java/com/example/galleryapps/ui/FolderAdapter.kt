package com.example.galleryapps.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.R
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.galleryapps.data.Bucket
import com.example.galleryapps.databinding.ActivityMainBinding
import com.example.galleryapps.databinding.ItemFolderBinding

class FolderAdapter(
    private val onClick: (Bucket) -> Unit
) : ListAdapter<Bucket, FolderAdapter.FolderViewHolder>(DIFF) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val binding = ItemFolderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FolderViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    class FolderViewHolder(
        private val binding: ItemFolderBinding,
        private val onClick: (Bucket) -> Unit
    ) : RecyclerView.ViewHolder(binding.root){
        fun bind(item: Bucket){
            binding.textName.text = item.name
            binding.textCount.text = binding.root.context.getString(com.example.galleryapps.R.string.photos_count, item.imageCount)
            binding.imageCover.load(item.coverUri){crossfade(true)}
            binding.root.setOnClickListener { onClick(item) }
        }
    }


    companion object{
        private val DIFF = object : DiffUtil.ItemCallback<Bucket>(){
            override fun areItemsTheSame(oldItem: Bucket, newItem: Bucket) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Bucket, newItem: Bucket) =
                oldItem == newItem


        }
    }
}
