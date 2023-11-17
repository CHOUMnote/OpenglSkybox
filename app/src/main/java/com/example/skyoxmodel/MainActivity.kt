package com.example.skyoxmodel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.skyoxmodel.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var mainSurfaceView:MainGLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.start.setOnClickListener {
            startActivity(Intent(this@MainActivity, DrawMainScene::class.java))
        }

        binding.debug.setOnClickListener {
            startActivity(Intent(this@MainActivity, Debug::class.java))
        }
    }
}