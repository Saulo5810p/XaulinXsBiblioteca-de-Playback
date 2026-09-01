package com.example.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Track
import com.example.ui.theme.*

@Composable
fun HiFiLibraryStatsCard(
    allTracks: List<Track>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val totalTracksCount = allTracks.size
    val totalDurationMs = allTracks.sumOf { it.durationMs }
    val totalHours = totalDurationMs / (1000 * 60 * 60)
    val totalMinutes = (totalDurationMs % (1000 * 60 * 60)) / (1000 * 60)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF061205))
            .border(1.5.dp, Color(0xFF13360B), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null, tint = TextLcdAmber, modifier = Modifier.size(16.dp))
                    Text(
                        text = "ESTATÍSTICAS DA BIBLIOTECA HI-FI",
                        color = TextLcdAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "STATUS: READY",
                    color = TextLcdGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // 4 Stats Grid Cells
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCell(
                    label = "TOTAL TRACKS",
                    value = String.format("%d FAIXAS", totalTracksCount),
                    color = TextLcdGreen
                )

                StatCell(
                    label = "PLAY TIME",
                    value = String.format("%dh %02dmin", totalHours, totalMinutes),
                    color = TextLcdGreen
                )

                StatCell(
                    label = "LAST IMPORT",
                    value = "04 AUG 2026",
                    color = TextLcdGreen
                )

                StatCell(
                    label = "SNR / THD",
                    value = "118dB / 0.002%",
                    color = TextLcdAmber
                )
            }
        }
    }
}

@Composable
fun StatCell(
    label: String,
    value: String,
    color: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            color = TextLcdDimGreen,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
