package com.example.skyoxmodel

import android.content.Context
import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CubeMap (val myContext: Context){
    private val TEXTURE_FILES = arrayOf("right.png", "left.png", "top.png", "bottom.png", "back.png", "front.png")
    private val drawOrder = intArrayOf(
        0, 3, 2, 0, 2, 1,
        2, 3, 7, 2, 7, 6,
        1, 2, 6, 1, 6, 5,
        4, 0, 1, 4, 1, 5,
        3, 0, 4, 3, 4, 7,
        5, 6, 7, 5, 7, 4,
    )

    private val vertexCoords = FloatArray(108).apply{//36*3(x,y,z)
        val vertex = arrayOf(
            floatArrayOf(-0.5f, 0.5f, -0.5f),
            floatArrayOf(-0.5f, -0.5f, -0.5f),
            floatArrayOf(0.5f, -0.5f, -0.5f),
            floatArrayOf(0.5f, 0.5f, -0.5f),
            floatArrayOf(-0.5f, 0.5f, 0.5f),
            floatArrayOf(-0.5f, -0.5f, 0.5f),
            floatArrayOf(0.5f, -0.5f, 0.5f),
            floatArrayOf(0.5f, 0.5f, 0.5f),
        )
        var index = 0
        for(i in 0..35){    //정점 찍기
            this[index++] = vertex[drawOrder[i]][0] //x
            this[index++] = vertex[drawOrder[i]][1] //y
            this[index++] = vertex[drawOrder[i]][2] //z
        }
    }

    private val vertexNormals = FloatArray(108).apply{
        val normals = arrayOf(
            floatArrayOf(-0.57735f, 0.57735f, -0.57735f),
            floatArrayOf(-0.57735f, -0.57735f, -0.57735f),
            floatArrayOf(0.57735f, -0.57735f, -0.57735f),
            floatArrayOf(0.57735f, 0.57735f, -0.57735f),
            floatArrayOf(-0.57735f, 0.57735f, 0.57735f),
            floatArrayOf(-0.57735f, -0.57735f, 0.57735f),
            floatArrayOf(0.57735f, -0.57735f, 0.57735f),
            floatArrayOf(0.57735f, 0.57735f, 0.57735f)
        )
        var index = 0
        for (i in 0 .. 35){
            this[index++] = normals[drawOrder[i]][0]
            this[index++] = normals[drawOrder[i]][1]
            this[index++] = normals[drawOrder[i]][2]
        }
    }

    private val vertexBuffer =
        ByteBuffer.allocateDirect(vertexCoords.size * 4).run(){
            order(ByteOrder.nativeOrder())

            asFloatBuffer().apply {
                put(vertexCoords)
                position(0)
            }
        }

    private val normalBuffer =
        ByteBuffer.allocateDirect(vertexNormals.size * 4).run(){
            order(ByteOrder.nativeOrder())

            asFloatBuffer().apply {
                put(vertexNormals)
                position(0)
            }
        }

    private var mProgram = -1
    private var cube1Handle = -1
    private var mWorldMatHandle = -1
    private var mvpMatrixHandle = -1
    private var mEyePosHandle = -1

    private val vertexCount:Int = vertexCoords.size / COORDS_PER_VERTEX
    private val vertexStride:Int = COORDS_PER_VERTEX * 4
    private var textureID = IntArray(1)

    init {
        val vertexShader: Int =
            loadShader(GLES30.GL_VERTEX_SHADER, "cubeVertexShader.glsl", myContext)
        val fragmentShader: Int =
            loadShader(GLES30.GL_FRAGMENT_SHADER, "cubeFragmentShader.glsl", myContext)
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
        GLES30.glEnableVertexAttribArray(13)
        GLES30.glVertexAttribPointer(
            13,
            COORDS_PER_VERTEX,
            GLES30.GL_FLOAT,
            false,
            vertexStride,
            normalBuffer
        )

        mWorldMatHandle = GLES30.glGetUniformLocation(mProgram,"worldMat")
        mvpMatrixHandle = GLES30.glGetUniformLocation(mProgram,"uMVPMatrix")
        cube1Handle = GLES30.glGetUniformLocation(mProgram,"cubeMap")
        mEyePosHandle = GLES30.glGetUniformLocation(mProgram, "eyePos").also{
            GLES30.glUniform3fv(it, 1, eyePos, 0)
        }

        GLES30.glUniform1i(cube1Handle, 2)
        GLES30.glGenTextures(1, textureID, 0)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE2)
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
            GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X+i, 0, GLES30.GL_RGBA, data.width, data.height, 0,
                GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, byteBuffer)
        }
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
    }


    fun draw(mvpMatrix:FloatArray, worldMat:FloatArray){
//        GLES30.glDepthMask(false)

        GLES30.glUseProgram(mProgram)
        GLES30.glUniform3fv(mEyePosHandle, 1, eyePos, 0)
        GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES30.glUniformMatrix4fv(mWorldMatHandle, 1, false, worldMat, 0)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)

//        GLES30.glDepthMask(true)
    }
}