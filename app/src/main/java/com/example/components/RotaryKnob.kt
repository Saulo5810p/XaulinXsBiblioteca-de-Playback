package com.example.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlin.math.abs
import kotlin.math.atan2

@Composable
fun RotaryKnob(
    value: Float, // 0f to 1f
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    knobColor: Color = MetalBevelLight,
    accentColor: Color = VintageAmber
) {
    val haptic = LocalHapticFeedback.current
    var lastHapticStep by remember { mutableStateOf((value * 20).toInt()) }

    // Map value (0f..1f) to angle range (-135 deg to +135 deg)
    val targetAngle = (value * 270f) - 135f
    val animatedAngle by animateFloatAsState(targetValue = targetAngle, label = "knobAngle")

    Box(
        modifier = modifier
            .size(size)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
                    val touchPos = change.position
                    val angleRad = atan2(touchPos.y - center.y, touchPos.x - center.x)
                    var deg = Math.toDegrees(angleRad.toDouble()).toFloat() + 90f
                    if (deg > 180f) deg -= 360f

                    // Clamp angle between -135 and +135 degrees
                    val clampedDeg = deg.coerceIn(-135f, 135f)
                    val newValue = (clampedDeg + 135f) / 270f

                    val currentStep = (newValue * 20).toInt()
                    if (currentStep != lastHapticStep) {
                        lastHapticStep = currentStep
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }

                    onValueChange(newValue)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.width / 2f - 8.dp.toPx()

            // Outer Tick Marks Ring
            val tickCount = 21
            for (i in 0 until tickCount) {
                val tickPercent = i / (tickCount - 1).toFloat()
                val tickAngleDeg = (tickPercent * 270f) - 135f - 90f
                val tickRad = Math.toRadians(tickAngleDeg.toDouble())
                val isMajor = i % 5 == 0

                val innerR = radius + 2.dp.toPx()
                val outerR = radius + if (isMajor) 7.dp.toPx() else 4.dp.toPx()

                val start = Offset(
                    center.x + innerR * kotlin.math.cos(tickRad).toFloat(),
                    center.y + innerR * kotlin.math.sin(tickRad).toFloat()
                )
                val end = Offset(
                    center.x + outerR * kotlin.math.cos(tickRad).toFloat(),
                    center.y + outerR * kotlin.math.sin(tickRad).toFloat()
                )

                drawLine(
                    color = if (tickPercent <= value) accentColor else TextMetallicMuted,
                    start = start,
                    end = end,
                    strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Outer Metallic Rim Gradient
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MetalBevelLight,
                        MetalPanelSurface,
                        MetalBevelDark
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // Inner Metallic Body
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF4E5460),
                        Color(0xFF23272E),
                        Color(0xFF14161A)
                    ),
                    start = Offset(center.x - radius, center.y - radius),
                    end = Offset(center.x + radius, center.y + radius)
                ),
                radius = radius - 3.dp.toPx(),
                center = center
            )

            // Bevel Ring
            drawCircle(
                color = MetallicBorder,
                radius = radius - 3.dp.toPx(),
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Notch Position Indicator
            val notchAngleRad = Math.toRadians((animatedAngle - 90f).toDouble())
            val notchDistance = radius - 10.dp.toPx()
            val notchCenter = Offset(
                center.x + notchDistance * kotlin.math.cos(notchAngleRad).toFloat(),
                center.y + notchDistance * kotlin.math.sin(notchAngleRad).toFloat()
            )

            drawCircle(
                color = accentColor,
                radius = 3.5.dp.toPx(),
                center = notchCenter
            )
        }
    }
}
