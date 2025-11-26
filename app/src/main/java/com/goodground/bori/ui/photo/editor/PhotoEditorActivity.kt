package com.goodground.bori.ui.photo.editor

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.SeekBar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.goodground.bori.R
import com.goodground.bori.databinding.ActivityPhotoEditorBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class PhotoEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhotoEditorBinding
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    private var selectedBitmap: Bitmap? = null
    private lateinit var originalBitmap: Bitmap
    private lateinit var editedBitmap: Bitmap
    private var currentContrast = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupResultLauncher()
        setupUI()

        binding.seekBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress - 100   // -100 ~ +100
                editedBitmap = applyBrightness(originalBitmap, value)
                binding.imageView.setImageBitmap(editedBitmap)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.sliderContrast.addOnChangeListener { _, value, _ ->
            currentContrast = value
            editedBitmap = applyContrast(originalBitmap, value)
            binding.imageView.setImageBitmap(editedBitmap)
        }
    }

    private fun setupUI() {
        // 처음엔 placeholder
        binding.imageView.setImageResource(R.drawable.ic_bori)

        // 이미지 선택 버튼
        binding.fabSelectImage.setOnClickListener {
            showImageSelectSheet()
        }
    }

    private fun setupResultLauncher() {
        resultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data

                // 갤러리 이미지
                if (data?.data != null) {
                    val uri = data.data!!
                    selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    binding.imageView.setImageBitmap(selectedBitmap)
                }

                // 카메라 이미지
                if (data?.extras?.get("data") != null) {
                    selectedBitmap = data.extras!!.get("data") as Bitmap
                    binding.imageView.setImageBitmap(selectedBitmap)
                }

                // 갤러리에서 받아온 Bitmap 넣기
                originalBitmap = selectedBitmap?.copy(Bitmap.Config.ARGB_8888, true)!!

                enableTools()
            }
        }
    }

    private fun showImageSelectSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.sheet_image_select, null)

        view.findViewById<View>(R.id.btnGallery).setOnClickListener {
            openGallery()
            dialog.dismiss()
        }

        view.findViewById<View>(R.id.btnCamera).setOnClickListener {
            openCamera()
            dialog.dismiss()
        }

        view.findViewById<View>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultLauncher.launch(intent)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        resultLauncher.launch(intent)
    }

    private fun enableTools() {
        // 이미지 편집용 기능 활성화
        binding.editTools.visibility = View.VISIBLE
    }

    private fun applyBrightness(bitmap: Bitmap, brightness: Int): Bitmap {
        val bmp = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val canvas = Canvas(bmp)

        // ColorMatrix 적용
        val brightnessMatrix = ColorMatrix(
            floatArrayOf(
                1f, 0f, 0f, 0f, brightness.toFloat(),
                0f, 1f, 0f, 0f, brightness.toFloat(),
                0f, 0f, 1f, 0f, brightness.toFloat(),
                0f, 0f, 0f, 1f, 0f
            )
        )

        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(brightnessMatrix)
        }

        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return bmp
    }

    private fun applyContrast(src: Bitmap, value: Float): Bitmap {
        // value: -100 ~ +100
        val contrast = (value + 100) / 100f   // 0.0 ~ 2.0

        val cm = ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, 0f,
                0f, contrast, 0f, 0f, 0f,
                0f, 0f, contrast, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )

        val ret = Bitmap.createBitmap(src.width, src.height, src.config)
        val canvas = Canvas(ret)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)

        return ret
    }
}
