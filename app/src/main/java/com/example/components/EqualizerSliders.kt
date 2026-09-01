package com.example.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EqualizerBand
import com.example.ui.theme.*

@Composable
fun EqualizerSliders(
    bands: List<EqualizerBand>,
    onBandLevelChanged: (Short, Short) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MetalPanelSurface)
            .border(2.dp, MetallicBorder, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        bands.forEach { band ->
            EqualizerSingleBandFader(
                band = band,
                onLevelChanged = { level -> onBandLevelChanged(band.bandIndex, level) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun EqualizerSingleBandFader(
    band: EqualizerBand,
    onLevelChanged: (Short) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val minLvl = band.minLevelMb.toFloat()
    val maxLvl = band.maxLevelMb.toFloat()
    val currentLvl = band.currentLevelMb.toFloat()

    val freqLabel = when {
        band.centerFrequencyHz >= 1000 -> "${band.centerFrequencyHz / 1000}k"
        else -> "${band.centerFrequencyHz}"
    }

    val dbValueInt = (band.currentLevelMb / 100)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // dB Value readout
        Text(
            text = if (dbValueInt > 0) "+${dbValueInt}dB" else "${dbValueInt}dB",
            color = if (dbValueInt > 0) VintageAmber else TextLcdGreen,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Box(
            modifier = Modifier
                .height(160.dp)
                .width(36.dp),
            contentAlignment = Alignment.Center
        ) {
            // Metallic Vertical Fader Track Line
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF0C0E12))
                    .border(1.dp, MetallicBorder, RoundedCornerShape(2.dp))
            )

            // Rotated Vertical Slider
            Slider(
                value = currentLvl,
                onValueChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onLevelChanged(it.toInt().toShort())
                },
                valueRange = minLvl..maxLvl,
                colors = SliderDefaults.colors(
                    thumbColor = BrassGold,
                    activeTrackColor = VintageAmber,
                    inactiveTrackColor = MetalBevelDark
                ),
                modifier = Modifier
                    .height(36.dp)
                    .width(160.dp)
                    .align(Alignment.Center)
                    // Rotate 270 degrees to make standard horizontal slider vertical
                    .rotate(-90f)
            )
        }

        // Center Frequency Label
        Text(
            text = freqLabel,
            color = TextMetallicLight,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
