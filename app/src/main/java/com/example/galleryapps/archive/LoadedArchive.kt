package com.example.galleryapps.archive

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.number.IntegerWidth
import coil.size.Size
import com.github.junrar.Archive
import com.github.junrar.BrokenHeader
import com.github.junrar.rarfile.FileHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class LoadedArchive private constructor(
    val type: ArchiveType,
    val file: File,
    val entries: List<ArchiveEntry>,
    private val zipFile: ZipFile?,
    private val zipEntries: List<ZipEntry>?,
    private val rarArchive: Archive?,
    private val rarHeader: List<FileHeader>?
) : Closeable {

    private val mutex = Mutex()

    private suspend fun readBytes(index: Int): ByteArray? = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                val stream: InputStream? = when (type) {
                    ArchiveType.ZIP -> zipEntries?.getOrNull(index)
                        ?.let { zipFile?.getInputStream(it) }

                    ArchiveType.RAR -> rarHeader?.getOrNull(index)
                        ?.let { rarArchive?.getInputStream(it) }

                    ArchiveType.UNKNOWN -> null
                }
                stream?.use { it.readBytes() }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun decode(index: Int, reqSize: Int): Bitmap? {
        val bytes = readBytes(index) ?: return null
        return withContext(Dispatchers.Default) {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)

            val options = BitmapFactory.Options().apply {
                inSampleSize = calcInSampleSize(bounds.outWidth, bounds.outHeight, reqSize)
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        }
    }

    override fun close() {
        runCatching { zipFile?.close() }
        runCatching { rarArchive?.close() }
    }

    companion object {

        fun open(file: File): LoadedArchive {
            return when (detectType(file)) {
                ArchiveType.ZIP -> {
                    val zipFile = ZipFile(file)
                    val zipEntries = zipFile.entries().toList()
                        .filter { !it.isDirectory && isImage(it.name) }
                        .sortedBy { it.name.lowercase() }
                    val entries = zipEntries.mapIndexed { i, e ->
                        ArchiveEntry(i, e.name.substringAfterLast('/'), e.size)
                    }
                    LoadedArchive(ArchiveType.ZIP, file, entries, zipFile, zipEntries, null, null)
                }

                ArchiveType.RAR -> {
                    val archive = Archive(file)
                    val headers = archive.fileHeaders
                        .filter { !it.isDirectory && isImage(it.fileName) }
                        .sortedBy { it.fileName.lowercase() }
                    val entries = headers.mapIndexed { i, h ->
                        ArchiveEntry(i, h.fileName.substringAfterLast('/'), h.fullUnpackSize)
                    }
                    LoadedArchive(ArchiveType.RAR, file, entries, null, null, archive, headers)
                }

                ArchiveType.UNKNOWN ->
                    LoadedArchive(ArchiveType.UNKNOWN, file, emptyList(), null, null, null, null)
            }
        }

        private fun detectType(file: File): ArchiveType {
            val header = ByteArray(8)
            val read = file.inputStream().use { it.read(header) }
            if (read >= 4 &&
                header[0] == 0x50.toByte() && header[1] == 0x4B.toByte()
            ) return ArchiveType.ZIP
            if (read >= 4 &&
                header[0] == 0x52.toByte() && header[1] == 0x61.toByte() &&
                header[2] == 0x72.toByte() && header[3] == 0x21.toByte()
            ) return ArchiveType.RAR
            return ArchiveType.UNKNOWN
        }

        private fun isImage(name: String): Boolean {
            val n = name.lowercase()
            return n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png") ||
                    n.endsWith(".gif") || n.endsWith(".webp") || n.endsWith(".bmp")
        }

        private fun calcInSampleSize(width: Int, height: Int, reqSize: Int): Int {
            var sample = 1
            if (width <= 0 || height <= 0) return sample
            while (width / (sample * 2) >= reqSize && height / (sample * 2) >= reqSize) {
                sample *= 2
            }
            return sample
        }
    }
}