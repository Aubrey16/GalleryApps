package com.example.galleryapps.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.galleryapps.databinding.ActivityMainBinding
import com.example.galleryapps.databinding.ItemViewerImageBinding

class ViewerPagerAdapter(private val uris: List<Uri>): RecyclerView.Adapter<ViewerPagerAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewerPagerAdapter.ViewHolder {
        val binding = ItemViewerImageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = uris.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(uris[position])
    }

    class ViewHolder(
        private val binding: ItemViewerImageBinding
    ) : RecyclerView.ViewHolder(binding.root){
        fun bind(uri: Uri) {
            binding.imageView.load(uri)
        }
    }
}