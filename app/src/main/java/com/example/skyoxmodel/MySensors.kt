package com.example.skyoxmodel
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class MySensors(private val context: Context) {
    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var sensorEventListener: SensorEventListener? = null
    private var sensorCallback: ((Float, Float, Float) -> Unit)? = null

    var rotationMatrix = floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
    var Degree = floatArrayOf(0f, 0f, 0f)
    var Position = floatArrayOf(0f,3f,3f)
    fun startListening(callback: (Float, Float, Float) -> Unit) {
        sensorCallback = callback

        sensorEventListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // 센서 정확도 변경 시 호출됨
            }

            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = event.values[0] // x축 기울기
                    val y = event.values[1] // y축 기울기
                    val z = event.values[2] // z축 기울기

                    Degree[0] = x
                    Degree[1] = y
                    Degree[2] = z

                    for(i in 0..2){
                        Degree[i]*=0.03f
                    }

                    sensorCallback?.invoke(Degree[0], Degree[1], Degree[2])
                }
            }
        }

        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stopListening() {
        sensorCallback = null
        sensorManager.unregisterListener(sensorEventListener)
        sensorEventListener = null
    }
}
