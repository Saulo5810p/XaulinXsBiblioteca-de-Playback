package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Track
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.ui.effects.CinematicProfile
import com.example.ui.effects.cinematicPressVisuals
import com.example.ui.effects.globalCinematicClickable
import com.example.ui.theme.*

@Composable
fun LyricsDialog(
    track: Track,
    onSaveLyrics: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var isEditing by remember(track.id) { mutableStateOf(false) }
    var editableText by remember(track.id, track.lyrics) { mutableStateOf(track.lyrics ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "LETRAS DA FAIXA",
                    color = TextMetallicLight,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = track.title,
                    color = VintageAmber,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        },
        text = {
            if (isEditing) {
                OutlinedTextField(
                    value = editableText,
                    onValueChange = { editableText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VintageAmber,
                        unfocusedBorderColor = MetallicBorder,
                        focusedTextColor = TextMetallicLight,
                        unfocusedTextColor = TextMetallicLight,
                        focusedContainerColor = MetalCardSurface,
                        unfocusedContainerColor = MetalCardSurface
                    )
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF071206))
                        .border(1.dp, MetallicBorder, RoundedCornerShape(6.dp))
                        .padding(12.dp)
                ) {
                    val scroll = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scroll)
                    ) {
                        Text(
                            text = if (editableText.isNotBlank()) editableText else "Nenhuma letra cadastrada para esta música.",
                            color = TextLcdGreen,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (isEditing) {
                val saveInteraction = remember { MutableInteractionSource() }
                Button(
                    onClick = {
                        onSaveLyrics(editableText)
                        isEditing = false
                    },
                    modifier = Modifier.cinematicPressVisuals(
                        interactionSource = saveInteraction,
                        profile = CinematicProfile.MEDIA_CONTROL
                    ),
                    interactionSource = saveInteraction,
                    colors = ButtonDefaults.buttonColors(containerColor = VintageAmber, contentColor = MetalDarkBackground)
                ) {
                    Text("SALVAR", fontWeight = FontWeight.Bold)
                }
            } else {
                val editInteraction = remember { MutableInteractionSource() }
                Button(
                    onClick = { isEditing = true },
                    modifier = Modifier.cinematicPressVisuals(
                        interactionSource = editInteraction,
                        profile = CinematicProfile.MEDIA_CONTROL
                    ),
                    interactionSource = editInteraction,
                    colors = ButtonDefaults.buttonColors(containerColor = MetalCardSurface, contentColor = VintageAmber)
                ) {
                    Text("EDITAR", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
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
