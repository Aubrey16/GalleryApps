package com.example.galleryapps.data

import android.net.Uri

data class Bucket(
    val id : Long,
    val name : String,
    val imageCount : Int,
    val coverUri : Uri
)
