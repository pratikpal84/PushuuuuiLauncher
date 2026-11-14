package com.pushuuu.launcher

import android.content.Context
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

object IconCache {
    private val cache = HashMap<String, ImageBitmap>()

    fun get(context: Context, packageName: String): ImageBitmap? {
        cache[packageName]?.let { return it }
        return try {
            val drawable = context.packageManager.getApplicationIcon(packageName)
            val bitmap = drawable.toBitmap(144, 144).asImageBitmap()
            cache[packageName] = bitmap
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}
