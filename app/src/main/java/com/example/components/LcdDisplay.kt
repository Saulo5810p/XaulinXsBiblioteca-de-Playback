package com.example.components

import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Track
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun LcdDisplay(
    track: Track?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    totalDurationMs: Long,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF071206)) // Deep LCD Glass
            .border(2.dp, MetallicBorder, RoundedCornerShape(6.dp))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Top Status Bar: STEREO Indicator, Codec, Bitrate, Sample Rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isPlaying) FluorescentGreen else TextLcdDimGreen)
                    )
                    Text(
                        text = "STEREO",
                        color = if (isPlaying) FluorescentGreen else TextLcdDimGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "QUARTZ LOCK • PITCH ±0.0%",
                        color = TextLcdDimGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = track?.codec ?: "FLAC 24-BIT",
                        color = TextLcdAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${track?.sampleRateHz?.div(1000) ?: 96}kHz / ${track?.bitrateKbps ?: 1411}kbps",
                        color = TextLcdGreen,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Middle Main Title & Artist Line
            Text(
                text = track?.title?.uppercase(Locale.getDefault()) ?: "BIBLIOTECA DE PLAYBACK - READY",
                color = TextLcdGreen,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )

            Text(
                text = track?.artist?.uppercase(Locale.getDefault()) ?: "PIONEER & YAMAHA HI-FI RECEIVER",
                color = TextLcdAmber,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Bottom Line: Time counter & Live Spectrum Bar Visualization
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${formatTime(currentPositionMs)} / ${formatTime(totalDurationMs)}",
                    color = TextLcdGreen,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                // Animated Equalizer Mini Spectrum Canvas
                LcdSpectrumBars(isPlaying = isPlaying)
            }
        }
    }
}

@Composable
fun LcdSpectrumBars(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spectrumAnim")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    Canvas(
        modifier = modifier
            .width(80.dp)
            .height(18.dp)
    ) {
        val barCount = 10
        val barWidth = size.width / barCount
        val maxHeight = size.height

        for (i in 0 until barCount) {
            val factor = if (isPlaying) {
                val val1 = kotlin.math.sin((i + 1) * 0.8f + animOffset * 6f)
                ((val1 + 1f) / 2f).coerceIn(0.2f, 1.0f)
            } else {
                0.1f
            }

            val barH = maxHeight * factor
            val x = i * barWidth + 2.dp.toPx()

            drawRect(
                color = if (factor > 0.8f) TextLcdAmber else TextLcdGreen,
                topLeft = androidx.compose.ui.geometry.Offset(x, maxHeight - barH),
                size = androidx.compose.ui.geometry.Size(barWidth - 3.dp.toPx(), barH)
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
