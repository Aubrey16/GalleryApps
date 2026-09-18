package com.example.galleryapps

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.galleryapps.data.MediaRepository
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        lifecycleScope.launch {
            val repo = MediaRepository(applicationContext)
            val images = repo.loadImages()
            val buckets = repo.loadBuckets()
            Log.d("GalleryApps", "Jumlah gambar : ${images.size}")
            Log.d("GalleryApps", "Jumlah Folder : ${buckets.size}")
            images.take(5).forEach { Log.d("GalleryApps", it.displayName) }
        }
        }
    }