package com.example.ui.effects

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * COMPOSE INTEGRATION: MODIFICADORES E CONTAINERS DE TRANSIÇÃO CINEMATOGRÁFICA
 */

/**
 * Modificador Compose Global para Elementos Clicáveis.
 * Aplica física de mola, impacto visual ao tocar, giro, escala elástica,
 * desfoque AGSL e aberração cromática RGB.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun Modifier.globalCinematicClickable(
    enabled: Boolean = true,
    profile: CinematicProfile = CinematicProfile.BUTTON_IMPACT,
    rotationDegrees: Float? = null,
    maxBlurRadius: Float? = null,
    maxChromaticShift: Float? = null,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
): Modifier = composed {
    val spec = remember(profile) { CinematicProfiles.getSpec(profile) }
    val effectiveRotation = rotationDegrees ?: spec.maxRotationDegrees

    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    var size by remember { mutableStateOf(IntSize.Zero) }
    val animProgress = remember { Animatable(1f) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    Modifier
        .onSizeChanged { size = it }
        .graphicsLayer {
            if (!enabled) return@graphicsLayer

            val progress = animProgress.value
            val isAnimating = progress < 1.0f

            // 1. Escala com Pulo Elástico e Compressão ao Toque
            val scaleFactor = if (isPressed) {
                spec.pressScale
            } else if (isAnimating) {
                1f + (spec.overshootScale - 1f) * kotlin.math.sin(progress * Math.PI.toFloat()).toFloat() * (1f - progress)
            } else {
                1f
            }
            scaleX = scaleFactor
            scaleY = scaleFactor

            // 2. Rotação cinética
            val currentRotation = if (isPressed) {
                -spec.maxRotationDegrees * 0.05f
            } else if (isAnimating) {
                (1f - progress) * effectiveRotation
            } else {
                0f
            }
            rotationZ = currentRotation

            // 3. Torção Perspectiva 3D
            val skewVal = if (isAnimating) {
                kotlin.math.sin(progress * Math.PI.toFloat() * 2f).toFloat() * 0.10f * spec.perspectiveMultiplier
            } else 0f
            rotationY = skewVal * 10f

            // 4. Velocidade e Desfoque AGSL
            val blurIntensity = if (isAnimating) {
                abs(kotlin.math.sin(progress * Math.PI.toFloat())).toFloat() * spec.blurMultiplier
            } else if (isPressed) {
                0.2f * spec.blurMultiplier
            } else {
                0f
            }

            val chromShift = maxChromaticShift ?: (blurIntensity * 0.8f * spec.chromaticMultiplier)
            val vignetteVal = blurIntensity * 0.25f * spec.vignetteMultiplier

            if (blurIntensity > 0.02f) {
                val w = size.width.toFloat().coerceAtLeast(100f)
                val h = size.height.toFloat().coerceAtLeast(100f)

                renderEffect = CinematicShader.createComposeCinematicEffect(
                    width = w,
                    height = h,
                    rotationSpeed = (1f - progress) * 2.5f,
                    scaleFactor = scaleFactor,
                    blurIntensity = blurIntensity,
                    chromaticShift = chromShift,
                    vignetteIntensity = vignetteVal
                )
            } else {
                renderEffect = null // ZERA TOTALMENTE A RENDERCAMADA NO REPOUSO
            }
        }
        .combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onLongClick = onLongClick?.let {
                {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    it()
                }
            },
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()

                coroutineScope.launch {
                    animProgress.snapTo(0f)
                    animProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }
            }
        )
}

/**
 * Modificador legadinho de compatibilidade para referências existentes
 */
fun Modifier.spinBlurClickable(
    enabled: Boolean = true,
    rotationDegrees: Float = 360f,
    maxBlurRadius: Float = 24f,
    onClick: () -> Unit
): Modifier = globalCinematicClickable(
    enabled = enabled,
    profile = CinematicProfile.BUTTON_IMPACT,
    rotationDegrees = rotationDegrees,
    maxBlurRadius = maxBlurRadius,
    onClick = onClick
)

