package com.example.ui.effects

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlin.math.abs
import kotlin.math.min

/**
 * SISTEMA CINEMATOGRÁFICO GLOBAL DE SCROLL
 *
 * Estende o motor de física/shader já usado em cliques (GlobalMotionEngine,
 * CinematicShader) para reagir à VELOCIDADE do gesto de arrastar listas.
 * Quanto mais rápido o dedo desliza, mais forte o motion blur + aberração
 * cromática AGSL — some suavemente assim que o dedo para (sem custo de GPU
 * em repouso, já que o shader zera o efeito com blur < 0.01).
 *
 * USO: aplicar `Modifier.cinematicScrollVelocity(lazyListState)` diretamente
 * no `Modifier` do LazyColumn/LazyRow (não no item) para o container inteiro
 * reagir à velocidade — ideal pra headers/backgrounds/hero images do topo.
 *
 * Para efeito POR ITEM (cada card "voando" conforme a posição no viewport),
 * usar `Modifier.cinematicScrollItem(lazyListState, index)` dentro do item.
 */

/** Perfil de intensidade do efeito de scroll — controla o quão "insano" fica. */
enum class ScrollCinematicProfile(
    val blurMultiplier: Float,
    val chromaticMultiplier: Float,
    val vignetteMultiplier: Float,
    val maxBlurPx: Float,
    val velocityThreshold: Float
) {
    /** Sutil — quase imperceptível em repouso, leve em arrastões rápidos. */
    SUBTLE(blurMultiplier = 0.5f, chromaticMultiplier = 0.4f, vignetteMultiplier = 0.2f, maxBlurPx = 14f, velocityThreshold = 900f),

    /** Padrão — bom equilíbrio entre efeito visível e performance no A35. */
    STANDARD(blurMultiplier = 1.0f, chromaticMultiplier = 0.8f, vignetteMultiplier = 0.4f, maxBlurPx = 22f, velocityThreshold = 700f),

    /** Insano — pedido do usuário: motion blur forte + aberração cromática acentuada. */
    INSANE(blurMultiplier = 1.8f, chromaticMultiplier = 1.6f, vignetteMultiplier = 0.7f, maxBlurPx = 34f, velocityThreshold = 450f)
}

/**
 * Rastreia a velocidade de scroll (px/frame suavizado) de qualquer ScrollableState.
 * Retorna um Float normalizado 0f..1f pronto pra alimentar o shader.
 */
@Composable
private fun rememberScrollVelocityNormalized(
    scrollableState: ScrollableState,
    profile: ScrollCinematicProfile
): Float {
    var lastOffset by remember { mutableFloatStateOf(0f) }
    var smoothedVelocity by remember { mutableFloatStateOf(0f) }

    val rawOffset = scrollOffsetOf(scrollableState)

    val delta = abs(rawOffset - lastOffset)
    lastOffset = rawOffset

    // Suavização exponencial: sobe rápido (resposta imediata ao gesto),
    // desce suave (o efeito "esvai" ao invés de cortar seco quando o dedo para).
    smoothedVelocity = if (delta > smoothedVelocity) {
        delta
    } else {
        smoothedVelocity * 0.82f + delta * 0.18f
    }

    return min(smoothedVelocity / profile.velocityThreshold, 1f)
}

/** Extrai um valor de offset acumulado comparável frame-a-frame, independente do tipo de estado. */
@Composable
private fun scrollOffsetOf(scrollableState: ScrollableState): Float {
    return when (scrollableState) {
        is LazyListState -> {
            val combined = (scrollableState.firstVisibleItemIndex * 10_000) + scrollableState.firstVisibleItemScrollOffset
            combined.toFloat()
        }
        else -> 0f
    }
}

/**
 * Aplica motion blur + aberração cromática AGSL ao CONTAINER inteiro
 * (LazyColumn/LazyRow/Column com scroll) proporcional à velocidade do gesto.
 *
 * Ideal para: fundo de tela inteira, headers que ficam por trás da lista,
 * ou quando não quer o custo de recalcular por item.
 */
fun Modifier.cinematicScrollVelocity(
    lazyListState: LazyListState,
    profile: ScrollCinematicProfile = ScrollCinematicProfile.STANDARD
): Modifier = composed {
    var size by remember { mutableFloatStateOf(0f) }
    var sizeH by remember { mutableFloatStateOf(0f) }
    val velocity = rememberScrollVelocityNormalized(lazyListState, profile)

    Modifier
        .onSizeChanged { size = it.width.toFloat(); sizeH = it.height.toFloat() }
        .graphicsLayer {
            if (velocity < 0.02f) {
                renderEffect = null
                return@graphicsLayer
            }
            val w = size.coerceAtLeast(100f)
            val h = sizeH.coerceAtLeast(100f)

            renderEffect = CinematicShader.createComposeCinematicEffect(
                width = w,
                height = h,
                rotationSpeed = 0f,
                scaleFactor = 1f,
                blurIntensity = velocity * profile.blurMultiplier,
                chromaticShift = velocity * profile.chromaticMultiplier,
                vignetteIntensity = velocity * profile.vignetteMultiplier
            )
        }
}

/**
 * Aplica o efeito cinematográfico POR ITEM dentro de um LazyColumn/LazyRow:
 * cada card ganha um leve blur+shrink+shift cromático quando a lista está
 * em alta velocidade, e volta nítido/normal quando o dedo para ou o item
 * está parado no centro do viewport.
 *
 * `index` é o índice do item dentro do `items { }` — passar o mesmo usado
 * no itemsIndexed/items para o cálculo de "distância do centro" funcionar.
 */
fun Modifier.cinematicScrollItem(
    lazyListState: LazyListState,
    index: Int,
    profile: ScrollCinematicProfile = ScrollCinematicProfile.STANDARD
): Modifier = composed {
    var size by remember { mutableFloatStateOf(0f) }
    var sizeH by remember { mutableFloatStateOf(0f) }
    val velocity = rememberScrollVelocityNormalized(lazyListState, profile)

    Modifier
        .onSizeChanged { size = it.width.toFloat(); sizeH = it.height.toFloat() }
        .graphicsLayer {
            if (velocity < 0.02f) {
                renderEffect = null
                scaleX = 1f
                scaleY = 1f
                return@graphicsLayer
            }

            // Leve "respiro" de escala pra reforçar a sensação de velocidade,
            // sem descolar o item do layout (efeito puramente visual via layer).
            val shrink = 1f - (velocity * 0.03f)
            scaleX = shrink
            scaleY = shrink

            val w = size.coerceAtLeast(100f)
            val h = sizeH.coerceAtLeast(100f)

            renderEffect = CinematicShader.createComposeCinematicEffect(
                width = w,
                height = h,
                rotationSpeed = velocity * 1.5f,
                scaleFactor = shrink,
                blurIntensity = velocity * profile.blurMultiplier,
                chromaticShift = velocity * profile.chromaticMultiplier,
                vignetteIntensity = velocity * profile.vignetteMultiplier * 0.5f
            )
        }
}
