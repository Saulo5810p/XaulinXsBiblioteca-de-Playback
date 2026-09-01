package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.components.AnalogVUMeter
import com.example.components.LcdDisplay
import com.example.components.MetallicButton
import com.example.components.PlaybackStudioSeekbarView
import com.example.components.RotaryKnob
import com.example.components.VinylTurntable
import com.example.model.PlaybackState
import com.example.model.RepeatMode
import com.example.ui.effects.CinematicProfile
import com.example.ui.effects.CinematicProfiles
import com.example.ui.effects.CinematicShader
import com.example.ui.effects.SpinBlurIconButton
import com.example.ui.effects.globalCinematicClickable
import com.example.ui.theme.*

import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import com.example.ui.util.rememberAlbumArtPainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    state: PlaybackState,
    onBackClick: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleShuffle: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenLyrics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track = state.currentTrack ?: return
    val scrollState = rememberScrollState()
    val haptic = LocalHapticFeedback.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Glassmorphic Deck Custom States
    var isMuted by remember { mutableStateOf(false) }
    var isTubePreampOn by remember { mutableStateOf(true) }
    var isBassBoostOn by remember { mutableStateOf(true) }
    var is45Rpm by remember { mutableStateOf(false) }

    val progressFraction = if (state.totalDurationMs > 0) {
        state.currentPositionMs.toFloat() / state.totalDurationMs.toFloat()
    } else 0f

    // ANIMAÇÃO DE ENTRADA CINEMATOGRÁFICA — giro 720°, motion blur e aberração
    // cromática máximos ao abrir a tela de reprodução (perfil NOW_PLAYING_ENTER).
    val enterSpec = remember { CinematicProfiles.getSpec(CinematicProfile.NOW_PLAYING_ENTER) }
    val enterProgress = remember { Animatable(0f) }
    var enterScreenSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(Unit) {
        enterProgress.snapTo(0f)
        enterProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessVeryLow
            )
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { enterScreenSize = it }
            .graphicsLayer {
                val remaining = 1f - enterProgress.value
                if (remaining > 0.005f) {
                    rotationZ = remaining * enterSpec.maxRotationDegrees
                    rotationY = remaining * 30f
                    val enterScale = 1f + (enterSpec.overshootScale - 1f) *
                        kotlin.math.sin(enterProgress.value * Math.PI.toFloat()).toFloat() * remaining +
                        (1f - remaining)
                    scaleX = enterScale.coerceAtLeast(0.05f)
                    scaleY = enterScale.coerceAtLeast(0.05f)
                    alpha = enterProgress.value.coerceIn(0f, 1f)

                    val w = enterScreenSize.width.toFloat().coerceAtLeast(300f)
                    val h = enterScreenSize.height.toFloat().coerceAtLeast(300f)
                    renderEffect = CinematicShader.createComposeCinematicEffect(
                        width = w,
                        height = h,
                        rotationSpeed = remaining * 4.0f,
                        scaleFactor = enterScale,
                        blurIntensity = remaining * enterSpec.blurMultiplier,
                        chromaticShift = remaining * enterSpec.chromaticMultiplier,
                        vignetteIntensity = remaining * enterSpec.vignetteMultiplier
                    )
                } else {
                    rotationZ = 0f
                    rotationY = 0f
                    scaleX = 1f
                    scaleY = 1f
                    alpha = 1f
                    renderEffect = null
                }
            },
        containerColor = MetalDarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(FluorescentGreen)
                            )
                            Text(
                                text = "STUDIO MASTER 4K ULTRA HI-FI",
                                color = TextMetallicLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "VACUUM TUBE RECEIVER HR-9900",
                            color = VintageAmber,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                navigationIcon = {
                    SpinBlurIconButton(
                        icon = Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        profile = CinematicProfile.HERO_TRANSITION,
                        tint = TextMetallicLight,
                        onClick = onBackClick
                    )
                },
                actions = {
                    SpinBlurIconButton(
                        icon = Icons.Default.Notes,
                        contentDescription = "Letras",
                        profile = CinematicProfile.MEDIA_CONTROL,
                        tint = VintageAmber,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onOpenLyrics()
                        }
                    )
                    SpinBlurIconButton(
                        icon = Icons.Default.Timer,
                        contentDescription = "Sleep Timer",
                        profile = CinematicProfile.MEDIA_CONTROL,
                        tint = if (state.sleepTimerRemainingSec > 0) FluorescentGreen else TextMetallicLight,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onOpenSleepTimer()
                        }
                    )
                    SpinBlurIconButton(
                        icon = Icons.Default.GraphicEq,
                        contentDescription = "Equalizador",
                        profile = CinematicProfile.MEDIA_CONTROL,
                        tint = VintageAmber,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onOpenEqualizer()
                        }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MetalPanelSurface,
                    titleContentColor = TextMetallicLight
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Vacuum Tube Warm Analog Glow Module Header Plate (Sleek Compact Version)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1E222A),
                                Color(0xFF2C323E),
                                Color(0xFF1B1E26)
                            )
                        )
                    )
                    .border(1.dp, MetallicBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Vacuum Tube Warm Glow Lamp
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isTubePreampOn) VintageAmber else Color(0xFF2C3038))
                                .border(1.dp, BrassGold, CircleShape)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "VALVULADO CLASS-A",
                                color = TextMetallicLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "• 192kHz / 32-bit DSD DIRECT",
                                color = FluorescentGreen,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Chassis Screws
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF5A6270))
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF5A6270))
                        )
                    }
                }
            }

            // LCD Glass Digital Display (Music Info with Fluorescent Green Details)
            LcdDisplay(
                track = track,
                isPlaying = state.isPlaying,
                currentPositionMs = state.currentPositionMs,
                totalDurationMs = state.totalDurationMs
            )

            // Dual Analog VU Meter Gauges (Compact Sleek Vintage Backlit Meters)
            AnalogVUMeter(
                levelLeft = if (isMuted) 0f else state.audioAmplitudeLeft,
                levelRight = if (isMuted) 0f else state.audioAmplitudeRight
            )

            // Center Vinyl Turntable Deck
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                VinylTurntable(
                    isPlaying = state.isPlaying && !isMuted,
                    albumArtPainter = rememberAlbumArtPainter(track),
                    trackTitle = track.title,
                    trackArtist = track.artist,
                    size = if (isLandscape) 210.dp else 340.dp,
                    onTogglePlayPause = onTogglePlayPause
                )
            }

            // Primary Playback Control Buttons Row - Aligned directly below the Vinyl Deck
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MetalPanelSurface,
                                Color(0xFF1B1E26),
                                Color(0xFF101217)
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(BrassGold, MetallicBorder, VintageAmber, BrassGold)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(vertical = 12.dp, horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetallicButton(
                        onClick = onToggleRepeat,
                        icon = when (state.repeatMode) {
                            RepeatMode.OFF -> Icons.Default.Repeat
                            RepeatMode.ALL -> Icons.Default.Repeat
                            RepeatMode.ONE -> Icons.Default.RepeatOne
                        },
                        text = when (state.repeatMode) {
                            RepeatMode.OFF -> "REP OFF"
                            RepeatMode.ALL -> "REP ALL"
                            RepeatMode.ONE -> "REP 1"
                        },
                        isActive = state.repeatMode != RepeatMode.OFF,
                        activeLedColor = VintageAmber
                    )

                    MetallicButton(
                        onClick = onPrevious,
                        icon = Icons.Default.SkipPrevious,
                        text = "PREV"
                    )

                    MetallicButton(
                        onClick = onTogglePlayPause,
                        icon = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        text = if (state.isPlaying) "PAUSE" else "PLAY",
                        isActive = state.isPlaying,
                        activeLedColor = FluorescentGreen
                    )

                    MetallicButton(
                        onClick = onNext,
                        icon = Icons.Default.SkipNext,
                        text = "NEXT"
                    )

                    MetallicButton(
                        onClick = onToggleShuffle,
                        icon = Icons.Default.Shuffle,
                        text = "RANDOM",
                        isActive = state.isShuffle,
                        activeLedColor = VintageAmber
                    )
                }
            }

            // High-End 3D Curved 3-Cabinet Audio Module Seekbar (PlaybackStudioSeekbarView)
            PlaybackStudioSeekbarView(
                currentPositionMs = state.currentPositionMs,
                totalDurationMs = state.totalDurationMs,
                onSeekTo = onSeekTo,
                modifier = Modifier.fillMaxWidth()
            )

            // Volume Rotary Knob & Receiver Controls Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MetalPanelSurface)
                    .border(2.dp, MetallicBorder, RoundedCornerShape(10.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "MASTER VOLUME",
                            color = TextMetallicLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        RotaryKnob(
                            value = if (isMuted) 0f else state.volumeLevel,
                            onValueChange = onVolumeChange,
                            size = 85.dp,
                            accentColor = VintageAmber
                        )
                        Text(
                            text = if (isMuted) "MUTED" else "${(state.volumeLevel * 100).toInt()}%",
                            color = if (isMuted) Color.Red else TextLcdAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "PRESET SOUND",
                            color = TextMetallicMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = state.selectedPresetName.uppercase(),
                            color = FluorescentGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MetallicButton(
                                onClick = onToggleFavorite,
                                icon = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                text = if (track.isFavorite) "FAV" else "ADD",
                                isActive = track.isFavorite,
                                activeLedColor = Color(0xFFFF3B30)
                            )
                        }
                    }
                }
            }

            // =========================================================
            // GLASSMORPHIC BOTTOM STUDIO DECK & PHYSICAL SIMULATED BUTTONS
            // WITH PLAQUE: "Made by Saulo R'S Productions Inc"
            // =========================================================
            GlassmorphicStudioDeck(
                isMuted = isMuted,
                onToggleMute = { isMuted = !isMuted },
                isTubePreampOn = isTubePreampOn,
                onToggleTubePreamp = { isTubePreampOn = !isTubePreampOn },
                isBassBoostOn = isBassBoostOn,
                onToggleBassBoost = { isBassBoostOn = !isBassBoostOn },
                is45Rpm = is45Rpm,
                onToggleRpm = { is45Rpm = !is45Rpm }
            )
        }
    }
}

