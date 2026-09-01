package com.example.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.ui.effects.CinematicProfile
import com.example.ui.effects.SpinBlurIconButton
import com.example.ui.effects.cinematicPressVisuals
import com.example.ui.effects.globalCinematicClickable

@Composable
fun StudioAlbumCard3D(
    albumName: String,
    tracksInAlbum: List<Track>,
    currentTrack: Track?,
    isPlaying: Boolean,
    onTrackSelected: (Track) -> Unit,
    onTogglePlayPause: () -> Unit,
    onToggleFavorite: (Track) -> Unit,
    onOpenEncarte: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }
    val mainTrack = tracksInAlbum.firstOrNull() ?: return
    val albumArtRes = AlbumArtUtil.getTrackAlbumArtRes(mainTrack)
    val studioColor = AlbumArtUtil.getTrackStudioColor(mainTrack)

    val isThisAlbumPlaying = isPlaying && currentTrack?.album == albumName

    // Continuous Vinyl Spinning
    val infiniteTransition = rememberInfiniteTransition(label = "giantVinylSpin")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Animated Vinyl Slide Out Position
    val vinylSlideOffset by animateDpAsState(
        targetValue = if (isThisAlbumPlaying) 60.dp else 24.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "vinylSlide"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MetalPanelSurface,
                        MetalCardSurface,
                        MetalDarkBackground
                    )
                )
            )
            .border(2.dp, if (isThisAlbumPlaying) studioColor else MetallicBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Album Top Header & Studio Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isThisAlbumPlaying) FluorescentGreen else studioColor)
                    )
                    Text(
                        text = "ÁLBUM MASTER HI-FI",
                        color = studioColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(studioColor.copy(alpha = 0.2f))
                            .border(1.dp, studioColor, RoundedCornerShape(4.dp))
                            .clickable { onOpenEncarte() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "📜 ENCARTE",
                            color = studioColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "${tracksInAlbum.size} FAIXAS • ${mainTrack.codec}",
                        color = TextLcdGreen,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Giant Album Cover & Vinyl Assembly Stage
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                // Studio Stage Ambient Backlight
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    studioColor.copy(alpha = 0.35f),
                                    studioColor.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Album Sleeve & Vinyl Disc
                Box(
                    modifier = Modifier
                        .width(260.dp)
                        .height(220.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    // Animated Vinyl Record
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .offset(x = vinylSlideOffset)
                            .rotate(if (isThisAlbumPlaying) rotationAngle else 0f)
                            .clip(CircleShape)
                            .background(Color(0xFF090A0D))
                            .border(2.dp, Color(0xFF232730), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_vinyl_disc_1785693778020),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Center Vinyl Label
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(studioColor, studioColor.copy(alpha = 0.6f), Color.Black)
                                    )
                                )
                                .border(1.dp, BrassGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "PIONEER",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Huge Album Art Jacket Sleeve
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MetalCardSurface)
                            .border(2.dp, studioColor, RoundedCornerShape(8.dp))
                            .shadow(12.dp, RoundedCornerShape(8.dp))
                            .clickable {
                                if (isThisAlbumPlaying) {
                                    onTogglePlayPause()
                                } else {
                                    onTrackSelected(mainTrack)
                                }
                            }
                    ) {
                        Image(
                            painter = painterResource(id = albumArtRes),
                            contentDescription = albumName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Play Badge Overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f))
                                .border(1.5.dp, studioColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isThisAlbumPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = studioColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            // Album Metadata Title
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = albumName.uppercase(),
                    color = TextMetallicLight,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "ARTISTA: ${mainTrack.artist.uppercase()}",
                    color = studioColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Quick Control Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val playAlbumInteraction = remember { MutableInteractionSource() }
                Button(
                    onClick = {
                        if (isThisAlbumPlaying) {
                            onTogglePlayPause()
                        } else {
                            onTrackSelected(mainTrack)
                        }
                    },
                    modifier = Modifier.cinematicPressVisuals(
                        interactionSource = playAlbumInteraction,
                        profile = CinematicProfile.MEDIA_CONTROL
                    ),
                    interactionSource = playAlbumInteraction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = studioColor,
                        contentColor = MetalDarkBackground
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(
                        imageVector = if (isThisAlbumPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isThisAlbumPlaying) "PAUSAR ÁLBUM" else "REPRODUZIR ÁLBUM",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                SpinBlurIconButton(
                    icon = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expandir Faixas",
                    profile = CinematicProfile.MEDIA_CONTROL,
                    tint = studioColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MetalCardSurface)
                        .border(1.dp, MetallicBorder, RoundedCornerShape(6.dp)),
                    onClick = { isExpanded = !isExpanded }
                )
            }

            // Tracklist Embedded Inside Jacket Box
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MetalDarkBackground)
                        .border(1.dp, MetallicBorder, RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "FAIXAS DO ÁLBUM ENORME",
                        color = TextMetallicMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                    )

                    tracksInAlbum.forEachIndexed { index, track ->
                        val isTrackPlaying = currentTrack?.id == track.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isTrackPlaying) MetalCardSurface else MetalPanelSurface)
                                .border(
                                    1.dp,
                                    if (isTrackPlaying) studioColor else Color.Transparent,
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable { onTrackSelected(track) }
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "${index + 1}.",
                                        color = if (isTrackPlaying) studioColor else TextMetallicMuted,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = track.title,
                                        color = if (isTrackPlaying) studioColor else TextMetallicLight,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${track.durationMs / 1000 / 60}:${String.format("%02d", (track.durationMs / 1000) % 60)}",
                                        color = TextLcdGreen,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )

                                    SpinBlurIconButton(
                                        icon = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorito",
                                        profile = CinematicProfile.LIST_ITEM,
                                        tint = if (track.isFavorite) Color(0xFFFF3B30) else TextMetallicMuted,
                                        iconSize = 16.dp,
                                        modifier = Modifier.size(24.dp),
                                        onClick = { onToggleFavorite(track) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
