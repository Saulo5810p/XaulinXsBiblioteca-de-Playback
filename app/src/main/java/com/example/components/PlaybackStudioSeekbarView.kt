package com.example.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * PlaybackStudioSeekbarView
 * High-End 80s/90s Hi-Fi 3D Curved Audio Module Seekbar.
 * 
 * Features:
 * - 3D Curved Cabinet Array (Left, Center, Right modules) in ~30° perspective view.
 * - 10-Layer Canvas Rendering: Drop Shadows, Machined Brass Chassis, Brushed Metal Front Panels,
 *   Real Driver Cones (Woofers & Tweeters), Hex Bolts, Amber LED Progress Rail, Metallic Slider Thumb Knob,
 *   Specular Studio Reflection Highlights.
 * - Interactive Curved Arc Progress Rail with MIN/MAX Backlit Indicators & Haptic Scrubbing.
 */
@Composable
fun PlaybackStudioSeekbarView(
    currentPositionMs: Long,
    totalDurationMs: Long,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 230.dp
) {
    val haptic = LocalHapticFeedback.current

    val rawProgress = if (totalDurationMs > 0) {
        (currentPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(rawProgress) }
    var dragVelocityPxMs by remember { mutableFloatStateOf(0f) }
    var lastDragTimestamp by remember { mutableLongStateOf(0L) }
    var lastDragXPosition by remember { mutableFloatStateOf(0f) }

    val activeProgress = if (isDragging) dragProgress else rawProgress

    // Smooth spring animation for thumb positioning
    val animatedProgress by animateFloatAsState(
        targetValue = activeProgress,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "seekbarProgress"
    )

    // Animated velocity decay after release
    val animatedVelocity by animateFloatAsState(
        targetValue = if (isDragging) dragVelocityPxMs else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "seekbarVelocity"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF14171E),
                        Color(0xFF0C0E12),
                        Color(0xFF161A22)
                    )
                )
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .graphicsLayer {
                    val absVel = kotlin.math.abs(animatedVelocity)
                    val velocityBlur = (absVel * 0.18f).coerceIn(0f, 22f)
                    val velocityChrom = (absVel * 0.12f).coerceIn(0f, 1.8f)

                    if (velocityBlur > 0.05f) {
                        val w = size.width.coerceAtLeast(100f)
                        val h = size.height.coerceAtLeast(100f)
                        renderEffect = com.example.ui.effects.CinematicShader.createComposeCinematicEffect(
                            width = w,
                            height = h,
                            rotationSpeed = animatedVelocity * 0.08f,
                            scaleFactor = 1.0f + (velocityBlur * 0.005f),
                            blurIntensity = velocityBlur / 20f,
                            chromaticShift = velocityChrom,
                            vignetteIntensity = (velocityBlur / 20f) * 0.4f
                        )
                    } else {
                        renderEffect = null
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val width = size.width
                        val paddingX = width * 0.08f
                        val trackWidth = width - (paddingX * 2)
                        val relativeX = (offset.x - paddingX).coerceIn(0f, trackWidth)
                        val frac = (relativeX / trackWidth).coerceIn(0f, 1f)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSeekTo((frac * totalDurationMs).toLong())
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            lastDragTimestamp = System.currentTimeMillis()
                            lastDragXPosition = offset.x
                            dragVelocityPxMs = 0f
                            val width = size.width
                            val paddingX = width * 0.08f
                            val trackWidth = width - (paddingX * 2)
                            val relativeX = (offset.x - paddingX).coerceIn(0f, trackWidth)
                            dragProgress = (relativeX / trackWidth).coerceIn(0f, 1f)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        onDragEnd = {
                            isDragging = false
                            dragVelocityPxMs = 0f
                            onSeekTo((dragProgress * totalDurationMs).toLong())
                        },
                        onDragCancel = {
                            isDragging = false
                            dragVelocityPxMs = 0f
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val now = System.currentTimeMillis()
                            val dt = (now - lastDragTimestamp).coerceAtLeast(1L)
                            val dx = change.position.x - lastDragXPosition
                            dragVelocityPxMs = (dx / dt.toFloat()) * 10f

                            lastDragTimestamp = now
                            lastDragXPosition = change.position.x

                            val width = size.width
                            val paddingX = width * 0.08f
                            val trackWidth = width - (paddingX * 2)
                            val relativeX = (change.position.x - paddingX).coerceIn(0f, trackWidth)
                            dragProgress = (relativeX / trackWidth).coerceIn(0f, 1f)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // =========================================================
                // LAYER 1: Drop Shadows under the curved Hi-Fi chassis
                // =========================================================
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.70f),
                    topLeft = Offset(w * 0.04f, h * 0.12f),
                    size = Size(w * 0.92f, h * 0.82f),
                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                )

                // =========================================================
                // LAYER 2: Main Outer Brass & Gold Curved Base Frame
                // =========================================================
                val baseOuterPath = Path().apply {
                    // Curved Top Arch (3D perspective ~30°)
                    moveTo(w * 0.05f, h * 0.16f)
                    cubicTo(
                        w * 0.25f, h * 0.02f,
                        w * 0.75f, h * 0.02f,
                        w * 0.95f, h * 0.16f
                    )
                    // Right curve down
                    lineTo(w * 0.96f, h * 0.82f)
                    // Sweeping Bottom Crescent Arc
                    cubicTo(
                        w * 0.75f, h * 0.96f,
                        w * 0.25f, h * 0.96f,
                        w * 0.04f, h * 0.82f
                    )
                    close()
                }

                drawPath(
                    path = baseOuterPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            BrassGold,
                            Color(0xFFD4AF37),
                            Color(0xFF5C4610),
                            BrassGold,
                            Color(0xFF241B06)
                        )
                    )
                )

                // Golden Specular Bevel Ring Outline
                drawPath(
                    path = baseOuterPath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.90f),
                            BrassGold,
                            Color(0xFF3B2E0B),
                            Color.White.copy(alpha = 0.70f)
                        )
                    ),
                    style = Stroke(width = 3.dp.toPx())
                )

                // =========================================================
                // LAYER 3 & 4: 3 Cabinets (Left, Center, Right) with Curved Perspective
                // =========================================================
                val cabTopY = h * 0.12f
                val cabHeight = h * 0.52f

                // --- LEFT CABINET (Curved perspective, smaller width) ---
                val leftCabRect = Rect(w * 0.07f, cabTopY, w * 0.29f, cabTopY + cabHeight)
                drawHiFiCabinetModule(
                    rect = leftCabRect,
                    isCenter = false,
                    hasWooferAndTweeter = true
                )

                // --- CENTER CABINET (Main oval woofer, larger width) ---
                val centerCabRect = Rect(w * 0.31f, cabTopY - 10.dp.toPx(), w * 0.69f, cabTopY + cabHeight + 4.dp.toPx())
                drawHiFiCabinetModule(
                    rect = centerCabRect,
                    isCenter = true,
                    hasWooferAndTweeter = false
                )

                // --- RIGHT CABINET (Curved perspective, smaller width) ---
                val rightCabRect = Rect(w * 0.71f, cabTopY, w * 0.93f, cabTopY + cabHeight)
                drawHiFiCabinetModule(
                    rect = rightCabRect,
                    isCenter = false,
                    hasWooferAndTweeter = true
                )

                // =========================================================
                // LAYER 7 & 8: CURVED SEEKBAR RAIL WITH AMBER LED TICKS & MIN/MAX
                // =========================================================
                val railY = h * 0.76f
                val railStartX = w * 0.09f
                val railEndX = w * 0.91f
                val railWidth = railEndX - railStartX

                // Dark Recessed Metal Track Bed
                val trackPath = Path().apply {
                    moveTo(railStartX, railY)
                    quadraticTo(w * 0.5f, railY + 12.dp.toPx(), railEndX, railY)
                }

                drawPath(
                    path = trackPath,
                    color = Color(0xFF0F1116),
                    style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
                )

                drawPath(
                    path = trackPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF322408), Color(0xFF100C03))
                    ),
                    style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                )

                drawPath(
                    path = trackPath,
                    brush = Brush.linearGradient(
                        colors = listOf(BrassGold, Color(0xFF6B5317), BrassGold)
                    ),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )

                // Individual Amber LED Tick Marks along the curved arc
                val totalTicks = 48
                val currentTickIndex = (animatedProgress * totalTicks).toInt()

                for (i in 0..totalTicks) {
                    val t = i.toFloat() / totalTicks
                    val tickX = railStartX + t * railWidth
                    // Curve equation offset
                    val arcOffsetY = 12.dp.toPx() * (4 * t * (1 - t))
                    val tickY = railY + arcOffsetY

                    val isLit = i <= currentTickIndex
                    val tickColor = if (isLit) VintageAmber else Color(0xFF382C17)
                    val tickHeight = if (i % 6 == 0) 10.dp.toPx() else 6.dp.toPx()

                    drawLine(
                        color = tickColor,
                        start = Offset(tickX, tickY - tickHeight / 2f),
                        end = Offset(tickX, tickY + tickHeight / 2f),
                        strokeWidth = if (isLit) 2.5.dp.toPx() else 1.5.dp.toPx()
                    )
                }

                // Active Glowing Amber Progress Track Overlay
                if (animatedProgress > 0.01f) {
                    val activeTrackPath = Path().apply {
                        moveTo(railStartX, railY)
                        val endX = railStartX + animatedProgress * railWidth
                        quadraticTo(
                            railStartX + (animatedProgress * railWidth * 0.5f),
                            railY + 12.dp.toPx() * (4 * (animatedProgress * 0.5f) * (1 - (animatedProgress * 0.5f))),
                            endX,
                            railY + 12.dp.toPx() * (4 * animatedProgress * (1 - animatedProgress))
                        )
                    }

                    drawPath(
                        path = activeTrackPath,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                VintageAmber.copy(alpha = 0.6f),
                                VintageAmber,
                                Color(0xFFFFD54F)
                            )
                        ),
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // MIN and MAX Backlit Indicator Boxes at Track Ends
                drawMinMaxBadge(
                    text = "MIN",
                    center = Offset(railStartX - 14.dp.toPx(), railY - 2.dp.toPx()),
                    isLit = animatedProgress < 0.15f
                )

                drawMinMaxBadge(
                    text = "MAX",
                    center = Offset(railEndX + 14.dp.toPx(), railY - 2.dp.toPx()),
                    isLit = animatedProgress > 0.85f
                )

                // =========================================================
                // LAYER 9: HEAVY BRASS & CHROME SLIDER THUMB KNOB
                // =========================================================
                val thumbX = railStartX + animatedProgress * railWidth
                val thumbArcOffset = 12.dp.toPx() * (4 * animatedProgress * (1 - animatedProgress))
                val thumbY = railY + thumbArcOffset

                // Thumb Cast Shadow
                drawRect(
                    color = Color.Black.copy(alpha = 0.60f),
                    topLeft = Offset(thumbX - 7.dp.toPx(), thumbY - 14.dp.toPx() + 4.dp.toPx()),
                    size = Size(14.dp.toPx(), 28.dp.toPx())
                )

                // Heavy Solid Chrome & Gold Slider Thumb Cylinder
                val thumbRect = Rect(thumbX - 7.dp.toPx(), thumbY - 14.dp.toPx(), thumbX + 7.dp.toPx(), thumbY + 14.dp.toPx())
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            BrassGold,
                            Color(0xFF4A3A10),
                            BrassGold,
                            Color.White
                        )
                    ),
                    topLeft = Offset(thumbRect.left, thumbRect.top),
                    size = Size(thumbRect.width, thumbRect.height),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                )

                // Thumb Bevel Frame
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White, MetalBevelLight, Color(0xFF241B06))
                    ),
                    topLeft = Offset(thumbRect.left, thumbRect.top),
                    size = Size(thumbRect.width, thumbRect.height),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                    style = Stroke(width = 1.2.dp.toPx())
                )

                // Center Engraved Marker Line on Slider Knob
                drawLine(
                    color = VintageAmber,
                    start = Offset(thumbX, thumbY - 10.dp.toPx()),
                    end = Offset(thumbX, thumbY + 10.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )

                // =========================================================
                // LAYER 10: SPECULAR GLASS & STUDIO LIGHTING SWEEPS
                // =========================================================
                val studioHighlightPath = Path().apply {
                    moveTo(w * 0.08f, h * 0.08f)
                    lineTo(w * 0.92f, h * 0.08f)
                    lineTo(w * 0.85f, h * 0.14f)
                    lineTo(w * 0.15f, h * 0.14f)
                    close()
                }

                drawPath(
                    path = studioHighlightPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.45f),
                            Color.Transparent
                        )
                    )
                )
            }
        }

        // Digital Time Display Row under Hi-Fi Module
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(VintageAmber)
                )
                Text(
                    text = "ELAPSED: ${formatMs(currentPositionMs)}",
                    color = TextLcdGreen,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "HI-FI SEEK CONTROL • 3D RACK",
                color = TextMetallicMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = "REMAINING: -${formatMs((totalDurationMs - currentPositionMs).coerceAtLeast(0L))}",
                color = TextMetallicLight,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Helper to draw 3D Cabinet Modules (Woofers, Tweeters, Braces, Hex Screws)
 */
private fun DrawScope.drawHiFiCabinetModule(
    rect: Rect,
    isCenter: Boolean,
    hasWooferAndTweeter: Boolean
) {
    // 1. Cabinet Solid Machined Graphite Back Body
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF323A48),
                Color(0xFF1E222B),
                Color(0xFF101217)
            )
        ),
        topLeft = Offset(rect.left, rect.top),
        size = Size(rect.width, rect.height),
        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
    )

    // Golden Bevel Chamfer Frame
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                BrassGold,
                Color(0xFF6B5317),
                BrassGold
            )
        ),
        topLeft = Offset(rect.left, rect.top),
        size = Size(rect.width, rect.height),
        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
        style = Stroke(width = 2.dp.toPx())
    )

    // Recessed Dark Speaker Baffle Faceplate
    val innerMargin = 6.dp.toPx()
    val baffleRect = Rect(rect.left + innerMargin, rect.top + innerMargin, rect.right - innerMargin, rect.bottom - innerMargin)

    drawRoundRect(
        color = Color(0xFF0A0C10),
        topLeft = Offset(baffleRect.left, baffleRect.top),
        size = Size(baffleRect.width, baffleRect.height),
        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
    )

    // Corner Hex Screws on Baffle Faceplate
    val screwRadius = 2.dp.toPx()
    drawHexScrew(Offset(baffleRect.left + 5.dp.toPx(), baffleRect.top + 5.dp.toPx()), screwRadius)
    drawHexScrew(Offset(baffleRect.right - 5.dp.toPx(), baffleRect.top + 5.dp.toPx()), screwRadius)
    drawHexScrew(Offset(baffleRect.left + 5.dp.toPx(), baffleRect.bottom - 5.dp.toPx()), screwRadius)
    drawHexScrew(Offset(baffleRect.right - 5.dp.toPx(), baffleRect.bottom - 5.dp.toPx()), screwRadius)

    if (isCenter) {
        // --- CENTER CABINET OVAL WOOFER DRIVER ---
        val centerPt = Offset(baffleRect.left + baffleRect.width / 2f, baffleRect.top + baffleRect.height / 2f)
        val ovalW = baffleRect.width * 0.72f
        val ovalH = baffleRect.height * 0.76f

        // Outer Metallic Ring Rim
        drawOval(
            brush = Brush.sweepGradient(
                colors = listOf(BrassGold, Color.White, Color(0xFF362B0A), BrassGold),
                center = centerPt
            ),
            topLeft = Offset(centerPt.x - ovalW / 2f, centerPt.y - ovalH / 2f),
            size = Size(ovalW, ovalH),
            style = Stroke(width = 3.dp.toPx())
        )

        // Rubber Surround Cone Roll
        drawOval(
            color = Color(0xFF161920),
            topLeft = Offset(centerPt.x - (ovalW * 0.88f) / 2f, centerPt.y - (ovalH * 0.88f) / 2f),
            size = Size(ovalW * 0.88f, ovalH * 0.88f)
        )

        // Paper/Poly Cone Surface
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF343B47), Color(0xFF0F1116)),
                center = centerPt
            ),
            topLeft = Offset(centerPt.x - (ovalW * 0.74f) / 2f, centerPt.y - (ovalH * 0.74f) / 2f),
            size = Size(ovalW * 0.74f, ovalH * 0.74f)
        )

        // Machined Metallic Central Dust Cap Dome
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, BrassGold, Color(0xFF2E2409)),
                center = centerPt
            ),
            topLeft = Offset(centerPt.x - (ovalW * 0.28f) / 2f, centerPt.y - (ovalH * 0.28f) / 2f),
            size = Size(ovalW * 0.28f, ovalH * 0.28f)
        )
    } else if (hasWooferAndTweeter) {
        // --- LEFT / RIGHT CABINETS (Tweeter top + Woofer bottom) ---
        val centerX = baffleRect.left + baffleRect.width / 2f
        val tweeterY = baffleRect.top + baffleRect.height * 0.28f
        val wooferY = baffleRect.top + baffleRect.height * 0.68f

        // 1. Dome Tweeter (Top)
        val tweeterRadius = baffleRect.width * 0.18f
        drawCircle(
            color = Color(0xFF222732),
            radius = tweeterRadius,
            center = Offset(centerX, tweeterY)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(BrassGold, Color(0xFF42350F)),
                center = Offset(centerX, tweeterY)
            ),
            radius = tweeterRadius * 0.55f,
            center = Offset(centerX, tweeterY)
        )

        // 2. Main Woofer (Bottom)
        val wooferRadius = baffleRect.width * 0.35f
        // Metallic Rim
        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(BrassGold, Color.White, Color(0xFF382D0D), BrassGold),
                center = Offset(centerX, wooferY)
            ),
            radius = wooferRadius,
            center = Offset(centerX, wooferY),
            style = Stroke(width = 2.5.dp.toPx())
        )
        // Rubber Surround
        drawCircle(
            color = Color(0xFF14171E),
            radius = wooferRadius * 0.88f,
            center = Offset(centerX, wooferY)
        )
        // Cone Surface
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF2A313C), Color(0xFF0C0E12)),
                center = Offset(centerX, wooferY)
            ),
            radius = wooferRadius * 0.70f,
            center = Offset(centerX, wooferY)
        )
        // Dust Cap
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, BrassGold, Color(0xFF241C06)),
                center = Offset(centerX, wooferY)
            ),
            radius = wooferRadius * 0.25f,
            center = Offset(centerX, wooferY)
        )
    }
}

private fun DrawScope.drawHexScrew(center: Offset, radius: Float) {
    drawCircle(
        color = Color(0xFF6E7888),
        radius = radius,
        center = center
    )
    drawCircle(
        color = Color(0xFF1E222A),
        radius = radius * 0.5f,
        center = center
    )
}

private fun DrawScope.drawMinMaxBadge(text: String, center: Offset, isLit: Boolean) {
    val boxWidth = 22.dp.toPx()
    val boxHeight = 12.dp.toPx()
    val rect = Rect(center.x - boxWidth / 2f, center.y - boxHeight / 2f, center.x + boxWidth / 2f, center.y + boxHeight / 2f)

    drawRoundRect(
        color = if (isLit) Color(0xFF4A380B) else Color(0xFF1A1D24),
        topLeft = Offset(rect.left, rect.top),
        size = Size(rect.width, rect.height),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )

    drawRoundRect(
        color = if (isLit) VintageAmber else Color(0xFF4A5260),
        topLeft = Offset(rect.left, rect.top),
        size = Size(rect.width, rect.height),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
        style = Stroke(width = 1.dp.toPx())
    )
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
