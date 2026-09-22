package com.example.galleryapps.ui

import android.app.Application
import android.os.Message
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.galleryapps.data.GalleryImage
import com.example.galleryapps.data.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface GalleryUiState{
    object Loading : GalleryUiState
    object Empty : GalleryUiState
    data class Success(val images: List<GalleryImage>) : GalleryUiState
    data class Error(val message: String) : GalleryUiState
}

class GalleryViewModel(application: Application) : AndroidViewModel(application){

    private val repository = MediaRepository(application)

    private val _uiState = MutableStateFlow<GalleryUiState>(GalleryUiState.Loading)
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    fun loadImages() {
        viewModelScope.launch {
            _uiState.value = GalleryUiState.Loading
            try {
                val images = repository.loadImages()
                _uiState.value = if (images.isEmpty()) {
                    GalleryUiState.Empty
                } else{
                    GalleryUiState.Success(images)
                }
            }catch (e: Exception) {
                _uiState.value = GalleryUiState.Error(e.message ?: "Ada kesalahan")
            }
        }
    }
}