package com.example.ui.dialogs

import androidx.compose.runtime.remember
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.ui.effects.CinematicProfile
import com.example.ui.effects.ScrollCinematicProfile
import com.example.ui.effects.cinematicPressVisuals
import com.example.ui.effects.cinematicScrollItem
import com.example.ui.effects.globalCinematicClickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.EqualizerSliders
import com.example.components.RotaryKnob
import com.example.model.EqualizerBand
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerDialog(
    bands: List<EqualizerBand>,
    presets: List<String>,
    selectedPresetName: String,
    bassBoost: Short,
    virtualizer: Short,
    onBandLevelChanged: (Short, Short) -> Unit,
    onPresetSelected: (Short, String) -> Unit,
    onBassBoostChanged: (Short) -> Unit,
    onVirtualizerChanged: (Short) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MetalPanelSurface,
        scrimColor = MetalDarkBackground.copy(alpha = 0.8f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EQUALIZADOR ANALÓGICO HI-FI",
                    color = TextMetallicLight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                val closeInteraction = remember { MutableInteractionSource() }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.cinematicPressVisuals(
                        interactionSource = closeInteraction,
                        profile = CinematicProfile.HERO_TRANSITION
                    ),
                    interactionSource = closeInteraction
                ) {
                    Text("FECHAR", color = VintageAmber, fontWeight = FontWeight.Bold)
                }
            }

            // Presets Horizontal Selector
            Text(
                text = "PRESETS DE CALIBRAGEM",
                color = TextMetallicMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )

            val presetsListState = rememberLazyListState()
            LazyRow(state = presetsListState, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(presets) { index, preset ->
                    val isSelected = preset == selectedPresetName
                    Box(
                        modifier = Modifier
                            .cinematicScrollItem(
                                lazyListState = presetsListState,
                                index = index,
                                profile = ScrollCinematicProfile.INSANE
                            )
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) VintageAmberGlow else MetalCardSurface)
                            .border(1.dp, if (isSelected) VintageAmber else MetallicBorder, RoundedCornerShape(4.dp))
                            .globalCinematicClickable(
                                profile = CinematicProfile.LIST_ITEM,
                                onClick = { onPresetSelected(index.toShort(), preset) }
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = preset.uppercase(),
                            color = if (isSelected) VintageAmber else TextMetallicLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Graphic Equalizer Faders Component
            EqualizerSliders(
                bands = bands,
                onBandLevelChanged = onBandLevelChanged
            )

            // Bass Boost & Virtualizer Knobs Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MetalCardSurface)
                    .border(1.dp, MetallicBorder, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "BASS BOOST",
                        color = TextMetallicLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    RotaryKnob(
                        value = bassBoost / 1000f,
                        onValueChange = { frac -> onBassBoostChanged((frac * 1000).toInt().toShort()) },
                        size = 65.dp,
                        accentColor = VintageAmber
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "VIRTUALIZER",
                        color = TextMetallicLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    RotaryKnob(
                        value = virtualizer / 1000f,
                        onValueChange = { frac -> onVirtualizerChanged((frac * 1000).toInt().toShort()) },
                        size = 65.dp,
                        accentColor = FluorescentGreen
                    )
                }
            }
        }
    }
}
