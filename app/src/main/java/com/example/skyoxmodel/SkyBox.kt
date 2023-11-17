package com.example.skyoxmodel

import android.content.Context
import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SkyBox (val myContext: Context){
    private val SIZE = 1f
    private val vertexCoords = floatArrayOf(
        -SIZE, SIZE, -SIZE,
        -SIZE, -SIZE, -SIZE,
        SIZE, -SIZE, -SIZE,
        SIZE, -SIZE, -SIZE,
        SIZE, SIZE, -SIZE,
        -SIZE, SIZE, -SIZE,

        -SIZE, -SIZE, SIZE,
        -SIZE, -SIZE, -SIZE,
        -SIZE, SIZE, -SIZE,
        -SIZE, SIZE, -SIZE,
        -SIZE, SIZE, SIZE,
        -SIZE, -SIZE, SIZE,

        SIZE, -SIZE, -SIZE,
        SIZE, -SIZE, SIZE,
        SIZE, SIZE, SIZE,
        SIZE, SIZE, SIZE,
        SIZE, SIZE, -SIZE,
        SIZE, -SIZE, -SIZE,

        -SIZE, -SIZE, SIZE,
        -SIZE, SIZE, SIZE,
        SIZE, SIZE, SIZE,
        SIZE, SIZE, SIZE,
        SIZE, -SIZE, SIZE,
        -SIZE, -SIZE, SIZE,

        -SIZE, SIZE, -SIZE,
        SIZE, SIZE, -SIZE,
        SIZE, SIZE, SIZE,
        SIZE, SIZE, SIZE,
        -SIZE, SIZE, SIZE,
        -SIZE, SIZE, -SIZE,

        -SIZE, -SIZE, -SIZE,
        -SIZE, -SIZE, SIZE,
        SIZE, -SIZE, -SIZE,
        SIZE, -SIZE, -SIZE,
        -SIZE, -SIZE, SIZE,
        SIZE, -SIZE, SIZE
    )
    private val TEXTURE_FILES = arrayOf("right.png", "left.png", "top.png", "bottom.png", "back.png", "front.png")
    private val TIME_TEXTURE_FILES = arrayOf("nightRight.png", "nightLeft.png", "nightTop.png", "nightBottom.png",
        "nightBack.png", "nightFront.png")

    private val vertexBuffer =
        ByteBuffer.allocateDirect(vertexCoords.size * 4).run(){
            order(ByteOrder.nativeOrder())

            asFloatBuffer().apply {
                put(vertexCoords)
                position(0)
            }
        }

    private var mProgram = -1
    private var viewMatHandle = -1
    private var proMatHandle = -1
    private var cube1Handle = -1
    private var cube2Handle = -1

    private val vertexCount:Int = vertexCoords.size / COORDS_PER_VERTEX
    private val vertexStride:Int = COORDS_PER_VERTEX * 4

    private var textureID = IntArray(2)
    private var timeTextureID = IntArray(1)

    init {
        val vertexShader: Int =
            loadShader(GLES30.GL_VERTEX_SHADER, "skyboxVertexShader.glsl", myContext)
        val fragmentShader: Int =
            loadShader(GLES30.GL_FRAGMENT_SHADER, "skyboxFragmentShader.glsl", myContext)
        mProgram = GLES30.glCreateProgram().also {
            GLES30.glAttachShader(it, vertexShader)
            GLES30.glAttachShader(it, fragmentShader)
            GLES30.glLinkProgram(it)
        }
        GLES30.glUseProgram(mProgram)
        GLES30.glEnableVertexAttribArray(12)
        GLES30.glVertexAttribPointer(
            12,
            COORDS_PER_VERTEX,
            GLES30.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )

        viewMatHandle = GLES30.glGetUniformLocation(mProgram,"viewMatrix")
        proMatHandle = GLES30.glGetUniformLocation(mProgram,"projectionMatrix")
        cube1Handle = GLES30.glGetUniformLocation(mProgram,"cubeMap")
        cube2Handle = GLES30.glGetUniformLocation(mProgram,"cubeMap2")

        GLES30.glUniform3f(GLES30.glGetUniformLocation(mProgram,"fogColor"), 0.2f, 0.2f, 0.2f)
//        GLES30.glUniform1f(GLES30.glGetUniformLocation(mProgram,"blendFactor"), 0.5f)
        GLES30.glUniform1i(cube1Handle, 0)
        GLES30.glUniform1i(cube2Handle, 1)
        GLES30.glGenTextures(2, textureID, 0)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, textureID[0])
        for(i in 0..5){
            val data = loadBitmap(TEXTURE_FILES[i], myContext)
            // 비트맵의 너비와 높이 구하기
            val width = data?.width ?: 0
            val height = data?.height ?: 0
            val byteBuffer = ByteBuffer.allocateDirect(width * height * 4) // RGBA 각각 1바이트씩 4바이트
            byteBuffer.order(ByteOrder.nativeOrder())
            data?.copyPixelsToBuffer(byteBuffer)
            byteBuffer.rewind()
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X+i, 0, GLES30.GL_RGBA, data.width, data.height, 0,
                GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, byteBuffer)
        }
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, textureID[1])
        for(i in 0..5){
            val data = loadBitmap(TIME_TEXTURE_FILES[i], myContext)
            // 비트맵의 너비와 높이 구하기
            val width = data?.width ?: 0
            val height = data?.height ?: 0
            val byteBuffer = ByteBuffer.allocateDirect(width * height * 4) // RGBA 각각 1바이트씩 4바이트
            byteBuffer.order(ByteOrder.nativeOrder())
            data?.copyPixelsToBuffer(byteBuffer)
            byteBuffer.rewind()
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X+i, 0, GLES30.GL_RGBA, data.width, data.height, 0,
                GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, byteBuffer)
        }

        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)

    }


    fun draw(viewMatrix:FloatArray, proMatrix:FloatArray, blend:Float){
        GLES30.glDepthMask(false)

        GLES30.glUseProgram(mProgram)

        GLES30.glUniformMatrix4fv(viewMatHandle, 1, false, viewMatrix, 0)
        GLES30.glUniformMatrix4fv(proMatHandle, 1, false, proMatrix, 0)
        GLES30.glUniform1f(GLES30.glGetUniformLocation(mProgram,"blendFactor"), blend)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)

        GLES30.glDepthMask(true)
    }
}