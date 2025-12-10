package com.goodground.bori.ui.photo.editor.blur

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.goodground.bori.ui.photo.editor.ImageFilters

class BlurPaintView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private lateinit var originalBitmap: Bitmap
    private lateinit var blurredBitmap: Bitmap

    private lateinit var maskBitmap: Bitmap
    private lateinit var maskCanvas: Canvas

    private val drawPath = Path()

    private val pathPaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 80f
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val maskPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        isAntiAlias = true
    }

    private var isInitialized = false

    fun setImage(bitmap: Bitmap) {

        val base = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        val scale = 0.25f
        val smallBitmap = Bitmap.createScaledBitmap(
            base,
            (bitmap.width * scale).toInt(),
            (bitmap.height * scale).toInt(),
            true
        )

        originalBitmap = smallBitmap

        // Blur 안정 값
        blurredBitmap = ImageFilters.fastBlur(smallBitmap, 18)

        maskBitmap = Bitmap.createBitmap(
            smallBitmap.width,
            smallBitmap.height,
            Bitmap.Config.ARGB_8888
        )
        maskCanvas = Canvas(maskBitmap)

        isInitialized = true
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (!isInitialized) return

        canvas.drawBitmap(originalBitmap, 0f, 0f, null)

        val combined = Bitmap.createBitmap(
            originalBitmap.width,
            originalBitmap.height,
            Bitmap.Config.ARGB_8888
        )

        val c = Canvas(combined)
        c.drawBitmap(blurredBitmap, 0f, 0f, null)
        c.drawBitmap(maskBitmap, 0f, 0f, maskPaint)

        canvas.drawBitmap(combined, 0f, 0f, null)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isInitialized) return false

        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                drawPath.reset()
                drawPath.moveTo(x, y)
            }

            MotionEvent.ACTION_MOVE -> {
                drawPath.lineTo(x, y)
                maskCanvas.drawPath(drawPath, pathPaint)
                invalidate()
            }

            MotionEvent.ACTION_UP -> drawPath.reset()
        }

        return true
    }

    fun exportResult(): Bitmap {
        val result = Bitmap.createBitmap(
            originalBitmap.width,
            originalBitmap.height,
            Bitmap.Config.ARGB_8888
        )

        val c = Canvas(result)
        c.drawBitmap(originalBitmap, 0f, 0f, null)

        val combined = Bitmap.createBitmap(
            originalBitmap.width,
            originalBitmap.height,
            Bitmap.Config.ARGB_8888
        )
        val cc = Canvas(combined)
        cc.drawBitmap(blurredBitmap, 0f, 0f, null)
        cc.drawBitmap(maskBitmap, 0f, 0f, maskPaint)

        c.drawBitmap(combined, 0f, 0f, null)
        return result
    }
}