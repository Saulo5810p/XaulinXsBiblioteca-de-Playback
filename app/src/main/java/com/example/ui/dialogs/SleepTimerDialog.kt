package com.example.ui.dialogs

import androidx.compose.runtime.remember
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.ui.effects.CinematicProfile
import com.example.ui.effects.cinematicPressVisuals
import com.example.ui.effects.globalCinematicClickable

@Composable
fun SleepTimerDialog(
    remainingSeconds: Int,
    onSetTimer: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "SLEEP TIMER RECEIVER",
                color = TextMetallicLight,
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (remainingSeconds > 0) {
                    val min = remainingSeconds / 60
                    val sec = remainingSeconds % 60
                    Text(
                        text = "TEMPO RESTANTE: ${String.format("%02d:%02d", min, sec)}",
                        color = FluorescentGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                val options = listOf(
                    15 to "15 MINUTOS",
                    30 to "30 MINUTOS",
                    45 to "45 MINUTOS",
                    60 to "60 MINUTOS",
                    0 to "DESATIVAR TIMER"
                )

                options.forEach { (minutes, label) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(MetalCardSurface)
                            .border(1.dp, MetallicBorder, RoundedCornerShape(4.dp))
                            .globalCinematicClickable(
                                profile = CinematicProfile.LIST_ITEM,
                                onClick = {
                                    onSetTimer(minutes)
                                    onDismiss()
                                }
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = label,
                            color = if (minutes == 0) Color(0xFFFF3B30) else VintageAmber,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            val closeInteraction = remember { MutableInteractionSource() }
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.cinematicPressVisuals(
                    interactionSource = closeInteraction,
                    profile = CinematicProfile.HERO_TRANSITION
                ),
                interactionSource = closeInteraction
            ) {
                Text("FECHAR", color = TextMetallicMuted)
            }
        },
        containerColor = MetalPanelSurface
    )
}
