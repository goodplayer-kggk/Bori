package com.goodground.bori.ui.photo.editor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import kotlin.math.max
import kotlin.math.min

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

    fun adjustSharpen(src: Bitmap, value: Int): Bitmap {
        // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        // TODO: performance issue(O(n3)). need to change algorithm <<<<<<<<<<<<<<<<<<<<
        // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

        val amount = value / 100f  // value: 0 ~ 100
        if (amount <= 0f) return src

        // sharpen kernel 동적 생성
        val kernel = floatArrayOf(
            0f, -amount, 0f,
            -amount, 1f + (amount * 4f), -amount,
            0f, -amount, 0f
        )

        return applyConvolution(src, kernel)
    }

    private fun applyConvolution(src: Bitmap, kernel: FloatArray): Bitmap {
        val width = src.width
        val height = src.height

        val pixels = IntArray(width * height)
        val outPixels = IntArray(width * height)
        src.getPixels(pixels, 0, width, 0, 0, width, height)

        // operation time is over O(n3).
        // need to change this to GPU shader or tensorflowlite version
        ///////////////////////////////
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {

                var r = 0f; var g = 0f; var b = 0f
                var idx = 0

                for (ky in -1..1) {
                    val row = (y + ky) * width
                    for (kx in -1..1) {
                        val color = pixels[row + (x + kx)]
                        val k = kernel[idx++]

                        r += ((color shr 16) and 0xFF) * k
                        g += ((color shr 8) and 0xFF) * k
                        b += (color and 0xFF) * k
                    }
                }

                outPixels[y * width + x] =
                    (0xFF shl 24) or
                            (r.coerceIn(0f, 255f).toInt() shl 16) or
                            (g.coerceIn(0f, 255f).toInt() shl 8) or
                            b.coerceIn(0f, 255f).toInt()
            }
        }

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(outPixels, 0, width, 0, 0, width, height)
        return result
    }

    /**
     * 밝은 영역(Highlights) 낮추기
     * @param value -100 ~ +100
     */
    fun adjustHighlights(src: Bitmap, value: Int): Bitmap {
        val factor = value / 100f
        val width = src.width
        val height = src.height

        val pixels = IntArray(width * height)
        val out = IntArray(width * height)
        src.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val c = pixels[i]

            val r = (c shr 16) and 0xFF
            val g = (c shr 8) and 0xFF
            val b = c and 0xFF

            val luminance = (0.299 * r + 0.587 * g + 0.114 * b).toInt()

            val weight = luminance / 255f  // 밝을수록 1에 가까움

            val newR = (r - (r * weight * factor)).coerceIn(0f, 255f)
            val newG = (g - (g * weight * factor)).coerceIn(0f, 255f)
            val newB = (b - (b * weight * factor)).coerceIn(0f, 255f)

            out[i] =
                (0xFF shl 24) or
                        (newR.toInt() shl 16) or
                        (newG.toInt() shl 8) or
                        newB.toInt()
        }

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(out, 0, width, 0, 0, width, height)
        return result
    }


    /**
     * 어두운 영역(Shadows) 밝게 만들기
     * @param value -100 ~ +100
     */
    fun adjustShadows(src: Bitmap, value: Int): Bitmap {
        val factor = value / 100f
        val width = src.width
        val height = src.height

        val pixels = IntArray(width * height)
        val out = IntArray(width * height)
        src.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val c = pixels[i]

            val r = (c shr 16) and 0xFF
            val g = (c shr 8) and 0xFF
            val b = c and 0xFF

            val luminance = (0.299 * r + 0.587 * g + 0.114 * b).toInt()

            val weight = (255 - luminance) / 255f  // 어두울수록 1에 가까움

            val newR = (r + (255 - r) * weight * factor).coerceIn(0f, 255f)
            val newG = (g + (255 - g) * weight * factor).coerceIn(0f, 255f)
            val newB = (b + (255 - b) * weight * factor).coerceIn(0f, 255f)

            out[i] =
                (0xFF shl 24) or
                        (newR.toInt() shl 16) or
                        (newG.toInt() shl 8) or
                        newB.toInt()
        }

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(out, 0, width, 0, 0, width, height)
        return result
    }

    fun fastBlur(src: Bitmap, radius: Int): Bitmap {
        val r = radius.coerceIn(1, 100) // 필요하면 적당히 제한
        // 항상 ARGB_8888로 작업
        val bitmap = src.copy(Bitmap.Config.ARGB_8888, true)
        val w = bitmap.width
        val h = bitmap.height
        val wh = w * h
        val pix = IntArray(wh)
        bitmap.getPixels(pix, 0, w, 0, 0, w, h)

        // 작업용 배열
        val rArr = IntArray(wh)
        val gArr = IntArray(wh)
        val bArr = IntArray(wh)

        val div = r * 2 + 1
        val dv = IntArray(256 * div)
        for (i in dv.indices) dv[i] = i / div

        // vmin 재사용 배열
        val vmin = IntArray(max(w, h))

        // --- 가로 패스 ---
        var yi = 0
        var yw = 0
        for (y in 0 until h) {
            var rsum = 0
            var gsum = 0
            var bsum = 0

            // 초기 윈도우 합
            for (i in -r..r) {
                val xIndex = (i).coerceAtLeast(0).coerceAtMost(w - 1)
                val p = pix[yw + xIndex]
                rsum += (p shr 16) and 0xFF
                gsum += (p shr 8) and 0xFF
                bsum += p and 0xFF
            }

            for (x in 0 until w) {
                // 안전한 인덱싱으로 저장
                rArr[yi] = dv[rsum.coerceIn(0, dv.size - 1)]
                gArr[yi] = dv[gsum.coerceIn(0, dv.size - 1)]
                bArr[yi] = dv[bsum.coerceIn(0, dv.size - 1)]

                // 다음 x로 슬라이딩 윈도우: 빠진 픽과 들어오는 픽 계산
                val addX = (x + r + 1).coerceAtMost(w - 1)
                val subX = (x - r).coerceAtLeast(0)

                val pAdd = pix[yw + addX]
                val pSub = pix[yw + subX]

                rsum += ((pAdd shr 16) and 0xFF) - ((pSub shr 16) and 0xFF)
                gsum += ((pAdd shr 8) and 0xFF) - ((pSub shr 8) and 0xFF)
                bsum += (pAdd and 0xFF) - (pSub and 0xFF)

                yi++
            }
            yw += w
        }

        // --- 세로 패스 ---
        for (x in 0 until w) {
            var rsum = 0
            var gsum = 0
            var bsum = 0
            // 초기 윈도우 합 (세로)
            for (i in -r..r) {
                val yIndex = (i).coerceAtLeast(0).coerceAtMost(h - 1)
                val idx = (yIndex * w + x)
                rsum += rArr[idx]
                gsum += gArr[idx]
                bsum += bArr[idx]
            }

            var yi2 = x
            for (y in 0 until h) {
                // 안전하게 픽셀이할당
                val rr = dv[rsum.coerceIn(0, dv.size - 1)]
                val gg = dv[gsum.coerceIn(0, dv.size - 1)]
                val bb = dv[bsum.coerceIn(0, dv.size - 1)]
                pix[yi2] = (0xFF shl 24) or (rr shl 16) or (gg shl 8) or bb

                // 슬라이드 윈도우: 들어오고 나가는 y 좌표 계산
                val addY = (y + r + 1).coerceAtMost(h - 1)
                val subY = (y - r).coerceAtLeast(0)

                val addIdx = addY * w + x
                val subIdx = subY * w + x

                rsum += rArr[addIdx] - rArr[subIdx]
                gsum += gArr[addIdx] - gArr[subIdx]
                bsum += bArr[addIdx] - bArr[subIdx]

                yi2 += w
            }
        }

        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(pix, 0, w, 0, 0, w, h)
        return out
    }
}