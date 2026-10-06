package com.example.galleryapps.archive

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ArchiveUiState {
    object Loading : ArchiveUiState
    object Empty : ArchiveUiState
    data class Success(val archive: LoadedArchive, val entries: List<ArchiveEntry>) : ArchiveUiState
    data class Error(val message: String) : ArchiveUiState
}

class ArchiveViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ArchiveRepository(application)

    private val _uiState = MutableStateFlow<ArchiveUiState>(ArchiveUiState.Loading)
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()

    fun open(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = ArchiveUiState.Loading
            val archive = repository.import(uri)
            _uiState.value = when {
                archive == null -> ArchiveUiState.Error("Gagal membuka file")
                archive.type == ArchiveType.UNKNOWN -> {
                    repository.deleteCache(archive.file)
                    ArchiveUiState.Error("Format tidak didukung (hanya ZIP/RAR)")
                }
                archive.entries.isEmpty() -> ArchiveUiState.Empty
                else -> ArchiveUiState.Success(archive, archive.entries)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        val state = _uiState.value
        if (state is ArchiveUiState.Success) {
            state.archive.close()
            repository.deleteCache(state.archive.file)
        }
    }
}