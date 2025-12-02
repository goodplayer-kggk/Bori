package com.goodground.bori.ui.photo.editor

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.goodground.bori.R
import com.goodground.bori.databinding.ActivityPhotoEditorBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.slider.Slider

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

        makeFabDraggableAndClickable()
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

    @SuppressLint("ClickableViewAccessibility")
    private fun makeFabDraggableAndClickable() {
        var dX = 0f
        var dY = 0f
        var startX = 0f
        var startY = 0f
        var isDragging = false

        val clickThreshold = 10

        binding.fabSelectImage.setOnTouchListener { view, event ->
            val parent = view.parent as View
            val parentWidth = parent.width
            val parentHeight = parent.height

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    isDragging = false
                    startX = event.rawX
                    startY = event.rawY
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = Math.abs(event.rawX - startX)
                    val deltaY = Math.abs(event.rawY - startY)

                    if (deltaX > clickThreshold || deltaY > clickThreshold) {
                        isDragging = true
                    }

                    if (isDragging) {
                        var newX = event.rawX + dX
                        var newY = event.rawY + dY

                        // 🔥 화면 경계 제한
                        // 좌측 경계
                        if (newX < 0f) newX = 0f
                        // 우측 경계
                        if (newX > parentWidth - view.width)
                            newX = (parentWidth - view.width).toFloat()

                        // 상단 경계
                        if (newY < 0f) newY = 0f
                        // 하단 경계
                        if (newY > parentHeight - view.height)
                            newY = (parentHeight - view.height).toFloat()

                        // 위치 적용
                        view.x = newX
                        view.y = newY
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        binding.fabSelectImage.performClick() // ← 클릭 처리
                    }
                }
            }
            true
        }
    }

    private fun setupToolButtons() {
        binding.btnBrightness.setOnClickListener {
            showAdjustmentSlider(
                "Brightness",
                -100,
                100,
                0) { value ->
                editedBitmap = ImageFilters.adjustBrightness(originalBitmap!!, value.toFloat())
                binding.imageView.setImageBitmap(editedBitmap)
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

        binding.btnHue.setOnClickListener {
            showHueDialog()
        }

        binding.btnColorTemp.setOnClickListener {
            showAdjustmentSlider(
                title = "Color Temperature",
                min = -100,
                max = 100,
                initial = 0
            ) { value ->
                editedBitmap = ImageFilters.adjustColorTemperature(originalBitmap!!, value)
                binding.imageView.setImageBitmap(editedBitmap)
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

    private fun showAdjustmentSlider(
        title: String,
        min: Int = -100,
        max: Int = 100,
        initial: Int = 0,
        onValueChanged: (Int) -> Unit
    ) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_slider_adjustment, null)
        dialog.setContentView(view)

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvValue = view.findViewById<TextView>(R.id.tvValue)
        val slider = view.findViewById<SeekBar>(R.id.seekBar)

        tvTitle.text = title

        slider.max = max - min
        slider.progress = initial - min
        tvValue.text = initial.toString()

        // 값 변경 이벤트
        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val realValue = min + progress
                tvValue.text = realValue.toString()
                onValueChanged(realValue)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        dialog.show()
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

    private fun showHueDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_hue, null)
        val slider = dialogView.findViewById<Slider>(R.id.sliderHue)
        val btnApply = dialogView.findViewById<Button>(R.id.btnApplyHue)

        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogView)

        slider.addOnChangeListener { _, value, _ ->
            selectedBitmap?.let {
                val preview = ImageFilters.adjustHue(it, value)
                binding.imageView.setImageBitmap(preview)
            }
        }

        btnApply.setOnClickListener {
            selectedBitmap = (binding.imageView.drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)
            dialog.dismiss()
        }

        dialog.show()
    }
}
