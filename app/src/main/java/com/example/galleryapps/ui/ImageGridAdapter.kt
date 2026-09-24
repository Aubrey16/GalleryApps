package com.example.galleryapps.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.galleryapps.data.GalleryImage
import com.example.galleryapps.databinding.ItemImageBinding

class ImageGridAdapter(
    private val onClick: (GalleryImage) -> Unit
) : ListAdapter<GalleryImage, ImageGridAdapter.ImageViewHolder>(DIFF) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ImageViewHolder {
        val binding = ItemImageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ImageViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


class ImageViewHolder(
    private val binding: ItemImageBinding,
    private val onClick: (GalleryImage) -> Unit
) : RecyclerView.ViewHolder(binding.root){
    fun bind(item: GalleryImage){
        binding.textView.text = item.displayName
        binding.imageView.load(item.uri){
            crossfade(true)
        }
        binding.root.setOnClickListener { onClick(item) }
    }
}

companion object {
    private val DIFF = object : DiffUtil.ItemCallback<GalleryImage>(){
        override fun areItemsTheSame(oldItem: GalleryImage, newItem: GalleryImage)=
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: GalleryImage, newItem: GalleryImage)=
            oldItem == newItem
        }
    }
}