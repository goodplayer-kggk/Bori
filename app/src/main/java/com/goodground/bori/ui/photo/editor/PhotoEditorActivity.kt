package com.goodground.bori.ui.photo.editor

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
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

    private lateinit var bottomSheet: BottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupResultLauncher()
        setupUI()

        bottomSheet = BottomSheetDialog(this)
        setupToolButtons()
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

//                enableTools()
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

    private fun setupToolButtons() {
        binding.btnBrightness.setOnClickListener {
            openAdjustmentSheet("밝기") { value ->
                applyBrightness(value)
            }
        }

        binding.btnContrast.setOnClickListener {
            openAdjustmentSheet("대비") { value ->
                applyContrast(value)
            }
        }

        binding.btnSaturation.setOnClickListener {
            openAdjustmentSheet("채도") { value ->
                applySaturation(value)
            }
        }
    }

    private fun openAdjustmentSheet(title: String, onValueChange: (Int) -> Unit) {
        val view = layoutInflater.inflate(R.layout.bottom_adjustment_sheet, null)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val seek = view.findViewById<SeekBar>(R.id.seekValue)

        tvTitle.text = title
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                onValueChange(progress - 100) // -100 ~ +100
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    private fun applyBrightness(value: Int) {
        selectedBitmap?.let {
            binding.imageView.setImageBitmap(
                ImageFilters.adjustBrightness(it, value.toFloat())
            )
        }
    }

    private fun applyContrast(value: Int) {
        selectedBitmap?.let {
            binding.imageView.setImageBitmap(
                ImageFilters.adjustContrast(it, value.toFloat())
            )
        }
    }

    private fun applySaturation(value: Int) {
        selectedBitmap?.let {
            binding.imageView.setImageBitmap(
                ImageFilters.adjustSaturation(it, value.toFloat())
            )
        }
    }
}
