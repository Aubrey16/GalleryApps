package com.example.galleryapps.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaRepository(private val context: Context) {
    suspend fun loadImages(): List<GalleryImage> = withContext(Dispatchers.IO){
        val images = mutableListOf<GalleryImage>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.MIME_TYPE
        )

        val selection = "${MediaStore.Images.Media.SIZE} > 0"
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val bucketIdCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val bucketNameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                images += GalleryImage(
                    id = id,
                    uri = uri,
                    displayName = cursor.getString(nameCol) ?: "unknown",
                    dateAddedMillis = cursor.getLong(dateCol) * 1000L,
                    size = cursor.getLong(sizeCol),
                    width = cursor.getInt(widthCol),
                    height = cursor.getInt(heightCol),
                    bucketId = cursor.getLong(bucketIdCol),
                    bucketName = cursor.getString(bucketNameCol) ?: "Lainnya",
                    mimeType = cursor.getString(mimeCol) ?: "image/*"
                )
            }
        }
        images
    }

    suspend fun loadBuckets(): List<Bucket>{
        val images = loadImages()
        return images
            .groupBy { it.bucketId }
            .map { (bucketId, items) ->
                val newest = items.first()
                Bucket(
                    id = bucketId,
                    name = newest.bucketName,
                    imageCount = items.size,
                    coverUri = newest.uri
                )
            }
            .sortedByDescending { it.imageCount }
    }
}
