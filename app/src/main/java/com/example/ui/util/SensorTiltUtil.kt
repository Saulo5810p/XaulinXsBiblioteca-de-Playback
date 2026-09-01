package com.example.ui.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberDeviceTilt(): State<Pair<Float, Float>> {
    val context = LocalContext.current
    var rawTiltX by remember { mutableStateOf(0f) }
    var rawTiltY by remember { mutableStateOf(0f) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = event.values[0] // -10 to +10 m/s^2
                    val y = event.values[1] // -10 to +10 m/s^2

                    // Normalize tilt values between -8 and +8 degrees
                    rawTiltX = (x / 9.81f * 12f).coerceIn(-12f, 12f)
                    rawTiltY = (y / 9.81f * 12f).coerceIn(-12f, 12f)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    val animatedX by animateFloatAsState(
        targetValue = rawTiltX,
        animationSpec = spring(stiffness = 200f),
        label = "tiltX"
    )
    val animatedY by animateFloatAsState(
        targetValue = rawTiltY,
        animationSpec = spring(stiffness = 200f),
        label = "tiltY"
    )

    return remember(animatedX, animatedY) {
        derivedStateOf { Pair(animatedX, animatedY) }
    }
}