@Composable
fun GlassmorphicStudioDeck(
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    isTubePreampOn: Boolean,
    onToggleTubePreamp: () -> Unit,
    isBassBoostOn: Boolean,
    onToggleBassBoost: () -> Unit,
    is45Rpm: Boolean,
    onToggleRpm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color(0xFF12151C).copy(alpha = 0.85f),
                        Color(0xFF0A0C0F)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        BrassGold,
                        Color.White.copy(alpha = 0.60f),
                        VintageAmber,
                        BrassGold
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Engraved Gold/Brass Metallic Plaque
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF3B2E15),
                            Color(0xFF1E1609),
                            Color(0xFF45361A)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(BrassGold, Color.White, BrassGold)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 18.dp, vertical = 7.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = BrassGold,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Made by Saulo R'S Productions Inc",
                    color = BrassGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
            }
        }

        Text(
            text = "SIMULATED GLASSMORPHIC PHYSICAL CONTROLS",
            color = TextMetallicMuted,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        // Glassmorphic Physical Push Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassmorphicButton(
                text = if (isMuted) "UNMUTE" else "MUTE",
                isActive = isMuted,
                activeColor = Color.Red,
                onClick = onToggleMute
            )

            GlassmorphicButton(
                text = "VALVULA 192K",
                isActive = isTubePreampOn,
                activeColor = VintageAmber,
                onClick = onToggleTubePreamp
            )

            GlassmorphicButton(
                text = "BASS MONSTER",
                isActive = isBassBoostOn,
                activeColor = FluorescentGreen,
                onClick = onToggleBassBoost
            )

            GlassmorphicButton(
                text = if (is45Rpm) "45 RPM" else "33⅓ RPM",
                isActive = is45Rpm,
                activeColor = BrassGold,
                onClick = onToggleRpm
            )
        }
    }
}

@Composable
fun GlassmorphicButton(
    text: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .globalCinematicClickable(
                profile = CinematicProfile.MEDIA_CONTROL,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.verticalGradient(
                    colors = if (isActive) {
                        listOf(
                            activeColor.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.15f),
                            Color.Black.copy(alpha = 0.60f)
                        )
                    } else {
                        listOf(
                            Color.White.copy(alpha = 0.18f),
                            Color.White.copy(alpha = 0.05f),
                            Color.Black.copy(alpha = 0.50f)
                        )
                    }
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        if (isActive) activeColor else Color.White.copy(alpha = 0.6f),
                        Color.White.copy(alpha = 0.2f),
                        if (isActive) activeColor else Color.White.copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isActive) activeColor else Color(0xFF282D36))
            )
            Text(
                text = text,
                color = if (isActive) Color.White else TextMetallicLight,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSec = (ms / 1000).toInt()
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%02d:%02d", min, sec)
}

