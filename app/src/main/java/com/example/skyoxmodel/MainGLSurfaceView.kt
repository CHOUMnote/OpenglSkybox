package com.example.skyoxmodel

import android.content.Context
import android.opengl.GLSurfaceView

class MainGLSurfaceView(context:Context, act : DrawMainScene) : GLSurfaceView(context){
    private val mainRenderer:MainGLRenderer
    init {
        setEGLContextClientVersion(3)
        mainRenderer = MainGLRenderer(context, act)
        setRenderer(mainRenderer)
        renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }
}