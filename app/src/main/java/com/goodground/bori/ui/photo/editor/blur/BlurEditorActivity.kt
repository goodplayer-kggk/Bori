package com.goodground.bori.ui.photo.editor.blur

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.goodground.bori.databinding.ActivityBlurEditorBinding
import java.io.InputStream

class BlurEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlurEditorBinding
    private lateinit var originalBitmap: Bitmap

    companion object {
        const val EXTRA_IMAGE_URI = "image_uri"
        const val EXTRA_RESULT_BITMAP = "result_bitmap"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBlurEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadImage()
        setupButtons()
    }

    private fun loadImage() {
        val imageUriString = intent.getStringExtra(EXTRA_IMAGE_URI)
        if (imageUriString.isNullOrEmpty()) {
            finish()
            return
        }

        val uri = Uri.parse(imageUriString)
        originalBitmap = uriToBitmap(uri)
        binding.blurPaintView.setImage(originalBitmap)
    }

    private fun uriToBitmap(uri: Uri): Bitmap {
        val input: InputStream? = contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(input!!)
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        binding.btnDone.setOnClickListener {
            val resultBitmap = binding.blurPaintView.exportResult()

            val byteArray = BitmapUtils.bitmapToByteArray(resultBitmap)

            val intent = Intent()
            intent.putExtra(EXTRA_RESULT_BITMAP, byteArray)

            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}