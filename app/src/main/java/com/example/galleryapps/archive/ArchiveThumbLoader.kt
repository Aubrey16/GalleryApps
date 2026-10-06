package com.example.galleryapps.archive

import android.graphics.Bitmap
import android.util.LruCache

class ArchiveThumbLoader(
    private val archive: LoadedArchive,
    cacheSizeKb: Int = 16 * 1024
) {
    private val cache = object : LruCache<Int, Bitmap>(cacheSizeKb) {
        override fun sizeOf(key: Int, value: Bitmap): Int = value.byteCount / 1024
    }

    suspend fun load(index: Int, reqSize: Int): Bitmap? {
        cache.get(index)?.let { return it }
        val bitmap = archive.decode(index, reqSize) ?: return null
        cache.put(index, bitmap)
        return bitmap
    }

    fun clear() = cache.evictAll()
}