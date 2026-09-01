package com.example.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.model.Track
import com.example.ui.effects.CinematicProfile
import com.example.ui.effects.SpinBlurIconButton
import com.example.ui.effects.globalCinematicClickable
import com.example.ui.theme.*
import com.example.ui.util.rememberAlbumArtPainter

@Composable
fun StudioTrackItemCard(
    track: Track,
    trackIndex: Int,
    isCurrentTrack: Boolean,
    isPlaying: Boolean,
    trackStudioColor: Color,
    onTrackSelected: (Track) -> Unit,
    onToggleFavorite: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 1dp press down effect on tap
    val pressOffsetY by animateFloatAsState(
        targetValue = if (isPressed) 3f else 0f,
        label = "pressOffsetY"
    )

    val formattedIndex = String.format("TRK %02d", trackIndex + 1)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { translationY = pressOffsetY }
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        if (isCurrentTrack) MetalCardSurface else MetalPanelSurface,
                        if (isCurrentTrack) Color(0xFF1C2028) else Color(0xFF15181E)
                    )
                )
            )
            .border(
                1.5.dp,
                if (isCurrentTrack) trackStudioColor else MetallicBorder,
                RoundedCornerShape(8.dp)
            )
            .globalCinematicClickable(
                profile = com.example.ui.effects.CinematicProfile.LIST_ITEM,
                onClick = { onTrackSelected(track) }
            )
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // TRK Index Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF071206))
                        .border(1.dp, if (isCurrentTrack) trackStudioColor else Color(0xFF1A3814), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = formattedIndex,
                        color = if (isCurrentTrack) trackStudioColor else TextLcdGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Album Art Frame
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, if (isCurrentTrack) trackStudioColor else MetallicBorder, RoundedCornerShape(6.dp))
                ) {
                    Image(
                        painter = rememberAlbumArtPainter(track),
                        contentDescription = track.album,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isCurrentTrack && isPlaying) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.45f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = trackStudioColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Title & Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        color = if (isCurrentTrack) trackStudioColor else TextMetallicLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        fontFamily = FontFamily.SansSerif
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = track.artist,
                            color = TextMetallicMuted,
                            fontSize = 12.sp,
                            maxLines = 1,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Text(
                            text = "• ${track.codec}",
                            color = TextLcdGreen,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Favorite Button
            SpinBlurIconButton(
                icon = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorito",
                profile = CinematicProfile.MEDIA_CONTROL,
                tint = if (track.isFavorite) Color(0xFFFF3B30) else TextMetallicMuted,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleFavorite(track)
                }
            )
        }
    }
}
