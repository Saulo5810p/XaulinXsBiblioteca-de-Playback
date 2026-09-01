package com.example.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Track
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.ui.effects.CinematicProfile
import com.example.ui.effects.cinematicPressVisuals
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VintageRadioTuner(
    allTracks: List<Track>,
    onPlayTrack: (Track) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var isScanning by remember { mutableStateOf(false) }
    var tunedFrequency by remember { mutableStateOf(98.5f) } // 88.0 - 108.0 MHz
    var tunedStationName by remember { mutableStateOf("ESTAÇÃO AUDIÓFILA STEREO HR-99") }

    fun scanRandomStation() {
        if (allTracks.isEmpty()) return
        scope.launch {
            isScanning = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            for (i in 1..15) {
                tunedFrequency = (880..1080).random() / 10f
                delay(80)
            }
            val randomTrack = allTracks.random()
            tunedStationName = "${randomTrack.title} • ${randomTrack.artist}"
            isScanning = false
            onPlayTrack(randomTrack)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E222A),
                        MetalPanelSurface,
                        Color(0xFF0F1216)
                    )
                )
            )
            .border(2.dp, MetallicBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Radio, contentDescription = null, tint = VintageAmber)
                    Text(
                        text = "SINTONIZADOR DE RÁDIO STEREO VINTAGE",
                        color = TextMetallicLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                StatusLedIndicator(
                    color = VintageAmber,
                    label = if (isScanning) "BUSCANDO..." else "LOCKED",
                    isActive = true
                )
            }

            // Glass Tuner Dial Window (88 - 108 MHz)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF071206))
                    .border(1.5.dp, Color(0xFF163B0E), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Scale Ticks (88 to 108 MHz)
                    val minFreq = 88.0f
                    val maxFreq = 108.0f
                    val totalTicks = 40
                    val tickSpacing = w / totalTicks

                    for (i in 0..totalTicks) {
                        val x = i * tickSpacing
                        val isMajor = i % 5 == 0
                        val tickHeight = if (isMajor) h * 0.45f else h * 0.25f

                        drawLine(
                            color = TextLcdDimGreen,
                            start = Offset(x, 0f),
                            end = Offset(x, tickHeight),
                            strokeWidth = if (isMajor) 1.8f else 1f
                        )
                    }

                    // Tuning Needle Position
                    val needleNorm = (tunedFrequency - minFreq) / (maxFreq - minFreq)
                    val needleX = needleNorm.coerceIn(0f, 1f) * w

                    drawLine(
                        color = Color(0xFFFF3B30),
                        start = Offset(needleX, 0f),
                        end = Offset(needleX, h),
                        strokeWidth = 2.5.dp.toPx()
                    )
                }

                // Frequency Display & Station Name
                Column(
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    Text(
                        text = String.format("%.1f MHz FM STEREO", tunedFrequency),
                        color = TextLcdAmber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (isScanning) "PROCURANDO SINAL DE RÁDIO..." else tunedStationName,
                        color = TextLcdGreen,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }
            }

            // Tune Button & Auto Seek
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tuneButtonInteraction = remember { MutableInteractionSource() }
                Button(
                    onClick = { scanRandomStation() },
                    modifier = Modifier.cinematicPressVisuals(
                        interactionSource = tuneButtonInteraction,
                        profile = CinematicProfile.MEDIA_CONTROL
                    ),
                    interactionSource = tuneButtonInteraction,
                    colors = ButtonDefaults.buttonColors(containerColor = VintageAmber, contentColor = MetalDarkBackground),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SINTONIZAR PRÓXIMA ESTAÇÃO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "QUARTZ SYNTHESIZER TUNER",
                    color = TextMetallicMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
