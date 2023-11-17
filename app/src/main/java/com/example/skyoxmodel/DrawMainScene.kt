package com.example.skyoxmodel

import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import com.example.skyoxmodel.databinding.ActivityMainSceneBinding

class DrawMainScene : AppCompatActivity() {
    val binding: ActivityMainSceneBinding by lazy{
        ActivityMainSceneBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        initSurfaceView()
        setContentView(binding.root)

        binding.eyeLeft.setOnClickListener {
            cameraRoatate(0.174f)
            binding.surfaceView.requestRender()
        }
        binding.eyeRight.setOnClickListener {
            cameraRoatate(-0.174f)
            binding.surfaceView.requestRender()
        }
        binding.eyeForward.setOnClickListener {
//            cameraMove(0.5f, 0f)
            camera_look_up(0.05f)
            binding.surfaceView.requestRender()
        }
        binding.eyeBackWard.setOnClickListener {
//            cameraMove(-0.5f, 0f)
            camera_look_up(-0.05f)
            binding.surfaceView.requestRender()
        }
    }

    fun initSurfaceView() {
        binding.surfaceView.setEGLContextClientVersion(3)

        binding.surfaceView.setRenderer(MainGLRenderer(this, this))

        binding.surfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }

    fun showPopup() {
        Handler(Looper.getMainLooper()).post {
            val alertDialog = AlertDialog.Builder(this).create()
            alertDialog.setTitle("종료")
            alertDialog.setMessage("확인 버튼을 눌러주세요.")

            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { dialog, _ ->
                // OK 버튼 클릭 시 처리할 로직
                this.finish()
                dialog.dismiss()
            }
            alertDialog.show()
        }
    }
}