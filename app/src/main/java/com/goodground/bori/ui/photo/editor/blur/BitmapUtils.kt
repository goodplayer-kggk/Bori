package com.goodground.bori.ui.photo.editor.blur

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

object BitmapUtils {

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        return stream.toByteArray()
    }
}
