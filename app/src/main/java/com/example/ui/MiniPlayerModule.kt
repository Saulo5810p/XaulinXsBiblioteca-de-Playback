package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.components.LcdSpectrumBars
import com.example.model.PlaybackState
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.ui.effects.cinematicPressVisuals
import com.example.ui.effects.globalCinematicClickable
import com.example.ui.theme.*
import com.example.ui.util.AlbumArtUtil
import com.example.ui.util.rememberDeviceTilt

@Composable
fun MiniPlayerModule(
    state: PlaybackState,
    onOpenNowPlaying: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit = {},
    onSeekTo: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val track = state.currentTrack ?: return
    var isExpanded by remember { mutableStateOf(false) }
    val (tiltX, tiltY) = rememberDeviceTilt().value

    val progressFraction = if (state.totalDurationMs > 0) {
        state.currentPositionMs.toFloat() / state.totalDurationMs.toFloat()
    } else 0f

    val albumArtRes = AlbumArtUtil.getTrackAlbumArtRes(track)
    val studioColor = AlbumArtUtil.getTrackStudioColor(track)

    // Animated panel height expansion
    val targetHeight = if (isExpanded) 310.dp else 78.dp
    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "miniPlayerHeight"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(animatedHeight)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MetalBevelLight,
                        MetalPanelSurface,
                        MetalDarkBackground,
                        Color(0xFF0D0B07)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(BrassGold, Color(0xFFFFF2A1), BrassGold, VintageAmber)
                ),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .shadow(16.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    change.consume()
                    if (dragAmount < -15f && !isExpanded) {
                        isExpanded = true
                    } else if (dragAmount > 15f && isExpanded) {
                        isExpanded = false
                    }
                }
            }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Drag Handle & Metallic Brass Trim Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .globalCinematicClickable(
                        profile = com.example.ui.effects.CinematicProfile.HERO_TRANSITION,
                        onClick = { isExpanded = !isExpanded }
                    )
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brass Screw Accent Left
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(BrassGold)
                        .border(1.dp, Color.Black, CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))

                // Pull-up / Pull-down Handle Pill
                Box(
                    modifier = Modifier
                        .width(46.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(BrassGold, Color(0xFFFFF2A1), BrassGold)
                            )
                        )
                )

                Spacer(modifier = Modifier.width(12.dp))
                // Brass Screw Accent Right
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(BrassGold)
                        .border(1.dp, Color.Black, CircleShape)
                )
            }

            // Compact View Content (Always visible on top bar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Album Art / Vinyl Thumb
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.5.dp, BrassGold, RoundedCornerShape(8.dp))
                        .clickable { onOpenNowPlaying() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = albumArtRes),
                        contentDescription = "Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Subtle Glare
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.White.copy(0.2f), Color.Transparent)
                                )
                            )
                    )
                }

                // Title, Artist & Format Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenNowPlaying() }
                        .padding(horizontal = 10.dp)
                ) {
                    Text(
                        text = track.title,
                        color = TextMetallicLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = track.artist,
                            color = BrassGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Text(
                            text = "• ${track.codec}",
                            color = TextLcdGreen,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Studio Spectrum VU Meter Indicator
                LcdSpectrumBars(
                    isPlaying = state.isPlaying,
                    modifier = Modifier.width(36.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Quick Play/Pause & Expand Toggle
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play/Pause Golden Button with Gyro Tilt
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(MetalBevelLight, MetalCardSurface, MetalBevelDark)
                                )
                            )
                            .border(1.5.dp, BrassGold, RoundedCornerShape(6.dp))
                            .graphicsLayer {
                                rotationZ = tiltX * 0.5f
                                rotationX = tiltY * 0.5f
                            }
                            .clickable { onTogglePlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = BrassGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Expand / Collapse Chevron Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MetalCardSurface)
                            .border(1.dp, MetallicBorder, RoundedCornerShape(6.dp))
                            .clickable { isExpanded = !isExpanded },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = "Expandir/Recolher",
                            tint = BrassGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // High-Contrast Golden Vibrancy Seekbar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Slider(
                    value = progressFraction,
                    onValueChange = { fraction ->
                        val targetMs = (fraction * state.totalDurationMs).toLong()
                        onSeekTo(targetMs)
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = BrassGold,
                        activeTrackColor = BrassGold,
                        inactiveTrackColor = MetalBevelDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .graphicsLayer {
                            rotationZ = tiltX * 0.2f
                        }
                )
            }

            // Expanded Deck Controls (Revealed when pulled up/expanded)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Time Stamps & Audio Specs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(state.currentPositionMs),
                            color = BrassGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Text(
                            text = "RECEIVER HIFI • 24-BIT / 192KHZ",
                            color = TextLcdGreen,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Text(
                            text = formatTime(state.totalDurationMs),
                            color = TextMetallicLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Golden Studio Control Deck Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Track
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MetalCardSurface)
                                .border(1.5.dp, BrassGold, CircleShape)
                                .globalCinematicClickable(
                                    profile = com.example.ui.effects.CinematicProfile.MEDIA_CONTROL,
                                    onClick = onPrevious
                                )
                                .graphicsLayer {
                                    rotationZ = tiltX * 0.6f
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior", tint = BrassGold)
                        }

                        // Play / Pause Master Button
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(BrassGold, Color(0xFFB8860B), Color(0xFF4A3500))
                                    )
                                )
                                .border(2.dp, Color(0xFFFFF5B8), CircleShape)
                                .globalCinematicClickable(
                                    profile = com.example.ui.effects.CinematicProfile.MEDIA_CONTROL,
                                    onClick = onTogglePlayPause
                                )
                                .graphicsLayer {
                                    rotationZ = tiltX * 0.8f
                                    rotationX = tiltY * 0.8f
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause Master",
                                tint = Color.Black,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Next Track
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MetalCardSurface)
                                .border(1.5.dp, BrassGold, CircleShape)
                                .globalCinematicClickable(
                                    profile = com.example.ui.effects.CinematicProfile.MEDIA_CONTROL,
                                    onClick = onNext
                                )
                                .graphicsLayer {
                                    rotationZ = tiltX * 0.6f
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Próximo", tint = BrassGold)
                        }

                        // Full Screen Reproduction Studio Open
                        val openStudioInteraction = remember { MutableInteractionSource() }
                        Button(
                            onClick = onOpenNowPlaying,
                            modifier = Modifier.cinematicPressVisuals(
                                interactionSource = openStudioInteraction,
                                profile = com.example.ui.effects.CinematicProfile.HERO_TRANSITION
                            ),
                            interactionSource = openStudioInteraction,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MetalCardSurface,
                                contentColor = BrassGold
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(BrassGold, VintageAmber)))
                        ) {
                            Icon(Icons.Default.Fullscreen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("VER ESTÚDIO", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
