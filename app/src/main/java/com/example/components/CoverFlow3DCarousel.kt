package com.example.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Track
import com.example.ui.theme.*
import com.example.ui.util.AlbumArtUtil
import com.example.ui.util.rememberDeviceTilt
import kotlin.math.abs

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.launch

import com.example.ui.util.rememberAlbumArtPainter
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.ui.effects.CinematicProfile
import com.example.ui.effects.SpinBlurIconButton
import com.example.ui.effects.cinematicPressVisuals
import com.example.ui.effects.globalCinematicClickable

@Composable
fun CoverFlow3DCarousel(
    tracks: List<Track>,
    currentTrack: Track?,
    isPlaying: Boolean,
    onTrackSelected: (Track) -> Unit,
    onOpenNowPlaying: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onToggleFavorite: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    if (tracks.isEmpty()) return

    val (tiltX, tiltY) = rememberDeviceTilt().value
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // Initial page index match
    if (tracks.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MetalCardSurface)
                    .border(2.dp, MetallicBorder, RoundedCornerShape(12.dp))
                    .padding(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = VintageAmber,
                    modifier = Modifier.size(52.dp)
                )
                Text(
                    text = "Sem Músicas",
                    color = TextMetallicLight,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Nenhuma música local encontrada no dispositivo. Adicione arquivos MP3/FLAC/WAV.",
                    color = TextMetallicMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        return
    }

    val initialIndex = remember(currentTrack) {
        val idx = tracks.indexOfFirst { it.id == currentTrack?.id }
        if (idx >= 0) idx else 0
    }

    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { tracks.size }
    )

    // Haptic feedback when flipping between album covers in 3D carousel
    LaunchedEffect(pagerState.currentPage) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    // Current Focused Track in 3D Carousel
    val focusedTrack = tracks.getOrNull(pagerState.currentPage) ?: currentTrack ?: tracks.first()
    val focusedStudioColor = AlbumArtUtil.getTrackStudioColor(focusedTrack)

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val availableHeight = maxHeight
        val availableWidth = maxWidth

        // Adaptive enlarged album sleeve size based on available screen space
        val albumSize = (minOf(availableWidth * 0.72f, availableHeight * 0.52f)).coerceIn(130.dp, 350.dp)
        val vinylSize = albumSize * 0.92f

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Full Screen Adaptive 3D CoverFlow Stage with Full Touch & Gesture Area (Over, Above & Below Album Covers)
            var accumulatedDrag by remember { mutableStateOf(0f) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .graphicsLayer {
                        rotationZ = tiltX * 0.35f
                        rotationX = tiltY * 0.35f
                    }
                    .pointerInput(pagerState.currentPage, tracks.size) {
                        detectHorizontalDragGestures(
                            onDragStart = { accumulatedDrag = 0f },
                            onDragEnd = {
                                if (accumulatedDrag < -30f && pagerState.currentPage < tracks.size - 1) {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                } else if (accumulatedDrag > 30f && pagerState.currentPage > 0) {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                    }
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                accumulatedDrag += dragAmount
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Dynamic Background Studio Ambient Aura Glow with Multi-Stop Metallic/Color Radial Gradients
                Box(
                    modifier = Modifier
                        .size(albumSize * 1.55f)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    focusedStudioColor.copy(alpha = 0.55f),
                                    VintageAmber.copy(alpha = 0.20f),
                                    focusedStudioColor.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                        .blur(32.dp)
                )

                // 3D Horizontal Pager
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = availableWidth * 0.22f),
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val track = tracks.getOrNull(page) ?: return@HorizontalPager
                    val isCentered = page == pagerState.currentPage
                    val isTrackPlaying = isCentered && isPlaying && (currentTrack?.id == track.id)

                    // Page Offset for Vivid 3D Perspective Projection
                    val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    val absOffset = abs(pageOffset)

                    // 3D Projection Calculation (High-Depth Android 3D Cover Flow)
                    val rotationYVal = (pageOffset * -58f).coerceIn(-75f, 75f)
                    val scaleVal = (1.18f - (absOffset * 0.28f)).coerceIn(0.60f, 1.25f)
                    val shadowVal = (20f - (absOffset * 14f)).coerceAtLeast(0f)
                    val alphaVal = (1f - (absOffset * 0.32f)).coerceIn(0.35f, 1f)

                    // Vinyl Disc Slide Out
                    val vinylSlideOffset by animateDpAsState(
                        targetValue = if (isCentered) (albumSize * 0.32f) else 0.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "vinylSlide"
                    )

                    // Vinyl Rotation Spin
                    val infiniteTransition = rememberInfiniteTransition(label = "vinylSpin")
                    val rotationAngle by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(3800, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "rotation"
                    )

                    val albumArtRes = AlbumArtUtil.getTrackAlbumArtRes(track)
                    val studioColor = AlbumArtUtil.getTrackStudioColor(track)

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(albumSize * 1.30f)
                            .graphicsLayer {
                                this.rotationY = rotationYVal + (tiltX * 0.55f)
                                this.rotationX = tiltY * 0.35f
                                this.scaleX = scaleVal
                                this.scaleY = scaleVal
                                this.shadowElevation = shadowVal
                                this.alpha = alphaVal
                                this.cameraDistance = 18f * density
                            }
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onTrackSelected(track)
                                onOpenNowPlaying()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Sleeve & Vinyl Assembly Box
                            Box(
                                modifier = Modifier
                                    .size(albumSize),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                // 3D Vinyl Disc
                                Box(
                                    modifier = Modifier
                                        .size(vinylSize)
                                        .offset(x = vinylSlideOffset)
                                        .rotate(if (isTrackPlaying) rotationAngle else 0f)
                                        .clip(CircleShape)
                                        .background(Color(0xFF08090C))
                                        .border(
                                            2.dp,
                                            Brush.sweepGradient(
                                                listOf(Color(0xFF4A525D), BrassGold, Color(0xFF282C34), Color(0xFF4A525D))
                                            ),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_vinyl_disc_1785693778020),
                                        contentDescription = "Vinyl Record",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Center Label
                                    Box(
                                        modifier = Modifier
                                            .size(vinylSize * 0.36f)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.radialGradient(
                                                    colors = listOf(studioColor, studioColor.copy(alpha = 0.65f), Color.Black)
                                                )
                                            )
                                            .border(1.dp, BrassGold, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "STUDIO 3D",
                                            color = Color.Black,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                // Album Sleeve Cover Case with Rich Gradient Border
                                Box(
                                    modifier = Modifier
                                        .size(albumSize)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MetalCardSurface)
                                        .border(
                                            width = if (isCentered) 3.5.dp else 1.2.dp,
                                            brush = if (isCentered) {
                                                Brush.linearGradient(
                                                    colors = listOf(BrassGold, studioColor, Color.White, studioColor)
                                                )
                                            } else {
                                                Brush.linearGradient(
                                                    colors = listOf(MetallicBorder, Color(0xFF3E4552))
                                                )
                                            },
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .shadow(
                                            elevation = if (isCentered) 22.dp else 4.dp,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    Image(
                                        painter = rememberAlbumArtPainter(track),
                                        contentDescription = track.album,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Multi-Stop Bevel Glare & Dynamic 3D Reflection Highlight
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(
                                                        Color.White.copy(alpha = 0.22f),
                                                        Color.Transparent,
                                                        Color.Black.copy(alpha = 0.55f)
                                                    )
                                                )
                                            )
                                    )

                                    // Touch / Click Badge on Focused Center Album
                                    if (isCentered) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(
                                                    Brush.horizontalGradient(
                                                        colors = listOf(
                                                            Color.Black.copy(alpha = 0.85f),
                                                            MetalPanelSurface.copy(alpha = 0.90f)
                                                        )
                                                    )
                                                )
                                                .border(
                                                    1.5.dp,
                                                    Brush.horizontalGradient(listOf(BrassGold, studioColor)),
                                                    RoundedCornerShape(20.dp)
                                                )
                                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isTrackPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    tint = studioColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "TOCAR / ESTÚDIO 3D",
                                                    color = TextMetallicLight,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 3D Glass Floor Dynamic Reflection
                            Box(
                                modifier = Modifier
                                    .width(albumSize)
                                    .height(42.dp)
                                    .graphicsLayer {
                                        scaleY = -0.50f
                                        alpha = 0.25f
                                    }
                                    .blur(5.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = albumArtRes),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, MetalDarkBackground)
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Full-Width Studio Information & Deck Action Plate with Gradient Trims
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(MetalPanelSurface, Color(0xFF14171D), Color(0xFF0C0E12))
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(BrassGold, focusedStudioColor, VintageAmber, BrassGold)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .graphicsLayer {
                        rotationZ = tiltX * -0.2f
                    }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(focusedStudioColor)
                            )
                            Text(
                                text = focusedTrack.title.uppercase(),
                                color = TextMetallicLight,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                        }
                        Text(
                            text = "${focusedTrack.artist.uppercase()} — ${focusedTrack.album.uppercase()}",
                            color = focusedStudioColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                        Text(
                            text = "QUALIDADE AUDIÓFILA: ${focusedTrack.codec} • ${focusedTrack.sampleRateHz / 1000}kHz / ${focusedTrack.bitrateKbps}kbps",
                            color = TextLcdGreen,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SpinBlurIconButton(
                            icon = if (focusedTrack.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            profile = CinematicProfile.MEDIA_CONTROL,
                            tint = if (focusedTrack.isFavorite) Color(0xFFFF3B30) else TextMetallicMuted,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MetalCardSurface)
                                .border(1.dp, MetallicBorder, RoundedCornerShape(6.dp)),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onToggleFavorite(focusedTrack)
                            }
                        )

                        val openNowPlayingInteraction = remember { MutableInteractionSource() }
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onTrackSelected(focusedTrack)
                                onOpenNowPlaying()
                            },
                            modifier = Modifier.cinematicPressVisuals(
                                interactionSource = openNowPlayingInteraction,
                                profile = CinematicProfile.HERO_TRANSITION
                            ),
                            interactionSource = openNowPlayingInteraction,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = focusedStudioColor,
                                contentColor = MetalDarkBackground
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInFull,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ABRIR REPRODUÇÃO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
