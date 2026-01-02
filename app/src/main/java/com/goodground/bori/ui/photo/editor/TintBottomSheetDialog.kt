package com.goodground.bori.ui.photo.editor

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.SeekBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.goodground.bori.databinding.DialogTintBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class TintBottomSheetDialog(
    context: Context,
    private val onTintChanged: (color: Int, strength: Float) -> Unit
) : BottomSheetDialog(context) {

    private lateinit var binding: DialogTintBinding
    private var currentColor: Int = Color.TRANSPARENT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogTintBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPresets()
        setupSlider()
    }

    private fun setupPresets() {
        val presets = listOf(
            TintPreset("Warm", Color.parseColor("#FFD6A5")),
            TintPreset("Cool", Color.parseColor("#A5D8FF")),
            TintPreset("Pink", Color.parseColor("#F4C2C2")),
            TintPreset("Green", Color.parseColor("#C1E1C1"))
        )

        binding.rvTintPresets.apply {
            layoutManager = LinearLayoutManager(
                context, LinearLayoutManager.HORIZONTAL, false
            )
            adapter = TintPresetAdapter(presets) { preset ->
                currentColor = preset.color
                notifyChange()
            }
        }
    }

    private fun setupSlider() {
        binding.seekTint.progress = 50
        binding.seekTint.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, value: Int, fromUser: Boolean) {
                notifyChange()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun notifyChange() {
        val strength = binding.seekTint.progress / 100f
        onTintChanged(currentColor, strength)
    }
}