/**
 * VARIANTE SOMENTE-VISUAL do efeito cinematográfico — SEM captura de clique.
 *
 * Usar em Button/IconButton/TextButton do Material3 (ou qualquer componente
 * que já tenha seu próprio onClick nativo): passe o MESMO `interactionSource`
 * que o componente usa (via `interactionSource = source` no parâmetro do
 * Button), e este modifier aplica giro/blur/aberração cromática reagindo ao
 * mesmo estado de "pressionado" do componente — sem competir pela captura
 * do toque. O onClick do Button/IconButton continua sendo o único responsável
 * por disparar a ação: aqui só entra a física visual.
 *
 * Por que isso existe: `globalCinematicClickable` aplica `.combinedClickable`
 * por conta própria, e quando esse modifier é passado para dentro de um
 * Button/IconButton do Material3, o clique NATIVO do componente intercepta o
 * gesto antes do combinedClickable externo — o onClick real nunca dispara.
 * Esta variante resolve isso não capturando toque algum.
 */
fun Modifier.cinematicPressVisuals(
    interactionSource: MutableInteractionSource,
    profile: CinematicProfile = CinematicProfile.BUTTON_IMPACT,
    rotationDegrees: Float? = null
): Modifier = composed {
    val spec = remember(profile) { CinematicProfiles.getSpec(profile) }
    val effectiveRotation = rotationDegrees ?: spec.maxRotationDegrees
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    var size by remember { mutableStateOf(IntSize.Zero) }
    val animProgress = remember { Animatable(1f) }
    var wasPressed by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        if (isPressed && !wasPressed) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        // Toque solto (transição true -> false) = clique concluído: dispara o giro de impacto.
        if (!isPressed && wasPressed) {
            coroutineScope.launch {
                animProgress.snapTo(0f)
                animProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }
        wasPressed = isPressed
    }

    Modifier
        .onSizeChanged { size = it }
        .graphicsLayer {
            val progress = animProgress.value
            val isAnimating = progress < 1.0f

            val scaleFactor = if (isPressed) {
                spec.pressScale
            } else if (isAnimating) {
                1f + (spec.overshootScale - 1f) * kotlin.math.sin(progress * Math.PI.toFloat()).toFloat() * (1f - progress)
            } else {
                1f
            }
            scaleX = scaleFactor
            scaleY = scaleFactor

            val currentRotation = if (isPressed) {
                -spec.maxRotationDegrees * 0.05f
            } else if (isAnimating) {
                (1f - progress) * effectiveRotation
            } else {
                0f
            }
            rotationZ = currentRotation

            val skewVal = if (isAnimating) {
                kotlin.math.sin(progress * Math.PI.toFloat() * 2f).toFloat() * 0.10f * spec.perspectiveMultiplier
            } else 0f
            rotationY = skewVal * 10f

            val blurIntensity = if (isAnimating) {
                abs(kotlin.math.sin(progress * Math.PI.toFloat())).toFloat() * spec.blurMultiplier
            } else if (isPressed) {
                0.2f * spec.blurMultiplier
            } else {
                0f
            }

            val chromShift = blurIntensity * 0.8f * spec.chromaticMultiplier
            val vignetteVal = blurIntensity * 0.25f * spec.vignetteMultiplier

            if (blurIntensity > 0.02f) {
                val w = size.width.toFloat().coerceAtLeast(100f)
                val h = size.height.toFloat().coerceAtLeast(100f)

                renderEffect = CinematicShader.createComposeCinematicEffect(
                    width = w,
                    height = h,
                    rotationSpeed = (1f - progress) * 2.5f,
                    scaleFactor = scaleFactor,
                    blurIntensity = blurIntensity,
                    chromaticShift = chromShift,
                    vignetteIntensity = vignetteVal
                )
            } else {
                renderEffect = null
            }
        }
}

