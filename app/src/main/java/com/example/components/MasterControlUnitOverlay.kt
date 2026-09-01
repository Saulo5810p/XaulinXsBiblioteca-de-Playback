package com.example.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.ui.theme.*
import com.example.ui.effects.CinematicProfile
import com.example.ui.effects.cinematicPressVisuals

@Composable
fun MasterControlUnitOverlay(
    onDismiss: () -> Unit,
    onOpenEqualizer: () -> Unit,
    accentColor: Color,
    onOpenAudioTest: () -> Unit = {},
    onOpenRearPanel: () -> Unit = {}
) {
    var volumeLevel by remember { mutableStateOf(0.8f) }
    var balanceLevel by remember { mutableStateOf(0.5f) } // 0f = L, 0.5f = Center, 1f = R
    var speedPreset by remember { mutableStateOf("1.0x (Direct)") }
    var sleepTimerText by remember { mutableStateOf("Desligado") }

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
                    Icon(Icons.Default.SettingsInputComponent, contentDescription = null, tint = accentColor)
                    Text(
                        text = "PAINEL MESTRE RECEIVER",
                        color = TextMetallicLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                RackScrewRivet()
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Volume & Balance Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MetalCardSurface)
                        .border(1.dp, MetallicBorder, RoundedCornerShape(8.dp))
                        .padding(12.dp)
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
                            RotaryKnob(
                                value = volumeLevel,
                                onValueChange = { volumeLevel = it },
                                size = 64.dp,
                                accentColor = accentColor
                            )
                            Text(
                                text = "VOLUME GAIN",
                                color = TextMetallicLight,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${(volumeLevel * 100).toInt()}% (-${((1f - volumeLevel) * 40).toInt()} dB)",
                                color = TextLcdAmber,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f).padding(start = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "BALANÇO STEREO (L / R)",
                                color = TextMetallicMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Slider(
                                value = balanceLevel,
                                onValueChange = { balanceLevel = it },
                                colors = SliderDefaults.colors(
                                    thumbColor = accentColor,
                                    activeTrackColor = accentColor,
                                    inactiveTrackColor = MetalBevelDark
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("LEFT 100%", color = TextLcdGreen, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text("CENTER", color = TextMetallicLight, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text("RIGHT 100%", color = TextLcdGreen, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                // Playback Pitch / Speed Presets
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "VELOCIDADE E PITCH DE REPRODUÇÃO",
                        color = TextMetallicMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("0.75x", "1.0x (Direct)", "1.25x", "1.5x").forEach { speed ->
                            val isSelected = speed == speedPreset
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) accentColor.copy(alpha = 0.25f) else MetalCardSurface)
                                    .border(1.dp, if (isSelected) accentColor else MetallicBorder, RoundedCornerShape(4.dp))
                                    .clickable { speedPreset = speed }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = speed,
                                    color = if (isSelected) accentColor else TextMetallicLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // Equalizer & Timer Shortcuts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val eqInteraction = remember { MutableInteractionSource() }
                    Button(
                        onClick = {
                            onDismiss()
                            onOpenEqualizer()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .cinematicPressVisuals(
                                interactionSource = eqInteraction,
                                profile = CinematicProfile.MEDIA_CONTROL
                            ),
                        interactionSource = eqInteraction,
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = MetalDarkBackground),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("EQUALIZADOR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    val timerInteraction = remember { MutableInteractionSource() }
                    Button(
                        onClick = {
                            sleepTimerText = when (sleepTimerText) {
                                "Desligado" -> "15 MIN"
                                "15 MIN" -> "30 MIN"
                                "30 MIN" -> "60 MIN"
                                else -> "Desligado"
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .cinematicPressVisuals(
                                interactionSource = timerInteraction,
                                profile = CinematicProfile.MEDIA_CONTROL
                            ),
                        interactionSource = timerInteraction,
                        colors = ButtonDefaults.buttonColors(containerColor = MetalCardSurface, contentColor = TextMetallicLight),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MetallicBorder)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp), tint = VintageAmber)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("TIMER: $sleepTimerText", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Rear Receiver Panel & Audio Channel Test Shortcuts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val rearPanelInteraction = remember { MutableInteractionSource() }
                    Button(
                        onClick = {
                            onDismiss()
                            onOpenRearPanel()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .cinematicPressVisuals(
                                interactionSource = rearPanelInteraction,
                                profile = CinematicProfile.MEDIA_CONTROL
                            ),
                        interactionSource = rearPanelInteraction,
                        colors = ButtonDefaults.buttonColors(containerColor = MetalCardSurface, contentColor = TextMetallicLight),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MetallicBorder)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp), tint = accentColor)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PAINEL TRASEIRO", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    val audioTestInteraction = remember { MutableInteractionSource() }
                    Button(
                        onClick = {
                            onDismiss()
                            onOpenAudioTest()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .cinematicPressVisuals(
                                interactionSource = audioTestInteraction,
                                profile = CinematicProfile.MEDIA_CONTROL
                            ),
                        interactionSource = audioTestInteraction,
                        colors = ButtonDefaults.buttonColors(containerColor = MetalCardSurface, contentColor = TextMetallicLight),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MetallicBorder)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp), tint = FluorescentGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("TESTE SINAL ÁUDIO", fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                Text("CONCLUÍDO", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MetalPanelSurface,
        shape = RoundedCornerShape(12.dp)
    )
}
