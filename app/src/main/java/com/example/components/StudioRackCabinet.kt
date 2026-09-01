package com.example.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Track
import com.example.ui.theme.*
import com.example.ui.util.AlbumArtUtil

@Composable
fun StudioRackCabinet(
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

    val groupedByAlbum = remember(allTracks) { allTracks.groupBy { it.album } }
    val albumList = groupedByAlbum.keys.toList()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cabinet Header Title
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MetalBevelLight, MetalPanelSurface, MetalBevelDark)
                    )
                )
                .border(2.dp, MetallicBorder, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Album, contentDescription = null, tint = BrassGold)
                    Text(
                        text = "CABINETE DE DISCOS & FITAS MASTER 3D",
                        color = TextMetallicLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "${groupedByAlbum.size} RACKS AUDIÓFILOS",
                    color = VintageAmber,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Shelf 1: Vinyl Albums Collection Rack (Enlarged Albums)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MetalDarkBackground)
                .border(2.dp, MetallicBorder, RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ESTANTE 1: COLEÇÃO DE VINIS AUDIÓFILOS (DISCOS 3D GRANDES)",
                    color = VintageAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(FluorescentGreen)
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
            ) {
                items(albumList) { albumName ->
                    val albumTracks = groupedByAlbum[albumName] ?: emptyList()
                    val firstTrack = albumTracks.firstOrNull() ?: return@items
                    val isAlbumPlaying = isPlaying && currentTrack?.album == albumName
                    val albumArtRes = AlbumArtUtil.getTrackAlbumArtRes(firstTrack)
                    val studioColor = AlbumArtUtil.getTrackStudioColor(firstTrack)

                    val elevation by animateFloatAsState(
                        targetValue = if (isAlbumPlaying) -24f else 0f,
                        label = "rackLift"
                    )

                    Box(
                        modifier = Modifier
                            .width(220.dp)
                            .height(260.dp)
                            .graphicsLayer {
                                translationY = elevation
                                rotationY = -14f
                                cameraDistance = 14f * density
                            }
                            .clickable { onTrackSelected(firstTrack) }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(195.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MetalCardSurface)
                                    .border(
                                        width = if (isAlbumPlaying) 3.dp else 1.5.dp,
                                        color = if (isAlbumPlaying) studioColor else MetallicBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Image(
                                    painter = painterResource(id = albumArtRes),
                                    contentDescription = albumName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                if (isAlbumPlaying) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(FluorescentGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = MetalDarkBackground,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                // Studio Hi-Fi Badge
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.Black.copy(alpha = 0.75f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${albumTracks.size} MÚSICAS",
                                        color = BrassGold,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = albumName,
                                color = if (isAlbumPlaying) studioColor else TextMetallicLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = firstTrack.artist,
                                color = TextMetallicMuted,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Wooden Hi-Fi Rack Shelf Rail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF8B5A2B), // Deep Walnut Wood
                                Color(0xFF4A2E12)
                            )
                        )
                    )
                    .border(1.dp, BrassGold)
            )
        }

        // Shelf 2: Edições Especiais & Faixas de Estúdio
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MetalDarkBackground)
                .border(2.dp, MetallicBorder, RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ESTANTE 2: EDIÇÕES ESPECIAIS & FAIXAS DE ESTÚDIO",
                    color = VintageAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = BrassGold, modifier = Modifier.size(16.dp))
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
            ) {
                items(allTracks) { track ->
                    val isTrackPlaying = isPlaying && currentTrack?.id == track.id
                    val albumArtRes = AlbumArtUtil.getTrackAlbumArtRes(track)
                    val studioColor = AlbumArtUtil.getTrackStudioColor(track)

                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(240.dp)
                            .clickable { onTrackSelected(track) }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(175.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MetalCardSurface)
                                    .border(
                                        width = if (isTrackPlaying) 3.dp else 1.dp,
                                        color = if (isTrackPlaying) studioColor else MetallicBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Image(
                                    painter = painterResource(id = albumArtRes),
                                    contentDescription = track.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                if (isTrackPlaying) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(FluorescentGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = MetalDarkBackground,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.Black.copy(alpha = 0.8f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = track.codec,
                                        color = TextLcdGreen,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Text(
                                text = track.title,
                                color = if (isTrackPlaying) studioColor else TextMetallicLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = track.artist,
                                color = TextMetallicMuted,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Wooden Hi-Fi Rack Shelf Rail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF8B5A2B), // Deep Walnut Wood
                                Color(0xFF4A2E12)
                            )
                        )
                    )
                    .border(1.dp, BrassGold)
            )
        }
    }
}
