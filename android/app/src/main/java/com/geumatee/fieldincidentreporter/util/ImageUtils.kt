package com.geumatee.fieldincidentreporter.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream

object ImageUtils {
    private const val MAX_CACHE_ENTRIES = 32
    private val bitmapCache =
        object : LinkedHashMap<String, Bitmap>(MAX_CACHE_ENTRIES, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Bitmap>?): Boolean {
                return size > MAX_CACHE_ENTRIES
            }
        }

    // Downscales to 480px JPEG (~20-40 KB) and encodes as a Data URI instead of multipart/S3 upload.
    // This allows mock json-server to persist images directly in JSON and both Android and Web to render them seamlessly.
    fun bitmapToBase64DataUri(bitmap: Bitmap, maxDimension: Int = 480): String {
        val ratio = minOf(
            maxDimension.toFloat() / bitmap.width,
            maxDimension.toFloat() / bitmap.height,
            1.0f
        )
        val width = (bitmap.width * ratio).toInt().coerceAtLeast(1)
        val height = (bitmap.height * ratio).toInt().coerceAtLeast(1)
        val resized = bitmap.scale(width, height)

        val outputStream = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        val dataUri = "data:image/jpeg;base64,$base64"
        synchronized(bitmapCache) {
            bitmapCache[dataUri] = resized
        }
        return dataUri
    }

    fun base64DataUriToBitmap(dataUri: String?): Bitmap? {
        if (dataUri.isNullOrBlank()) return null
        synchronized(bitmapCache) {
            bitmapCache[dataUri]?.let { return it }
        }
        return try {
            val base64Part = dataUri.substringAfter("base64,", dataUri)
            val bytes = Base64.decode(base64Part, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.also { decoded ->
                synchronized(bitmapCache) {
                    bitmapCache[dataUri] = decoded
                }
            }
        } catch (_: Exception) {
            null
        }
    }
}