/**
 * Transição Cinematográfica Global para Troca de Telas (ScreenState)
 */
@Composable
fun <T> GlobalCinematicScreenTransition(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            val enter = fadeIn(animationSpec = tween(450)) + scaleIn(
                initialScale = 0.50f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            val exit = fadeOut(animationSpec = tween(300)) + scaleOut(
                targetScale = 0.80f,
                animationSpec = tween(300)
            )
            enter togetherWith exit
        },
        label = "GlobalCinematicScreenNav"
    ) { state ->
        var transitionProgress by remember { mutableFloatStateOf(0f) }
        var screenSize by remember { mutableStateOf(IntSize.Zero) }

        LaunchedEffect(state) {
            val anim = Animatable(0f)
            anim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
            ) {
                transitionProgress = value
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { screenSize = it }
                .graphicsLayer {
                    if (transitionProgress < 1f) {
                        val remaining = 1f - transitionProgress

                        // Giro Espiral e Inclinação Perspectiva 3D
                        rotationZ = remaining * -180f
                        rotationY = remaining * 25f

                        val currentScale = 1f + 0.35f * kotlin.math.sin(transitionProgress * Math.PI.toFloat()).toFloat()
                        scaleX = currentScale
                        scaleY = currentScale

                        val blurVal = remaining * 1.8f
                        val w = screenSize.width.toFloat().coerceAtLeast(300f)
                        val h = screenSize.height.toFloat().coerceAtLeast(300f)

                        renderEffect = CinematicShader.createComposeCinematicEffect(
                            width = w,
                            height = h,
                            rotationSpeed = remaining * 3.5f,
                            scaleFactor = currentScale,
                            blurIntensity = blurVal,
                            chromaticShift = remaining * 1.5f,
                            vignetteIntensity = remaining * 0.8f
                        )
                    } else {
                        rotationZ = 0f
                        rotationY = 0f
                        scaleX = 1f
                        scaleY = 1f
                        renderEffect = null
                    }
                }
        ) {
            content(state)
        }
    }
}

/**
 * Transição Cinematográfica Global para Troca de Abas
 */
@Composable
fun <T> GlobalSpinBlurTransition(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            val enter = fadeIn(animationSpec = tween(380)) + scaleIn(
                initialScale = 0.60f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            val exit = fadeOut(animationSpec = tween(250)) + scaleOut(
                targetScale = 0.85f,
                animationSpec = tween(250)
            )
            enter togetherWith exit
        },
        label = "GlobalSpinBlurTabNav"
    ) { state ->
        var transitionProgress by remember { mutableFloatStateOf(0f) }
        var screenSize by remember { mutableStateOf(IntSize.Zero) }

        LaunchedEffect(state) {
            val anim = Animatable(0f)
            anim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
            ) {
                transitionProgress = value
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { screenSize = it }
                .graphicsLayer {
                    if (transitionProgress < 1f) {
                        val remaining = 1f - transitionProgress

                        rotationZ = remaining * -90f
                        rotationY = remaining * 15f

                        val currentScale = 1f + 0.22f * kotlin.math.sin(transitionProgress * Math.PI.toFloat()).toFloat()
                        scaleX = currentScale
                        scaleY = currentScale

                        val blurVal = remaining
                        val w = screenSize.width.toFloat().coerceAtLeast(300f)
                        val h = screenSize.height.toFloat().coerceAtLeast(300f)

                        renderEffect = CinematicShader.createComposeCinematicEffect(
                            width = w,
                            height = h,
                            rotationSpeed = remaining * 2.5f,
                            scaleFactor = currentScale,
                            blurIntensity = blurVal,
                            chromaticShift = remaining * 0.9f,
                            vignetteIntensity = remaining * 0.4f
                        )
                    } else {
                        rotationZ = 0f
                        rotationY = 0f
                        scaleX = 1f
                        scaleY = 1f
                        renderEffect = null
                    }
                }
        ) {
            content(state)
        }
    }
}
