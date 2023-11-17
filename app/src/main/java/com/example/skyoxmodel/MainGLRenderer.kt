package com.example.skyoxmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import android.util.Log
import java.io.BufferedInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

const val COORDS_PER_VERTEX = 3

var eyePos = floatArrayOf(0f,3f,3f)
var eyeAt = floatArrayOf(0f,3f,3f)
var cameraVec = floatArrayOf(0f, 0.0f, -0.7071f)

val lightDir = floatArrayOf(1.0f,1.0f,1.0f)
val lightAmbient = floatArrayOf(0.1f,0.1f,0.1f)
val lightDiffuse = floatArrayOf(1.0f,1.0f,1.0f)
val lightSpecular = floatArrayOf(1.0f,1.0f,1.0f)

private var startTime = SystemClock.uptimeMillis()
private var rotYAngle = 0f;

var prevPosX = 3f
var outwardDir = true
var spinDegree = 0.0f

var objectPos = arrayOf(
    floatArrayOf(3f,0f,-7f), floatArrayOf(-3f,0f,-7f),
    floatArrayOf(3f,0f,-4f), floatArrayOf(-3f,0f,-4f),
    floatArrayOf(3f,0f,-1f), floatArrayOf(-3f,0f,-1f),
)

var time = 0f

private lateinit var mySensors: MySensors

class MainGLRenderer(val context: Context, val act : DrawMainScene) : GLSurfaceView.Renderer{
    private var modelMatrix = FloatArray(16).apply {Matrix.setIdentityM(this, 0)}
    private var viewMatrix = FloatArray(16).apply {Matrix.setIdentityM(this, 0)}
    private var skyViewMatrix = FloatArray(16).apply {Matrix.setIdentityM(this, 0)}
    private var projectionMatrix = FloatArray(16).apply {Matrix.setIdentityM(this, 0)}
    private var vpMatrix = FloatArray(16).apply {Matrix.setIdentityM(this, 0)}
    private var mvpMatrix = FloatArray(16).apply {Matrix.setIdentityM(this, 0)}

    private lateinit var mGround:MyLitTexGround
    private lateinit var mGround2:MyLitGround
    private lateinit var mHexa:MyLitHexa
    private lateinit var mCube0:MyLitCube
    private lateinit var mCube:MyLitTexCube
    private lateinit var skyBox: SkyBox
    private lateinit var cubeMap: CubeMap


    var alpha = 0.001f

    fun time_manage(){
        time+=alpha
//        Log.d("시간 : ",time.toString())
        if(time>1f)
            alpha = -0.001f
        else if(time<0)
            alpha = 0.001f

        var diff_value = time
        if(time < 0.4f) diff_value = 0.4f
        else diff_value = time

        for(i in 0..2){
            lightDiffuse[i] = diff_value
            lightSpecular[i] = diff_value
        }
    }
    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES30.glClearColor(0.2f,0.2f,0.2f,1f)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)

        mGround = MyLitTexGround(context)
//        mGround2 = MyLitGround(context)
        mHexa = MyLitHexa(context)
        mCube0 = MyLitCube(context)
        mCube = MyLitTexCube(context)
        mySensors = MySensors(context)
        skyBox = SkyBox(context)
        cubeMap = CubeMap(context)

        mySensors.startListening { x, y, z ->
            cameraMove(x,y)
        }
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0,0,width,height)

        val ratio = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix,0, 90f, ratio, 0.001f, 1000f)

        Matrix.setLookAtM(viewMatrix, 0,eyePos[0],eyePos[1],eyePos[2],eyeAt[0], eyeAt[1], eyeAt[2],0f,1f, 0f)

        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    override fun onDrawFrame(p0: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        val endTime = SystemClock.uptimeMillis()
        val angle = 0.1f * (endTime- startTime).toFloat()
        startTime = endTime
        rotYAngle += angle

        spinDegree += angle*0.1f
        var rotYMatrix = FloatArray(16).apply {Matrix.setIdentityM(this, 0)}
        Matrix.rotateM(rotYMatrix, 0, rotYAngle, 0f, 1f, 0f)

        var rotMatrix = FloatArray(16).apply {Matrix.setIdentityM(this, 0)}
        Matrix.rotateM(rotMatrix, 0, 45f, 0f, 0f, 1f)
        Matrix.multiplyMM(rotMatrix, 0, rotYMatrix, 0, rotMatrix, 0)

        skyViewMatrix = viewMatrix.clone()
        Matrix.rotateM(skyViewMatrix, 0, spinDegree, 0f,1f,0f)
        skyBox.draw(skyViewMatrix, projectionMatrix, time)

        eyeAt[0] = eyePos[0] + cameraVec[0]
//        eyeAt[1] = eyePos[1] + cameraVec[1]
        eyeAt[2] = eyePos[2] + cameraVec[2]
        Matrix.setLookAtM(viewMatrix, 0,eyePos[0],eyePos[1],eyePos[2],eyeAt[0], eyeAt[1], eyeAt[2],0f,1f, 0f)
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix,0, viewMatrix,0)

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0f, 2f, 2f)
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
//        mCube0.draw(mvpMatrix, modelMatrix)
        cubeMap.draw(mvpMatrix, modelMatrix)

