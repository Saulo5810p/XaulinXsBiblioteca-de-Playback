package com.example.ui.effects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize

/**
 * COMPONENTES AUXILIARES LEGADOS/SISTÊMICOS
 */

@Composable
fun SpinBlurAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(320)) + scaleIn(
            initialScale = 0.4f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ),
        exit = fadeOut(animationSpec = tween(260)) + scaleOut(
            targetScale = 0.5f,
            animationSpec = tween(260)
        )
    ) {
        // transition.currentState/targetState do AnimatedVisibility já cobre
        // tanto ENTRANDO quanto SAINDO — o LaunchedEffect(visible) reage às
        // duas transições (visible=true anima entrada, visible=false anima saída),
        // então o giro cinematográfico dispara SEMPRE, nas duas direções.
        var transitionProgress by remember { mutableFloatStateOf(0f) }
        var screenSize by remember { mutableStateOf(IntSize.Zero) }

        LaunchedEffect(visible) {
            val anim = Animatable(if (visible) 0f else 1f)
            anim.animateTo(
                targetValue = if (visible) 1f else 0f,
                animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
            ) {
                transitionProgress = value
            }
        }

        Box(
            modifier = Modifier
                .onSizeChanged { screenSize = it }
                .graphicsLayer {
                    val remaining = 1f - transitionProgress
                    if (remaining > 0.005f) {
                        rotationZ = remaining * 360f
                        rotationY = remaining * 24f
                        val blur = remaining * 3.0f

                        val w = screenSize.width.toFloat().coerceAtLeast(100f)
                        val h = screenSize.height.toFloat().coerceAtLeast(100f)

                        renderEffect = CinematicShader.createComposeCinematicEffect(
                            width = w,
                            height = h,
                            rotationSpeed = remaining * 2.2f,
                            scaleFactor = 1f + remaining * 0.25f,
                            blurIntensity = blur,
                            chromaticShift = remaining * 2.2f,
                            vignetteIntensity = remaining * 0.8f
                        )
                    } else {
                        rotationZ = 0f
                        rotationY = 0f
                        renderEffect = null
                    }
                }
        ) {
            content()
        }
    }
}

@Composable
fun SpinBlurIconButton(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    profile: CinematicProfile = CinematicProfile.MEDIA_CONTROL,
    tint: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
    iconSize: androidx.compose.ui.unit.Dp? = null,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    IconButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier.cinematicPressVisuals(
            interactionSource = interactionSource,
            profile = profile
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = if (iconSize != null) Modifier.size(iconSize) else Modifier
        )
    }
}
