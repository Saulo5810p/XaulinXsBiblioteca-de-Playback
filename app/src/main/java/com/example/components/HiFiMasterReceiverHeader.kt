package com.example.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.PlaybackState
import com.example.ui.ReceiverTab
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun HiFiMasterReceiverHeader(
    state: PlaybackState,
    selectedTab: ReceiverTab,
    trackCount: Int,
    accentColor: Color,
    tiltX: Float,
    onOpenEqualizer: () -> Unit,
    onOpenControlUnit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // Bootup sequence simulation
    var isBooting by remember { mutableStateOf(true) }
    var bootMessage by remember { mutableStateOf("INITIALIZING RECEIVER...") }

    LaunchedEffect(Unit) {
        delay(250)
        bootMessage = "CALIBRATING STEREO DECK..."
        delay(350)
        bootMessage = "PIONEER & YAMAHA HR-9900 READY"
        delay(400)
        isBooting = false
    }

    // Flashing LED for SCAN activity
    val infiniteTransition = rememberInfiniteTransition(label = "scanLed")
    val scanLedAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanAlpha"
    )

    // Dynamic LCD Text based on state or tab switch
    var lcdStatusText by remember { mutableStateOf("") }
    LaunchedEffect(selectedTab, trackCount) {
        lcdStatusText = "MODULE: ${selectedTab.label}"
        delay(600)
        lcdStatusText = "$trackCount FAIXAS REGISTRADAS NO RACK"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MetalBevelLight,
                        MetalPanelSurface,
                        Color(0xFF0F1216),
                        MetalBevelDark
                    )
                )
            )
            .border(2.dp, Brush.horizontalGradient(listOf(MetallicBorder, accentColor, MetallicBorder)), RoundedCornerShape(10.dp))
            .graphicsLayer { rotationZ = tiltX * 0.2f }
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Top Bar: Logo Branding + Model Name + Screw Rivets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Corner Rivet 1
                RackScrewRivet()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_icon_1785693653769),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, accentColor, CircleShape)
                    )

                    Column {
                        Text(
                            text = "ESTÚDIO DE PLAYBACK 3D",
                            color = TextMetallicLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "PIONEER & YAMAHA RECEIVER HR-9900",
                            color = accentColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Status LEDs: SCAN, PLAYING, QUEUE, HI-RES
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 🔴 SCAN LED
                    StatusLedIndicator(
                        color = Color(0xFFFF3B30),
                        label = "SCAN",
                        isActive = true,
                        alpha = scanLedAlpha
                    )

                    // 🟢 PLAYING LED
                    StatusLedIndicator(
                        color = FluorescentGreen,
                        label = "PLAY",
                        isActive = state.isPlaying
                    )

                    // 🟡 QUEUE LED
                    StatusLedIndicator(
                        color = VintageAmber,
                        label = "QUEUE",
                        isActive = state.queue.isNotEmpty()
                    )

                    // 🔵 HI-RES STEREO LED
                    StatusLedIndicator(
                        color = Color(0xFF00E5FF),
                        label = "HI-RES",
                        isActive = true
                    )
                }

                // Corner Rivet 2
                RackScrewRivet()
            }

            // Middle Display Area: LCD Display Panel + Twin Mini VU Meters + Master Knob
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Intelligent Hi-Fi LCD Display Screen
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF061205))
                        .border(1.5.dp, Color(0xFF13360B), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBooting) "BOOT MODE" else if (state.isPlaying) "NOW PLAYING" else "RECEIVER READY",
                                color = TextLcdAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = state.currentTrack?.codec ?: "FLAC 24-BIT/192KHZ",
                                color = TextLcdGreen,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = if (isBooting) bootMessage else if (state.currentTrack != null) "${state.currentTrack.title} • ${state.currentTrack.artist}" else lcdStatusText,
                            color = TextLcdGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )

                        // LED Spectrum Mini Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MiniEqualizerSpectrumBar(isPlaying = state.isPlaying, amplitude = state.audioAmplitudeLeft)

                            Text(
                                text = "QUARTZ LOCK • HI-FI DIRECT",
                                color = TextLcdDimGreen,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Twin Mini Analog VU Gauge
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .height(58.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFC4983D),
                                    Color(0xFF8A6721),
                                    Color(0xFF5E4310)
                                )
                            )
                        )
                        .border(1.dp, BrassGold, RoundedCornerShape(6.dp))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        HeaderSingleMiniVu(
                            level = if (state.isPlaying) state.audioAmplitudeLeft else 0.05f,
                            label = "L",
                            modifier = Modifier.weight(1f)
                        )
                        HeaderSingleMiniVu(
                            level = if (state.isPlaying) state.audioAmplitudeRight else 0.05f,
                            label = "R",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Master Volume / Control Knob
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MetalBevelLight,
                                        MetalPanelSurface,
                                        MetalBevelDark
                                    )
                                )
                            )
                            .border(1.5.dp, BrassGold, CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onOpenControlUnit()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Controles Receiver",
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = "KNOB HIFI",
                        color = TextMetallicMuted,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun StatusLedIndicator(
    color: Color,
    label: String,
    isActive: Boolean,
    alpha: Float = 1.0f
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(if (isActive) color.copy(alpha = alpha) else Color(0xFF222830))
                .border(1.dp, if (isActive) color else MetallicBorder, CircleShape)
        )
        Text(
            text = label,
            color = if (isActive) color else TextMetallicMuted,
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun RackScrewRivet() {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF5A606C),
                        Color(0xFF22262E),
                        Color(0xFF0F1115)
                    )
                )
            )
            .border(1.dp, Color(0xFF6E7582), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "⊕",
            color = Color(0xFF8E96A4),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HeaderSingleMiniVu(
    level: Float,
    label: String,
    modifier: Modifier = Modifier
) {
    val animatedLevel by animateFloatAsState(
        targetValue = level.coerceIn(0.02f, 1f),
        animationSpec = spring(stiffness = 350f, dampingRatio = 0.6f),
        label = "miniVu"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val pivot = Offset(w / 2f, h * 1.35f)
            val needleLen = h * 1.15f

            val minAngle = -35f
            val maxAngle = 35f
            val angleDeg = minAngle + animatedLevel * (maxAngle - minAngle)
            val angleRad = Math.toRadians((angleDeg - 90f).toDouble())

            val tipX = pivot.x + needleLen * kotlin.math.cos(angleRad).toFloat()
            val tipY = pivot.y + needleLen * kotlin.math.sin(angleRad).toFloat()

            // Draw Needle
            drawLine(
                color = VUNeedleRed,
                start = pivot,
                end = Offset(tipX, tipY),
                strokeWidth = 1.8.dp.toPx()
            )
        }
        Text(
            text = label,
            color = Color.Black,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun MiniEqualizerSpectrumBar(
    isPlaying: Boolean,
    amplitude: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "eqBar")
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(300), RepeatMode.Reverse), label = "b1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(240), RepeatMode.Reverse), label = "b2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(380), RepeatMode.Reverse), label = "b3"
    )
    val bar4 by infiniteTransition.animateFloat(
        initialValue = 0.7f, targetValue = 0.1f,
        animationSpec = infiniteRepeatable(tween(290), RepeatMode.Reverse), label = "b4"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(12.dp)
    ) {
        val bars = if (isPlaying) listOf(bar1, bar2, bar3, bar4, bar2, bar1) else listOf(0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f)
        bars.forEach { heightFactor ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(heightFactor.coerceIn(0.1f, 1f))
                    .clip(RoundedCornerShape(1.dp))
                    .background(FluorescentGreen)
            )
        }
    }
}
