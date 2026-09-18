package com.example.galleryapps.data

import android.net.Uri


data class GalleryImage(
    val id : Long,
    val uri : Uri,
    val displayName: String,
    val dateAddedMillis : Long,
    val size : Long,
    val width : Int,
    val height : Int,
    val bucketId : Long,
    val bucketName : String,
    val mimeType : String
)