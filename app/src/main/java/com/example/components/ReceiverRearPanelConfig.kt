package com.example.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import com.example.ui.effects.globalCinematicClickable

@Composable
fun ReceiverRearPanelConfig(
    accentColor: Color,
    onSelectThemePreset: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedInput by remember { mutableStateOf("AUX / AUDIOPHILE") }
    var selectedSpeakers by remember { mutableStateOf("SPEAKERS A + B") }

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
                    Icon(Icons.Default.Settings, contentDescription = null, tint = accentColor)
                    Text(
                        text = "PAINEL TRASEIRO DO RECEIVER",
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
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Gold RCA Input Jacks Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0F1216))
                        .border(1.5.dp, MetallicBorder, RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "ENTRADAS DE ÁUDIO RCA (INPUT SELECTOR)",
                            color = TextMetallicMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf("PHONO", "CD PLAYER", "TAPE DECK", "AUX / AUDIOPHILE").forEach { input ->
                                val isSelected = input == selectedInput
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.globalCinematicClickable(
                                        profile = CinematicProfile.LIST_ITEM,
                                        onClick = { selectedInput = input }
                                    )
                                ) {
                                    // RCA Pair (Red/White)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        RcaJackDot(color = Color(0xFFE53935), isSelected = isSelected)
                                        RcaJackDot(color = Color(0xFFECEFF1), isSelected = isSelected)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = input,
                                        color = if (isSelected) accentColor else TextMetallicMuted,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                // Speaker Terminals A + B
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0F1216))
                        .border(1.5.dp, MetallicBorder, RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "SAÍDA DE CAIXAS (SPEAKER IMPEDANCE 4-16 OHMS)",
                            color = TextMetallicMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            listOf("SPEAKERS A", "SPEAKERS B", "SPEAKERS A + B").forEach { spk ->
                                val isSelected = spk == selectedSpeakers
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSelected) accentColor.copy(alpha = 0.2f) else MetalCardSurface)
                                        .border(1.dp, if (isSelected) accentColor else MetallicBorder, RoundedCornerShape(4.dp))
                                        .globalCinematicClickable(
                                            profile = CinematicProfile.LIST_ITEM,
                                            onClick = { selectedSpeakers = spk }
                                        )
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = spk,
                                        color = if (isSelected) accentColor else TextMetallicLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                // Color Backlight Theme Presets (Technics Blue, Amber Vintage, Emerald Green)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "MATRIZ DE BACKLIGHT DO RECEIVER",
                        color = TextMetallicMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "AMBER VINTAGE" to Color(0xFFFFB300),
                            "TECHNICS BLUE" to Color(0xFF00E5FF),
                            "EMERALD GREEN" to Color(0xFF00E676)
                        ).forEach { (name, color) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(color.copy(alpha = 0.25f))
                                    .border(1.5.dp, color, RoundedCornerShape(4.dp))
                                    .globalCinematicClickable(
                                        profile = CinematicProfile.LIST_ITEM,
                                        onClick = { onSelectThemePreset(name) }
                                    )
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = name,
                                    color = color,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
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
                Text("FECHAR PAINEL TRASEIRO", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MetalPanelSurface,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun RcaJackDot(color: Color, isSelected: Boolean) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.5.dp, if (isSelected) BrassGold else Color.Gray, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color.Black)
        )
    }
}
