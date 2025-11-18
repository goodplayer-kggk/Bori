package com.goodground.bori.ui.photo.editor

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupResultLauncher()
        setupUI()
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
}
