package com.example.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.sin

@Composable
fun OscilloscopeVisualizer(
    isPlaying: Boolean,
    amplitude: Float,
    phosphorColor: Color = Color(0xFF33FF33),
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "oscilloscope")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "oscPhase"
    )

    val gridColor = phosphorColor.copy(alpha = 0.15f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF030A03))
            .border(2.dp, Brush.verticalGradient(listOf(MetallicBorder, phosphorColor.copy(alpha = 0.5f), MetallicBorder)), RoundedCornerShape(8.dp))
            .padding(6.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. CRT Oscilloscope Grid Lines (10x8 subdivisions)
            val cols = 10
            val rows = 8
            val colSpacing = w / cols
            val rowSpacing = h / rows

            for (i in 1 until cols) {
                drawLine(
                    color = gridColor,
                    start = Offset(i * colSpacing, 0f),
                    end = Offset(i * colSpacing, h),
                    strokeWidth = 1f
                )
            }
            for (j in 1 until rows) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, j * rowSpacing),
                    end = Offset(w, j * rowSpacing),
                    strokeWidth = 1f
                )
            }

            // Center reticle crosshairs
            drawLine(
                color = phosphorColor.copy(alpha = 0.35f),
                start = Offset(0f, h / 2f),
                end = Offset(w, h / 2f),
                strokeWidth = 1.5f
            )
            drawLine(
                color = phosphorColor.copy(alpha = 0.35f),
                start = Offset(w / 2f, 0f),
                end = Offset(w / 2f, h),
                strokeWidth = 1.5f
            )

            // 2. Phosphor Waveform
            val path = Path()
            val points = 120
            val currentAmp = if (isPlaying) amplitude.coerceIn(0.15f, 1.0f) else 0.03f
            val baseFreq = 3f

            for (i in 0..points) {
                val x = (i.toFloat() / points) * w
                val normX = (i.toFloat() / points) * 2f * Math.PI.toFloat()

                val wave1 = sin(normX * baseFreq + phase)
                val wave2 = sin(normX * (baseFreq * 2.1f) + phase * 1.5f) * 0.4f
                val wave3 = sin(normX * (baseFreq * 4.3f) - phase * 2f) * 0.2f

                val yOffset = (wave1 + wave2 + wave3) * (h * 0.32f * currentAmp)
                val y = (h / 2f) + yOffset

                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            // Outer glow path
            drawPath(
                path = path,
                color = phosphorColor.copy(alpha = 0.3f),
                style = Stroke(width = 6.dp.toPx())
            )
            // Sharp center phosphor trace
            drawPath(
                path = path,
                color = phosphorColor,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Overlay status text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CRT OSCILLOSCOPE HR-200",
                color = phosphorColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = if (isPlaying) "50mV/DIV • 1kHz" else "STANDBY • NO SIGNAL",
                color = phosphorColor.copy(alpha = 0.8f),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
