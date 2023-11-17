package com.example.skyoxmodel

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.skyoxmodel.databinding.ActivityDebugBinding


class Debug : AppCompatActivity() {
    private lateinit var mySensors: MySensors
    val binding: ActivityDebugBinding by lazy{
        ActivityDebugBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        supportActionBar?.hide()
        setContentView(binding.root)

        mySensors = MySensors(this)
        mySensors.startListening { x, y, z ->
            binding.accX.text = x.toString()
            binding.accY.text = y.toString()
            binding.accZ.text = z.toString()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mySensors.stopListening()
    }
}