//        Matrix.setIdentityM(modelMatrix, 0)
//        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
//        mGround.draw(vpMatrix, modelMatrix)
//        mGround2.draw(vpMatrix, modelMatrix)


//        lightDir[0] = sin(rotYAngle*0.01f)
//        lightDir[2] = cos(rotYAngle*0.01f)

        val posX:Float
        if(outwardDir)
            posX = prevPosX + angle * 0.01f
        else
            posX = prevPosX - angle * 0.01f
        if(posX > 9)
            outwardDir = false
        else if(posX<1)
            outwardDir = true
        prevPosX = posX

        var objectId = 0
        for(z in -7..0 step 3){
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, posX, 0f, z.toFloat())
            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
            mHexa.draw(mvpMatrix, modelMatrix)
            objectPos[objectId++][0] = posX

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, -posX, 0f, z.toFloat())
            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
            mHexa.draw(mvpMatrix, modelMatrix)
            objectPos[objectId++][0] = -posX

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, posX, 1.5f, z.toFloat())
            Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
            mCube.draw(mvpMatrix, modelMatrix)

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, -posX, 1.5f, z.toFloat())
            Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
            mCube.draw(mvpMatrix, modelMatrix)
        }

        if(detectCollision(eyePos[0],eyePos[2])){
            eyePos[0] = 0f
            eyePos[1] = 3f
            eyePos[2] = 3f
            cameraVec[0] = 0f
            cameraVec[1] = -0.7071f
            cameraVec[2] = -0.7071f

            act.showPopup()
        }
        time_manage()
    }
}

fun loadShader(type:Int, filename:String, myContext: Context):Int{
    var temp = GLES30.glCreateShader(type).also{
            shader ->

        val inputStream = myContext.assets.open(filename)
        val inputBuffer = ByteArray(inputStream.available())
        inputStream.read(inputBuffer);
        val shaderCode = String(inputBuffer)

        GLES30.glShaderSource(shader, shaderCode)
        GLES30.glCompileShader(shader)

        val compiled = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer()
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled)
        if(compiled.get(0) == 0){
            GLES30.glGetShaderiv(shader, GLES30.GL_INFO_LOG_LENGTH, compiled)
            if(compiled.get(0)>1){
                Log.e("Shader1", "$type shader compile error")
            }
            GLES30.glDeleteShader(shader)
            Log.e("Shader2", "$type shader compile error")
        }
    }

    return temp
}

fun loadBitmap(filename:String, myContext:Context): Bitmap {
    val manager = myContext.assets
    val inputStream = BufferedInputStream(manager.open(filename))
    val bitmap : Bitmap? = BitmapFactory.decodeStream(inputStream)
    return bitmap!!
}

fun cameraRoatate(theta:Float){
    val sinTheta = sin(theta)
    val cosTheta = cos(theta)
    val newVecZ = cosTheta * cameraVec[2] - sinTheta * cameraVec[0]
    val newVecX = sinTheta * cameraVec[2] + cosTheta * cameraVec[0]
    cameraVec[0] = newVecX
    cameraVec[2] = newVecZ
}

fun cameraMove(distanceX:Float, distanceZ:Float){
//    val newPosX = eyePos[0] + distanceX * cameraVec[0]
//    val newPosZ = eyePos[2] + distanceZ * cameraVec[2]

    val newPosX = eyePos[0] - distanceX
    val newPosZ = eyePos[2] + distanceZ
    if(!detectCollision(newPosX, newPosZ)) {
        eyePos[0] = newPosX
        eyePos[2] = newPosZ
    }
}

fun camera_look_up(a:Float){
    eyeAt[1]+=a
}

fun detectCollision(newPosX:Float, newPosZ:Float):Boolean{
//    if(newPosX < -10 || newPosX > 10 || newPosZ < -10 || newPosZ > 10){
//        return true
//    }

    for(i in 0..objectPos.size-1){
        if(abs(newPosX - objectPos[i][0]) < 1.0 && abs(newPosZ - objectPos[i][2]) < 1.0){
            Log.d("collision", "collision!!!($newPosX, 0, $newPosZ)")
            return true
        }
    }

    return false
}


