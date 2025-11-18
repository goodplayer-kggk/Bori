package com.goodground.bori.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.goodground.bori.databinding.ActivityMainBinding
import com.goodground.bori.ui.photo.editor.PhotoEditorActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEditPhoto.setOnClickListener {
            val intent = Intent(this, PhotoEditorActivity::class.java)
            startActivity(intent)
        }
    }
}