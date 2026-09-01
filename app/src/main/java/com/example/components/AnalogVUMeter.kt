package com.example.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AnalogVUMeter(
    levelLeft: Float,  // 0f to 1f
    levelRight: Float, // 0f to 1f
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(MetalPanelSurface)
            .border(2.dp, MetallicBorder, RoundedCornerShape(6.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SingleVUMeterGauge(
            level = levelLeft,
            label = "VU L",
            modifier = Modifier.weight(1f)
        )
        SingleVUMeterGauge(
            level = levelRight,
            label = "VU R",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SingleVUMeterGauge(
    level: Float,
    label: String,
    modifier: Modifier = Modifier
) {
    val animatedLevel by animateFloatAsState(
        targetValue = level.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.65f),
        label = "vuNeedle"
    )

    Box(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFCC9E48), // Sleek Vintage Backlit Amber Yellow
                        Color(0xFFA67C2E),
                        Color(0xFF7A571A)
                    )
                )
            )
            .border(1.dp, BrassGold.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val pivot = Offset(width / 2f, height * 1.3f)
            val needleLength = height * 1.05f

            // Scale Arc (-45 deg to +45 deg)
            val minAngleDeg = -45f
            val maxAngleDeg = 45f
            val currentAngleDeg = minAngleDeg + animatedLevel * (maxAngleDeg - minAngleDeg)

            // Draw dB Scale ticks
            val dbLabels = listOf("-20", "-10", "-7", "-5", "-3", "0", "+1", "+3")
            val dbCount = dbLabels.size

            for (i in 0 until dbCount) {
                val fraction = i / (dbCount - 1).toFloat()
                val angleDeg = minAngleDeg + fraction * (maxAngleDeg - minAngleDeg) - 90f
                val angleRad = Math.toRadians(angleDeg.toDouble())

                val isRedZone = fraction >= 0.7f
                val tickColor = if (isRedZone) Color(0xFFD32F2F) else Color(0xFF3E2723)

                val innerR = needleLength * 0.75f
                val outerR = needleLength * 0.88f

                val start = Offset(
                    pivot.x + innerR * kotlin.math.cos(angleRad).toFloat(),
                    pivot.y + innerR * kotlin.math.sin(angleRad).toFloat()
                )
                val end = Offset(
                    pivot.x + outerR * kotlin.math.cos(angleRad).toFloat(),
                    pivot.y + outerR * kotlin.math.sin(angleRad).toFloat()
                )

                drawLine(
                    color = tickColor,
                    start = start,
                    end = end,
                    strokeWidth = if (i == 5 || isRedZone) 2.5.dp.toPx() else 1.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Draw Red Warning Zone Arc (0dB to +3dB)
            drawArc(
                color = Color(0xFFE53935),
                startAngle = -90f + minAngleDeg + 0.7f * (maxAngleDeg - minAngleDeg),
                sweepAngle = 0.3f * (maxAngleDeg - minAngleDeg),
                useCenter = false,
                topLeft = Offset(pivot.x - needleLength * 0.85f, pivot.y - needleLength * 0.85f),
                size = Size(needleLength * 1.7f, needleLength * 1.7f),
                style = Stroke(width = 3.dp.toPx())
            )

            // Draw Needle
            val needleRad = Math.toRadians((currentAngleDeg - 90f).toDouble())
            val needleTip = Offset(
                pivot.x + needleLength * kotlin.math.cos(needleRad).toFloat(),
                pivot.y + needleLength * kotlin.math.sin(needleRad).toFloat()
            )

            drawLine(
                color = VUNeedleRed,
                start = pivot,
                end = needleTip,
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Needle Pivot Pin Base
            drawCircle(
                color = Color(0xFF212121),
                radius = 8.dp.toPx(),
                center = pivot
            )
            drawCircle(
                color = Color(0xFFD4AF37),
                radius = 4.dp.toPx(),
                center = pivot
            )

            // Glass Reflection Overlay Highlight
            val glassPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(width, 0f)
                lineTo(width, height * 0.35f)
                lineTo(0f, height * 0.15f)
                close()
            }
            drawPath(
                path = glassPath,
                color = Color.White.copy(alpha = 0.18f)
            )
        }

        Text(
            text = label,
            color = Color(0xFF3E2723),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 6.dp, top = 4.dp)
        )

        Text(
            text = "VU",
            color = Color(0xFF5D4037),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 6.dp, bottom = 4.dp)
        )
    }
}
