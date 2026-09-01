package com.example.components

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlaybackState
import com.example.model.Track
import com.example.ui.theme.*
import com.example.ui.util.AlbumArtUtil
import com.example.ui.util.rememberAlbumArtPainter
import com.example.ui.effects.CinematicProfile
import com.example.ui.effects.SpinBlurIconButton
import com.example.ui.effects.globalCinematicClickable

@Composable
fun AudiophileListeningRoom(
    state: PlaybackState,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val currentTrack = state.currentTrack
    val accentColor = AlbumArtUtil.getTrackStudioColor(currentTrack)

    // Vacuum tube filament glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "tubeGlow")
    val tubeGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tubeAlpha"
    )

    // Disc spin animation
    val discRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (state.isPlaying) 3000 else 60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "discSpin"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF141820),
                        Color(0xFF090B0E),
                        Color(0xFF020304)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SpinBlurIconButton(
                        icon = Icons.Default.ArrowBack,
                        contentDescription = "Sair",
                        profile = CinematicProfile.HERO_TRANSITION,
                        tint = TextMetallicLight,
                        onClick = onClose
                    )
                    Column {
                        Text(
                            text = "SALA DE ESCUTA AUDIÓFILA",
                            color = TextMetallicLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "MODO DE ALTA FIDELIDADE PURA • CLASS A VALVULAR",
                            color = VintageAmber,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatusLedIndicator(color = VintageAmber, label = "TUBE", isActive = true, alpha = tubeGlowAlpha)
                    StatusLedIndicator(color = FluorescentGreen, label = "DIRECT", isActive = true)
                }
            }

            // Central Hero Area: Vacuum Tubes + Disc Spinning Turntable
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // Dual Vacuum Tubes Graphic
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VacuumTubeGlowWidget(glowAlpha = tubeGlowAlpha, accentColor = VintageAmber)
                    VacuumTubeGlowWidget(glowAlpha = tubeGlowAlpha, accentColor = VintageAmber)
                }

                // Vinyl/CD Disc Centerpiece
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF2A2D34),
                                    Color(0xFF111317),
                                    Color(0xFF07080A)
                                )
                            )
                        )
                        .border(3.dp, BrassGold, CircleShape)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTogglePlayPause()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Grooves canvas
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        for (r in 35..100 step 8) {
                            drawCircle(
                                color = Color(0xFF3A3E48).copy(alpha = 0.4f),
                                radius = r.dp.toPx(),
                                center = center,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                            )
                        }
                    }

                    // Rotating Artwork Center
                    if (currentTrack != null) {
                        Image(
                            painter = rememberAlbumArtPainter(currentTrack),
                            contentDescription = currentTrack.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(110.dp)
                                .graphicsLayer { rotationZ = discRotation }
                                .clip(CircleShape)
                                .border(2.dp, accentColor, CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(MetalCardSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Album, contentDescription = null, tint = TextMetallicMuted, modifier = Modifier.size(48.dp))
                        }
                    }

                    // Spindle Hole
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F1115))
                            .border(1.5.dp, BrassGold, CircleShape)
                    )
                }

                // Track Title & High-Res Specs
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentTrack?.title ?: "Selecione uma faixa no rack",
                        color = TextMetallicLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (currentTrack != null) "${currentTrack.artist} • ${currentTrack.album}" else "Sala de Escuta Estúdio 3D",
                        color = TextMetallicMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF081406))
                            .border(1.dp, Color(0xFF1B4013), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = currentTrack?.codec ?: "FLAC 24-BIT / 192KHZ UNCOMPRESSED • SNR 118dB",
                            color = TextLcdGreen,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // CRT Oscilloscope & VU Meter Section
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OscilloscopeVisualizer(
                    isPlaying = state.isPlaying,
                    amplitude = state.audioAmplitudeLeft,
                    phosphorColor = FluorescentGreen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp)
                )

                // Master Playback Controls Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MetalPanelSurface)
                        .border(1.5.dp, MetallicBorder, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SpinBlurIconButton(
                        icon = Icons.Default.SkipPrevious,
                        contentDescription = "Anterior",
                        profile = CinematicProfile.MEDIA_CONTROL,
                        tint = TextMetallicLight,
                        iconSize = 32.dp,
                        onClick = onPrevious
                    )

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                            .globalCinematicClickable(
                                profile = CinematicProfile.MEDIA_CONTROL,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onTogglePlayPause()
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = MetalDarkBackground,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    SpinBlurIconButton(
                        icon = Icons.Default.SkipNext,
                        contentDescription = "Próxima",
                        profile = CinematicProfile.MEDIA_CONTROL,
                        tint = TextMetallicLight,
                        iconSize = 32.dp,
                        onClick = onNext
                    )
                }
            }
        }
    }
}

@Composable
fun VacuumTubeGlowWidget(
    glowAlpha: Float,
    accentColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(64.dp)
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x44FFFFFF),
                            accentColor.copy(alpha = glowAlpha * 0.6f),
                            Color(0xFF15181E)
                        )
                    )
                )
                .border(1.5.dp, BrassGold, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 4.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Filament Center Line
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(30.dp)
                    .background(accentColor.copy(alpha = glowAlpha))
            )
        }
        Text(
            text = "6SN7 TUBE",
            color = accentColor,
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}
