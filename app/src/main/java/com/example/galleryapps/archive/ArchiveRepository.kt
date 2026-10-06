package com.example.galleryapps.archive

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ArchiveRepository(private val context: Context) {

    suspend fun import(uri: Uri): LoadedArchive? = withContext(Dispatchers.IO) {
        val cacheFile = copyToCache(uri) ?: return@withContext null
        try {
            LoadedArchive.open(cacheFile)
        } catch (e: Exception) {
            runCatching { cacheFile.delete() }
            null
        }
    }

    private fun copyToCache(uri: Uri): File? {
        return try {
            val name = queryDisplayName(uri) ?: "archive_${System.currentTimeMillis()}"
            val dir = File(context.cacheDir, "archives").apply { mkdirs() }
            val outFile = File(dir, name)
            context.contentResolver.openInputStream(uri)?.use { input ->
                outFile.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            outFile
        } catch (e: Exception) {
            null
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        return context.contentResolver.query(
            uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }

    fun deleteCache(file: File) {
        runCatching { file.delete() }
    }
}