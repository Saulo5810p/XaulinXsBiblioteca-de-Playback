package com.example.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.ui.effects.ScrollCinematicProfile
import com.example.ui.effects.cinematicScrollItem
import com.example.ui.effects.globalCinematicClickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Track
import com.example.ui.effects.CinematicProfile
import com.example.ui.effects.SpinBlurIconButton
import com.example.ui.theme.*
import com.example.ui.util.AlbumArtUtil

@Composable
fun StudioTapeDeck(
    allTracks: List<Track>,
    currentTrack: Track?,
    isPlaying: Boolean,
    onTrackSelected: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    if (allTracks.isEmpty()) {
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
                    text = "Nenhuma música local encontrada no dispositivo.",
                    color = TextMetallicMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        return
    }

    val groupedByArtist = remember(allTracks) { allTracks.groupBy { it.artist } }

    val infiniteTransition = rememberInfiniteTransition(label = "tapeReels")
    val reelRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "reelAngle"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High End Reel-to-Reel Master Tape Deck Unit
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MetalBevelLight, MetalPanelSurface, MetalBevelDark)
                    )
                )
                .border(2.dp, MetallicBorder, RoundedCornerShape(10.dp))
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AKAI / TEAC REEL-TO-REEL MASTER TAPE DECK",
                        color = TextMetallicLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (isPlaying) "GRAVANDO / REPRODUZINDO" else "PARADO",
                        color = if (isPlaying) FluorescentGreen else TextMetallicMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Dual Reel Spools Canvas Display
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0C0E12))
                        .border(1.5.dp, MetallicBorder, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Supply Reel
                    TapeReelSpool(
                        rotationAngle = if (isPlaying) reelRotation else 0f,
                        isSupply = true
                    )

                    // Tape Head Assembly Indicator
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(BrassGold)
                        )
                        Text(
                            text = "38 CM/S",
                            color = VintageAmber,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Right Take-Up Reel
                    TapeReelSpool(
                        rotationAngle = if (isPlaying) reelRotation else 0f,
                        isSupply = false
                    )
                }
            }
        }

        // Artists Section Cards
        Text(
            text = "ARTISTAS DO ESTÚDIO DE GRAVAÇÃO",
            color = VintageAmber,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        val tapeDeckListState = rememberLazyListState()
        LazyColumn(
            state = tapeDeckListState,
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val artistKeys = groupedByArtist.keys.toList()
            itemsIndexed(artistKeys) { index, artistName ->
                val artistTracks = groupedByArtist[artistName] ?: emptyList()
                val firstTrack = artistTracks.firstOrNull() ?: return@itemsIndexed
                val albumArtRes = AlbumArtUtil.getTrackAlbumArtRes(firstTrack)
                val studioColor = AlbumArtUtil.getTrackStudioColor(firstTrack)

                val isArtistPlaying = isPlaying && currentTrack?.artist == artistName

                Box(
                    modifier = Modifier
                        .cinematicScrollItem(
                            lazyListState = tapeDeckListState,
                            index = index,
                            profile = ScrollCinematicProfile.INSANE
                        )
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MetalCardSurface)
                        .border(
                            1.5.dp,
                            if (isArtistPlaying) studioColor else MetallicBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .globalCinematicClickable(
                            profile = com.example.ui.effects.CinematicProfile.LIST_ITEM,
                            onClick = { onTrackSelected(firstTrack) }
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Image(
                                painter = painterResource(id = albumArtRes),
                                contentDescription = artistName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, studioColor, CircleShape)
                            )

                            Column {
                                Text(
                                    text = artistName.uppercase(),
                                    color = TextMetallicLight,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${artistTracks.size} faixas gravadas no estúdio",
                                    color = studioColor,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        SpinBlurIconButton(
                            icon = Icons.Default.PlayArrow,
                            contentDescription = "Tocar Artista",
                            profile = CinematicProfile.MEDIA_CONTROL,
                            tint = MetalDarkBackground,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(studioColor),
                            onClick = { onTrackSelected(firstTrack) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TapeReelSpool(
    rotationAngle: Float,
    isSupply: Boolean
) {
    Box(
        modifier = Modifier
            .size(90.dp)
            .rotate(rotationAngle),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.width / 2f - 4.dp.toPx()

            // Outer Metallic Reel Flange
            drawCircle(
                color = MetalBevelLight,
                radius = outerRadius,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )

            // Inner Tape Pack Circle
            drawCircle(
                color = Color(0xFF2C1B10), // Magnetic Tape Brown
                radius = outerRadius * (if (isSupply) 0.8f else 0.65f),
                center = center
            )

            // 3-Spoke Aluminum Hub Cutouts
            val spokeCount = 3
            for (i in 0 until spokeCount) {
                val angleDeg = i * 120f
                val rad = Math.toRadians(angleDeg.toDouble())
                val endPos = Offset(
                    center.x + (outerRadius * 0.75f) * kotlin.math.cos(rad).toFloat(),
                    center.y + (outerRadius * 0.75f) * kotlin.math.sin(rad).toFloat()
                )

                drawLine(
                    color = MetalBevelLight,
                    start = center,
                    end = endPos,
                    strokeWidth = 3.5.dp.toPx()
                )
            }

            // Center Brass Nut
            drawCircle(
                color = BrassGold,
                radius = 12.dp.toPx(),
                center = center
            )
        }
    }
}
