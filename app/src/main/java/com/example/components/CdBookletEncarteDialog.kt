package com.example.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.ui.effects.CinematicProfile
import com.example.ui.effects.ScrollCinematicProfile
import com.example.ui.effects.cinematicPressVisuals
import com.example.ui.effects.cinematicScrollItem
import com.example.ui.effects.globalCinematicClickable
import com.example.ui.effects.SpinBlurIconButton
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Track
import com.example.ui.theme.*
import com.example.ui.util.AlbumArtUtil
import com.example.ui.util.rememberAlbumArtPainter

@Composable
fun CdBookletEncarteDialog(
    albumName: String,
    tracksInAlbum: List<Track>,
    currentTrack: Track?,
    isPlaying: Boolean,
    onTrackSelected: (Track) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPage by remember { mutableStateOf(0) } // 0 = Capa Frontal + CD, 1 = Contracapa + Faixas
    val representativeTrack = tracksInAlbum.firstOrNull()
    val accentColor = AlbumArtUtil.getTrackStudioColor(representativeTrack)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Album, contentDescription = null, tint = accentColor)
                    Text(
                        text = "ENCARTE FÍSICO DO CD",
                        color = TextMetallicLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                SpinBlurIconButton(
                    icon = Icons.Default.Close,
                    contentDescription = "Fechar",
                    profile = CinematicProfile.DIALOG_CLOSE,
                    tint = TextMetallicMuted,
                    onClick = onDismiss
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Page Toggle Buttons (Front Cover vs Back Cover)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (selectedPage == 0) accentColor.copy(alpha = 0.25f) else MetalCardSurface)
                            .border(1.dp, if (selectedPage == 0) accentColor else MetallicBorder, RoundedCornerShape(4.dp))
                            .clickable { selectedPage = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📀 DISCO & CAPA",
                            color = if (selectedPage == 0) accentColor else TextMetallicLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (selectedPage == 1) accentColor.copy(alpha = 0.25f) else MetalCardSurface)
                            .border(1.dp, if (selectedPage == 1) accentColor else MetallicBorder, RoundedCornerShape(4.dp))
                            .clickable { selectedPage = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📜 CONTRACAPA & CRÉDITOS",
                            color = if (selectedPage == 1) accentColor else TextMetallicLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                if (selectedPage == 0) {
                    // Page 1: Jewel Case Front Cover + CD Disc Ejection Visual
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0C0E12))
                                .border(2.dp, MetallicBorder, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Front Cover Art
                                if (representativeTrack != null) {
                                    Image(
                                        painter = rememberAlbumArtPainter(representativeTrack),
                                        contentDescription = albumName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(150.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .border(1.5.dp, accentColor, RoundedCornerShape(4.dp))
                                    )
                                }

                                // CD Compact Disc Media Visual
                                Box(
                                    modifier = Modifier
                                        .size(130.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFFE2E6EE),
                                                    Color(0xFF9AA0AC),
                                                    Color(0xFF4A505C)
                                                )
                                            )
                                        )
                                        .border(2.dp, BrassGold, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF0C0E12))
                                            .border(1.5.dp, Color(0xFF9AA0AC), CircleShape)
                                    )
                                }
                            }
                        }

                        // Album Details Footer
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = albumName,
                                color = TextMetallicLight,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ARTISTA: ${representativeTrack?.artist ?: "Estúdio Hi-Fi"} • ANO: 2026",
                                color = TextMetallicMuted,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "GRAVADORA: PIONEER & YAMAHA HR AUDIOPHILE LABS",
                                color = TextLcdAmber,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                } else {
                    // Page 2: Back Cover + Tracklist
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "FAIXAS REGISTRADAS NO ENCARTE (${tracksInAlbum.size})",
                            color = TextLcdGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        val encarteListState = rememberLazyListState()
                        LazyColumn(
                            state = encarteListState,
                            modifier = Modifier.height(200.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            itemsIndexed(tracksInAlbum) { index, trk ->
                                val isCurrent = currentTrack?.id == trk.id
                                Row(
                                    modifier = Modifier
                                        .cinematicScrollItem(
                                            lazyListState = encarteListState,
                                            index = index,
                                            profile = ScrollCinematicProfile.INSANE
                                        )
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isCurrent) accentColor.copy(alpha = 0.2f) else MetalCardSurface)
                                        .border(1.dp, if (isCurrent) accentColor else MetallicBorder, RoundedCornerShape(4.dp))
                                        .globalCinematicClickable(
                                            profile = CinematicProfile.LIST_ITEM,
                                            onClick = {
                                                onTrackSelected(trk)
                                                onDismiss()
                                            }
                                        )
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = String.format("%02d.", index + 1),
                                            color = accentColor,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = trk.title,
                                            color = TextMetallicLight,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    if (isCurrent && isPlaying) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val confirmInteraction = remember { MutableInteractionSource() }
            Button(
                onClick = onDismiss,
                modifier = Modifier.cinematicPressVisuals(
                    interactionSource = confirmInteraction,
                    profile = CinematicProfile.HERO_TRANSITION
                ),
                interactionSource = confirmInteraction,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = MetalDarkBackground),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("FECHAR ENCARTE", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MetalPanelSurface,
        shape = RoundedCornerShape(12.dp)
    )
}
