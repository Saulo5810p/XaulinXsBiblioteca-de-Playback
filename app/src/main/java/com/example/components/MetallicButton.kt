package com.example.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.effects.globalCinematicClickable
import com.example.ui.theme.*

@Composable
fun MetallicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    text: String? = null,
    isActive: Boolean = false,
    activeLedColor: Color = VintageAmber
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val translateY by animateFloatAsState(targetValue = if (isPressed) 3f else 0f, label = "ButtonPress3D")

    val baseModifier = modifier.globalCinematicClickable(
        profile = com.example.ui.effects.CinematicProfile.BUTTON_IMPACT,
        onClick = onClick
    )

    Box(
        modifier = baseModifier
            .graphicsLayer {
                translationY = translateY
            }
            // 3D Shadow Base Drop
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF000000), Color(0xFF101217))
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(bottom = if (isPressed) 1.dp else 3.1.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(
                Brush.verticalGradient(
                    colors = if (isPressed) {
                        listOf(Color(0xFF121419), Color(0xFF22262E), Color(0xFF181B22))
                    } else if (isActive) {
                        listOf(Color(0xFF4C5260), Color(0xFF2D323E), activeLedColor.copy(alpha = 0.35f))
                    } else {
                        listOf(Color(0xFF5A6270), Color(0xFF333945), Color(0xFF1B1E26))
                    }
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    colors = if (isActive) {
                        listOf(activeLedColor, activeLedColor.copy(alpha = 0.6f), activeLedColor)
                    } else {
                        listOf(Color(0xFFA0ABBA), Color(0xFF555C68), Color(0xFF1A1C22))
                    }
                ),
                shape = RoundedCornerShape(7.dp)
            )
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // LED Status Indicator with Glow
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isActive) activeLedColor else Color(0xFF0D0E12))
                    .border(
                        1.dp,
                        if (isActive) Color.White.copy(alpha = 0.9f) else Color(0xFF383F4D),
                        CircleShape
                    )
            )

            Spacer(modifier = Modifier.height(5.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = text,
                        tint = if (isActive) activeLedColor else TextMetallicLight,
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (text != null) {
                    Text(
                        text = text,
                        color = if (isActive) activeLedColor else TextMetallicLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

