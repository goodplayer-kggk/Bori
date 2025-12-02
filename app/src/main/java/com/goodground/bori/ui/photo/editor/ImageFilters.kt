package com.goodground.bori.ui.photo.editor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

object ImageFilters {

    /**
     * 🔥 공통 함수 – ColorMatrix를 적용해 새 Bitmap 반환
     */
    private fun applyColorMatrix(src: Bitmap, matrix: ColorMatrix): Bitmap {
        val result = Bitmap.createBitmap(src.width, src.height, src.config)
        val canvas = Canvas(result)

        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(matrix)
        }

        canvas.drawBitmap(src, 0f, 0f, paint)
        return result
    }

    /**
     * ✨ 밝기 조절 (-100 ~ 100)
     */
    fun adjustBrightness(src: Bitmap, value: Float): Bitmap {
        val normalized = value / 100f * 255f

        val matrix = ColorMatrix(
            floatArrayOf(
                1f, 0f, 0f, 0f, normalized,
                0f, 1f, 0f, 0f, normalized,
                0f, 0f, 1f, 0f, normalized,
                0f, 0f, 0f, 1f, 0f
            )
        )
        return applyColorMatrix(src, matrix)
    }

    /**
     * 🔥 대비 조절 (0.0 ~ 3.0)
     * 1.0 = 기본
     */
    fun adjustContrast(src: Bitmap, contrast: Float): Bitmap {
        val translation = (-0.5f * contrast + 0.5f) * 255f

        val matrix = ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, translation,
                0f, contrast, 0f, 0f, translation,
                0f, 0f, contrast, 0f, translation,
                0f, 0f, 0f, 1f, 0f
            )
        )
        return applyColorMatrix(src, matrix)
    }

    /**
     * 🎨 채도 조절 (0f = 흑백, 1f = 기본, 2f = 채도 2배)
     */
    fun adjustSaturation(src: Bitmap, value: Float): Bitmap {
        val matrix = ColorMatrix()
        matrix.setSaturation(value)
        return applyColorMatrix(src, matrix)
    }

    /**
     * 🎨 색조 회전 (Hue)  -180 ~ 180
     */
    fun adjustHue(src: Bitmap, degrees: Float): Bitmap {
        val matrix = ColorMatrix()
        matrix.setRotate(0, degrees)
        matrix.setRotate(1, degrees)
        matrix.setRotate(2, degrees)
        return applyColorMatrix(src, matrix)
    }

    fun adjustColorTemperature(src: Bitmap, value: Int): Bitmap {
        // value 범위: -100(차갑게) ~ +100(따뜻하게)
        val bitmap = src.copy(Bitmap.Config.ARGB_8888, true)

        val warm = value / 100f // -1.0 ~ +1.0

        // 따뜻한 경우 → R과 G 증가
        val rScale = 1f + (warm * 0.4f)
        val gScale = 1f + (warm * 0.2f)

        // 차가운 경우 → B 증가 (warm 음수일 때)
        val bScale = 1f - (warm * 0.4f)

        val cm = ColorMatrix(
            floatArrayOf(
                rScale, 0f,     0f,     0f, 0f,
                0f,     gScale, 0f,     0f, 0f,
                0f,     0f,     bScale, 0f, 0f,
                0f,     0f,     0f,     1f, 0f
            )
        )

        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(cm)

        val canvas = Canvas(bitmap)
        canvas.drawBitmap(src, 0f, 0f, paint)

        return bitmap
    }

    /**
     * 🔥 Blur, Sharpen 같은 효과는 ColorMatrix로는 한계가 있으므로
     * Convolution Kernel 방식 별도로 준비
     */
    fun applyKernel(src: Bitmap, kernel: FloatArray): Bitmap {
        require(kernel.size == 9) { "Kernel must be 3x3 (9 elements)" }

        val width = src.width
        val height = src.height
        val result = Bitmap.createBitmap(width, height, src.config)

        val pixels = IntArray(width * height)
        val output = IntArray(width * height)

        src.getPixels(pixels, 0, width, 0, 0, width, height)

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {

                var r = 0f
                var g = 0f
                var b = 0f

                var idx = 0

                // 3x3 Kernel 적용
                for (ky in -1..1) {
                    for (kx in -1..1) {
                        val pixel = pixels[(y + ky) * width + (x + kx)]

                        r += ((pixel shr 16) and 0xFF) * kernel[idx]
                        g += ((pixel shr 8) and 0xFF) * kernel[idx]
                        b += (pixel and 0xFF) * kernel[idx]

                        idx++
                    }
                }

                val nr = r.coerceIn(0f, 255f).toInt()
                val ng = g.coerceIn(0f, 255f).toInt()
                val nb = b.coerceIn(0f, 255f).toInt()

                output[y * width + x] = (0xFF shl 24) or (nr shl 16) or (ng shl 8) or nb
            }
        }

        result.setPixels(output, 0, width, 0, 0, width, height)
        return result
    }
}