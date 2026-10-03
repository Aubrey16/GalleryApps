package com.example.galleryapps.ui

import android.app.Application
import android.os.Message
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.galleryapps.data.Bucket
import com.example.galleryapps.data.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface FolderUiState{
    object Loading : FolderUiState
    object Empty : FolderUiState
    data class Success(val buckets: List<Bucket>): FolderUiState
    data class Error(val message: String) : FolderUiState
}

class FolderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MediaRepository(application)
    private val _uiState = MutableStateFlow<FolderUiState>(FolderUiState.Loading)
    val uiState: StateFlow<FolderUiState> = _uiState.asStateFlow()

    fun loadBuckets(){
        viewModelScope.launch {
            _uiState.value = FolderUiState.Loading
            try {
                val buckets = repository.loadBuckets()
                _uiState.value = if (buckets.isEmpty()){
                    FolderUiState.Empty
                } else {
                    FolderUiState.Success(buckets)
                }
            } catch (e: Exception){
                _uiState.value = FolderUiState.Error(e.message ?: "ada kesalahan GNG")
            }
        }
    }
}