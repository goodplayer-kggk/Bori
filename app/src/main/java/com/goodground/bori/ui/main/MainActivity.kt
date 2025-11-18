package com.goodground.bori.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.goodground.bori.databinding.ActivityMainBinding
import com.goodground.bori.ui.photo.editor.PhotoEditorActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val intent = Intent(this, PhotoEditorActivity::class.java)
                intent.putExtra("imageUri", it.toString())
                startActivity(intent)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEditPhoto.setOnClickListener {
            galleryLauncher.launch("image/*")
        }
    }
}