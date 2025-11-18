package com.goodground.bori.ui.photo.editor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.goodground.bori.databinding.ActivityImageEditorBinding

class PhotoEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageEditorBinding
    private lateinit var ivPreview: ImageView
    private lateinit var originalBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ivPreview = binding.ivPreview

        val uri = Uri.parse(intent.getStringExtra("imageUri"))
        originalBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        ivPreview.setImageBitmap(originalBitmap)

        initSliders()
    }

    private fun initSliders() {
        binding.sbBrightness.apply {
            progress = 100
            setOnSeekBarChangeListener(changeListener)
        }
        binding.sbContrast.apply {
            progress = 100
            setOnSeekBarChangeListener(changeListener)
        }
        binding.sbSaturation.apply {
            progress = 100
            setOnSeekBarChangeListener(changeListener)
        }
    }

    private val changeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, value: Int, fromUser: Boolean) {
            applyAdjustments()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    private fun applyAdjustments() {
        val brightness = binding.sbBrightness.progress - 100
        val contrast = binding.sbContrast.progress / 100f
        val saturation = binding.sbSaturation.progress / 100f

        val cm = ColorMatrix()

        // 🔥 Saturation
        val satMatrix = ColorMatrix()
        satMatrix.setSaturation(saturation)
        cm.postConcat(satMatrix)

        // 🔥 Brightness
        val bMatrix = ColorMatrix(
            floatArrayOf(
                1f, 0f, 0f, 0f, brightness.toFloat(),
                0f, 1f, 0f, 0f, brightness.toFloat(),
                0f, 0f, 1f, 0f, brightness.toFloat(),
                0f, 0f, 0f, 1f, 0f
            )
        )
        cm.postConcat(bMatrix)

        // 🔥 Contrast
        val c = contrast
        val translate = (1 - c) * 128
        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                c, 0f, 0f, 0f, translate,
                0f, c, 0f, 0f, translate,
                0f, 0f, c, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        cm.postConcat(contrastMatrix)

        val filtered = Bitmap.createBitmap(
            originalBitmap.width,
            originalBitmap.height,
            originalBitmap.config
        )

        val canvas = Canvas(filtered)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(originalBitmap, 0f, 0f, paint)

        ivPreview.setImageBitmap(filtered)
    }
}