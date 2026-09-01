package com.example.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.ui.effects.CinematicProfile
import com.example.ui.effects.SpinBlurIconButton
import com.example.ui.effects.cinematicPressVisuals
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AudioChannelTestDialog(
    accentColor: Color,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var activeTestMode by remember { mutableStateOf<String?>(null) }
    var testProgressLevel by remember { mutableStateOf(0f) }

    fun runTest(mode: String) {
        scope.launch {
            activeTestMode = mode
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            for (i in 1..20) {
                testProgressLevel = i / 20f
                delay(100)
            }
            delay(1000)
            activeTestMode = null
            testProgressLevel = 0f
        }
    }

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
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = accentColor)
                    Text(
                        text = "GERADOR DE TESTE DE ÁUDIO HI-FI",
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
                // LCD Test Status Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF071206))
                        .border(1.5.dp, Color(0xFF163B0E), RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (activeTestMode != null) "TESTE EM EXECUÇÃO: $activeTestMode" else "SISTEMA PRONTO PARA TESTE DE RESPOSTA",
                            color = TextLcdAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Text(
                            text = if (activeTestMode != null) "INJETANDO SINAL DE SINTONIA 1kHz (0dBFS)" else "Selecione o canal para verificar a calibração de fase e caixa.",
                            color = TextLcdGreen,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        if (activeTestMode != null) {
                            LinearProgressIndicator(
                                progress = { testProgressLevel },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = FluorescentGreen,
                                trackColor = Color(0xFF12280E)
                            )
                        }
                    }
                }

                // Test Channels Buttons Grid
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "CANAL ESQUERDO (LEFT 1kHz)" to "LEFT",
                        "CANAL DIREITO (RIGHT 1kHz)" to "RIGHT",
                        "STEREO AMBOS (1kHz SINE)" to "STEREO",
                        "RUÍDO ROSA (PINK NOISE 20Hz-20kHz)" to "PINK_NOISE"
                    ).forEach { (label, modeKey) ->
                        val isTesting = activeTestMode == modeKey
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isTesting) accentColor.copy(alpha = 0.25f) else MetalCardSurface)
                                .border(1.dp, if (isTesting) accentColor else MetallicBorder, RoundedCornerShape(6.dp))
                                .clickable { runTest(modeKey) }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.GraphicEq, contentDescription = null, tint = if (isTesting) accentColor else TextMetallicMuted)
                                    Text(
                                        text = label,
                                        color = if (isTesting) accentColor else TextMetallicLight,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                if (isTesting) {
                                    Text("TESTANDO...", color = TextLcdAmber, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
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
                Text("CONCLUIR TESTE", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MetalPanelSurface,
        shape = RoundedCornerShape(12.dp)
    )
}
