package com.example.galleryapps.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.galleryapps.data.GalleryImage
import com.example.galleryapps.data.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BucketViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MediaRepository(application)
    private val _images = MutableStateFlow<List<GalleryImage>>(emptyList())
    val images: StateFlow<List<GalleryImage>> = _images.asStateFlow()
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun loadImages(bucketId: Long){
        viewModelScope.launch {
            _loading.value = true
            try {
                _images.value = repository.loadImagesInBucket(bucketId)
            } catch (e : Exception){
                _images.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }
}