package com.example.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import com.example.ui.effects.CinematicProfile
import com.example.ui.effects.globalCinematicClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * High-End Hi-Fi Turntable (Technics SL-1200 / Marantz / Pioneer Style)
 * Features:
 * 1. Independent Machined Heavy Aluminum Platter (TurntablePlatter)
 * 2. Independent Vinyl Record entity (VinylRecord) with rotation, grooves, scratch, and physical LP swap sequence
 * 3. 3D Volumetric Smoked Acrylic Dust Cover (Cúpula Acrílica 3D Fumê) with 4 side walls, bevelled lapidated edges,
 *    fixed metal chassis hinges, specular glare streaks, gravitational physics easing, and internal optical occlusion.
 * 4. Chrome Tone Arm Assembly with mechanical cueing lever lift and dynamic shadow.
 */
@Composable
fun VinylTurntable(
    isPlaying: Boolean,
    albumArtPainter: Painter? = null,
    trackTitle: String = "",
    trackArtist: String = "",
    modifier: Modifier = Modifier,
    size: Dp = 360.dp,
    onTogglePlayPause: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // Interactive & Physical State
    var isLidClosed by remember { mutableStateOf(true) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var isTouchingVinyl by remember { mutableStateOf(false) }
    var touchSpeedFactor by remember { mutableFloatStateOf(1.0f) }
    var currentRpmFactor by remember { mutableFloatStateOf(0f) }
    var isCueLifted by remember { mutableStateOf(false) }
    var lastTouchTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var isIdle by remember { mutableStateOf(false) }

    // Physical Disc Exchange Cinematic Sequence State
    var discOffsetX by remember { mutableFloatStateOf(0f) }
    var discAlpha by remember { mutableFloatStateOf(1f) }
    var isSwappingDisc by remember { mutableStateOf(false) }
    var activeTrackKey by remember { mutableStateOf(trackTitle + trackArtist) }

    // Ambient Idle Camera Presence (Zoom out 3.5% after 4.5s idle)
    LaunchedEffect(lastTouchTime) {
        isIdle = false
        delay(4500)
        isIdle = true
    }

    val idleCameraScale by animateFloatAsState(
        targetValue = if (isIdle) 0.965f else 1.0f,
        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        label = "idleZoom"
    )

    // Needle Drop Micro-Vibration Haptic Effect
    LaunchedEffect(isCueLifted) {
        if (!isCueLifted && isPlaying) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    // CINEMATIC PHYSICAL DISC SWAP SEQUENCE (Troca Física de LPs)
    LaunchedEffect(trackTitle, trackArtist) {
        val newKey = trackTitle + trackArtist
        if (newKey != activeTrackKey && newKey.isNotEmpty()) {
            isSwappingDisc = true
            // 1. Elevate Cue Arm
            isCueLifted = true
            delay(150)

            // 2. Open Acrylic Dust Cover
            isLidClosed = false
            delay(300)

            // 3. Eject active LP disc (Slide right + fade)
            val slideOutAnim = TargetBasedAnimation(
                animationSpec = tween(400, easing = FastOutSlowInEasing),
                typeConverter = Float.VectorConverter,
                initialValue = 0f,
                targetValue = 500f
            )
            val startTime = System.nanoTime()
            while (discOffsetX < 490f) {
                val playTime = System.nanoTime() - startTime
                discOffsetX = slideOutAnim.getValueFromNanos(playTime)
                discAlpha = (1f - (discOffsetX / 500f)).coerceIn(0f, 1f)
                delay(16)
            }

            activeTrackKey = newKey

            // 4. Slide in new LP disc from the left (-500f -> 0f)
            discOffsetX = -500f
            discAlpha = 0f

            val slideInAnim = TargetBasedAnimation(
                animationSpec = tween(450, easing = FastOutSlowInEasing),
                typeConverter = Float.VectorConverter,
                initialValue = -500f,
                targetValue = 0f
            )
            val startInTime = System.nanoTime()
            while (discOffsetX < -5f) {
                val playTime = System.nanoTime() - startInTime
                discOffsetX = slideInAnim.getValueFromNanos(playTime)
                discAlpha = (1f + (discOffsetX / 500f)).coerceIn(0f, 1f)
                delay(16)
            }
            discOffsetX = 0f
            discAlpha = 1f

            // 5. Close Dust Cover gently
            delay(200)
            isLidClosed = true

            // 6. Lower Cue Arm onto vinyl
            delay(400)
            isCueLifted = false
            isSwappingDisc = false
        }
    }

    // Platter Motor Inertia Physics (Smooth Rev-Up on Play, Natural Friction Coast-to-Stop on Pause)
    LaunchedEffect(isPlaying) {
        val targetRpm = if (isPlaying) 1.0f else 0.0f
        val duration = if (isPlaying) 1100 else 2300

        if (isPlaying) {
            // Closing dust cover & lowering tonearm on Play
            isLidClosed = true
            isCueLifted = true
            delay(180)
            isCueLifted = false
        } else if (!isSwappingDisc) {
            // Raising tonearm on Pause
            isCueLifted = true
        }

        animate(
            initialValue = currentRpmFactor,
            targetValue = targetRpm,
            animationSpec = tween(
                durationMillis = duration,
                easing = if (isPlaying) FastOutSlowInEasing else LinearOutSlowInEasing
            )
        ) { value, _ ->
            currentRpmFactor = value
        }
    }

    // Touch Friction Speed Restoration Physics (Vinyl Scratch / Pitch Bend)
    LaunchedEffect(isTouchingVinyl) {
        if (isTouchingVinyl) {
            animate(
                initialValue = touchSpeedFactor,
                targetValue = 0.12f,
                animationSpec = tween(durationMillis = 180, easing = LinearOutSlowInEasing)
            ) { value, _ -> touchSpeedFactor = value }
        } else {
            animate(
                initialValue = touchSpeedFactor,
                targetValue = 1.0f,
                animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)
            ) { value, _ -> touchSpeedFactor = value }
        }
    }

    // Continuous Frame Loop for Platter & Vinyl Rotation Physics
    LaunchedEffect(Unit) {
        var lastTime = System.nanoTime()
        while (true) {
            withFrameNanos { frameTime ->
                val dt = (frameTime - lastTime) / 1_000_000_000f
                lastTime = frameTime
                if (currentRpmFactor > 0.0001f || isTouchingVinyl) {
                    rotationAngle = (rotationAngle + 120f * currentRpmFactor * touchSpeedFactor * dt) % 360f
                }
            }
        }
    }

    // Tone arm angle animation (0 deg rest, 26 deg playing on vinyl)
    val toneArmAngle by animateFloatAsState(
        targetValue = if (isPlaying && !isSwappingDisc) 26f else 0f,
        animationSpec = spring(stiffness = 160f, dampingRatio = 0.78f),
        label = "toneArmAngle"
    )

    // Mechanical Cueing Lever Lift Height Offset
    val toneArmLiftOffset by animateDpAsState(
        targetValue = if (isCueLifted) (-10).dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioLowBouncy),
        label = "cueLift"
    )

    // 3D Smoked Acrylic Cover Gravitational Rotation (0° closed to -72° fully open)
    val lidRotationX by animateFloatAsState(
        targetValue = if (isLidClosed) 0f else -72f,
        animationSpec = spring(stiffness = 110f, dampingRatio = 0.85f),
        label = "smokedGlassLidAngle3D"
    )

    val lidAlpha by animateFloatAsState(
        targetValue = if (isLidClosed) 0.98f else 0.42f,
        label = "smokedGlassLidAlpha"
    )

    val lidReflectionFactor by animateFloatAsState(
        targetValue = if (isLidClosed) 1.0f else 0.15f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "lidReflection"
    )

    val glassBlurRadius by animateDpAsState(
        targetValue = if (isLidClosed) 3.5.dp else 0.dp,
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing),
        label = "glassBlur"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        // Main Turntable Chassis Frame
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = idleCameraScale
                    scaleY = idleCameraScale
                }
                .pointerInput(Unit) {
                    detectTapGestures {
                        lastTouchTime = System.currentTimeMillis()
                    }
                }
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF323844),
                            Color(0xFF1E222B),
                            Color(0xFF111318),
                            Color(0xFF232832)
                        )
                    )
                )
                .border(
                    width = 3.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFA1ACB8),
                            MetallicBorder,
                            BrassGold,
                            Color(0xFF151820)
                        )
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            // Corner Mounting Screws
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ScrewHead()
                ScrewHead()
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ScrewHead()
                ScrewHead()
            }

            // Fixed Metal Rear Hinges (Mounted on the Deck, stays fixed while cover rotates on them)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = (-4).dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DeckHingeMount()
                DeckHingeMount()
            }

            // Discreet Under-Platter Amber Backlight Glow
            Box(
                modifier = Modifier
                    .size(size * 0.82f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                VintageAmber.copy(alpha = if (isTouchingVinyl) 0.70f else 0.42f),
                                BrassGold.copy(alpha = 0.20f),
                                Color.Transparent
                            )
                        )
                    )
                    .blur(16.dp)
            )

            // =========================================================
            // 1. TURNTABLE PLATTER (Prato de Alumínio Usinado)
            // =========================================================
            TurntablePlatter(
                currentRpmFactor = currentRpmFactor,
                rotationAngle = rotationAngle,
                isTouchingVinyl = isTouchingVinyl,
                glassBlurRadius = glassBlurRadius,
                modifier = Modifier.fillMaxSize()
            )

            // =========================================================
            // 2. VINYL RECORD ENTITY (Disco de Vinil Independente)
            // =========================================================
            VinylRecord(
                albumArtPainter = albumArtPainter,
                rotationAngle = rotationAngle,
                discOffsetX = discOffsetX,
                discAlpha = discAlpha,
                glassBlurRadius = glassBlurRadius,
                isLidClosed = isLidClosed,
                onTouchStart = {
                    isTouchingVinyl = true
                    lastTouchTime = System.currentTimeMillis()
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                onTouchEnd = {
                    isTouchingVinyl = false
                },
                onDragScratch = { delta ->
                    lastTouchTime = System.currentTimeMillis()
                    rotationAngle = (rotationAngle + delta * 0.4f) % 360f
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                modifier = Modifier.fillMaxSize(0.90f)
            )

            // Center Spindle Metallic Pin
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .rotate(rotationAngle * 0.5f)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            colors = listOf(
                                Color.White,
                                BrassGold,
                                Color(0xFF332000),
                                Color.White
                            )
                        )
                    )
                    .border(0.5.dp, MetalBevelLight, CircleShape)
            )

            // Tone Arm Shadow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = 6.dp, y = (10 + toneArmLiftOffset.value.toInt()).dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val pivot = Offset(this.size.width * 0.86f, this.size.height * 0.14f)
                    val armLength = this.size.width * 0.63f
                    val angleRad = Math.toRadians((toneArmAngle + 120f).toDouble())
                    val armEnd = Offset(
                        pivot.x + armLength * kotlin.math.cos(angleRad).toFloat(),
                        pivot.y + armLength * kotlin.math.sin(angleRad).toFloat()
                    )

                    drawLine(
                        color = Color.Black.copy(alpha = 0.45f),
                        start = pivot,
                        end = armEnd,
                        strokeWidth = 6.dp.toPx()
                    )
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.40f),
                        radius = 8.dp.toPx(),
                        center = armEnd
                    )
                }
            }

            // Chrome Tone Arm Armature Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = toneArmLiftOffset)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val pivot = Offset(this.size.width * 0.86f, this.size.height * 0.14f)
                    val armLength = this.size.width * 0.63f

                    // Tone Arm Base Gimbal
                    drawCircle(
                        color = Color(0xFF535C6D),
                        radius = 16.dp.toPx(),
                        center = pivot
                    )
                    drawCircle(
                        color = BrassGold,
                        radius = 9.dp.toPx(),
                        center = pivot
                    )

                    // Chrome Shaft
                    val angleRad = Math.toRadians((toneArmAngle + 120f).toDouble())
                    val armEnd = Offset(
                        pivot.x + armLength * kotlin.math.cos(angleRad).toFloat(),
                        pivot.y + armLength * kotlin.math.sin(angleRad).toFloat()
                    )

                    drawLine(
                        color = Color(0xFFF0F4F8),
                        start = pivot,
                        end = armEnd,
                        strokeWidth = 4.5.dp.toPx()
                    )

                    // Stylus Cartridge
                    drawCircle(
                        color = BrassGold,
                        radius = 7.dp.toPx(),
                        center = armEnd
                    )
                }
            }

            // Touch Scratching Active Badge
            if (isTouchingVinyl) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 18.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Red.copy(alpha = 0.85f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "MANUAL VINYL SCRATCH • ATRITO APLICADO",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Cueing Arm Lifted Indicator
            if (isCueLifted) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 22.dp, end = 22.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(VintageAmber.copy(alpha = 0.9f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isSwappingDisc) "TROCA FÍSICA DE LP..." else "CUEING ARM LIFTED",
                        color = MetalDarkBackground,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // =========================================================
            // 3. 3D VOLUMETRIC SMOKED ACRYLIC DUST COVER (Cúpula Acrílica 3D)
            // =========================================================
            DustCover3D(
                lidRotationX = lidRotationX,
                lidAlpha = lidAlpha,
                lidReflectionFactor = lidReflectionFactor,
                isLidClosed = isLidClosed,
                onToggleLid = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isLidClosed = !isLidClosed
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Engine Status Bar & Toggle Cover Button
        Row(
            modifier = Modifier.fillMaxWidth(0.94f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (currentRpmFactor > 0.01f) FluorescentGreen else Color(0xFF3A4250))
                )
                Text(
                    text = when {
                        isSwappingDisc -> "CARREGANDO LP NA MESA..."
                        isTouchingVinyl -> "TOUCH FRICTION ACTIVE"
                        currentRpmFactor in 0.01f..0.99f -> if (isPlaying) "REV-UP INERTIA..." else "COASTING TO STOP..."
                        currentRpmFactor >= 0.99f -> "DIRECT DRIVE 33⅓ RPM"
                        else -> "MOTOR STOPPED"
                    },
                    color = if (isTouchingVinyl) Color.Red else if (currentRpmFactor in 0.01f..0.99f || isSwappingDisc) VintageAmber else TextMetallicLight,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(MetalPanelSurface, Color(0xFF282D38))
                        )
                    )
                    .border(1.dp, MetallicBorder, RoundedCornerShape(6.dp))
                    .globalCinematicClickable(
                        profile = CinematicProfile.LIST_ITEM,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            isLidClosed = !isLidClosed
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Vidro",
                        tint = VintageAmber,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = if (isLidClosed) "ABRIR TAMPA (72°)" else "FECHAR TAMPA (0°)",
                        color = TextMetallicLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

/**
 * Machined Aluminum Turntable Platter
 */
@Composable
private fun TurntablePlatter(
    currentRpmFactor: Float,
    rotationAngle: Float,
    isTouchingVinyl: Boolean,
    glassBlurRadius: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(10.dp)
            .blur(glassBlurRadius)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF4A5262),
                        Color(0xFF282D38),
                        Color(0xFF0E1014)
                    )
                )
            )
            .border(4.dp, MetalBevelLight, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Stroboscopic Dots Outer Rim
        val strobeVisualAngle = if (currentRpmFactor >= 0.99f && !isTouchingVinyl) {
            rotationAngle * 0.03f // Optical locked stroboscopic illusion
        } else {
            rotationAngle
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val outerRadius = this.size.width / 2f - 8.dp.toPx()

            for (i in 0 until 90) {
                val dotAngleRad = Math.toRadians((strobeVisualAngle + i * 4.0f).toDouble())
                val dotX = center.x + outerRadius * kotlin.math.cos(dotAngleRad).toFloat()
                val dotY = center.y + outerRadius * kotlin.math.sin(dotAngleRad).toFloat()

                drawCircle(
                    color = Color(0xFFD4DCE8),
                    radius = 2.2.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
            }
        }

        // Quartz Lock Speed Indicator Badge
        if (currentRpmFactor >= 0.99f && !isTouchingVinyl) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 28.dp, start = 28.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF0D2818))
                    .border(0.8.dp, FluorescentGreen, RoundedCornerShape(3.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "QUARTZ LOCK",
                    color = FluorescentGreen,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * Independent Vinyl Record Entity (Disco de Vinil)
 */
@Composable
private fun VinylRecord(
    albumArtPainter: Painter?,
    rotationAngle: Float,
    discOffsetX: Float,
    discAlpha: Float,
    glassBlurRadius: Dp,
    isLidClosed: Boolean,
    onTouchStart: () -> Unit,
    onTouchEnd: () -> Unit,
    onDragScratch: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .offset(x = discOffsetX.dp)
            .graphicsLayer { alpha = discAlpha }
            .rotate(rotationAngle)
            .blur(glassBlurRadius)
            .clip(CircleShape)
            .background(Color(0xFF07080B))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onTouchStart() },
                    onDragEnd = { onTouchEnd() },
                    onDragCancel = { onTouchEnd() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDragScratch(dragAmount.x + dragAmount.y)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Vinyl Micro-Grooves Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val maxR = this.size.width / 2f

            for (r in (maxR * 0.35f).toInt()..(maxR * 0.96f).toInt() step 4) {
                drawCircle(
                    color = Color(0xFF222730),
                    radius = r.toFloat(),
                    center = center,
                    style = Stroke(width = 1.1.dp.toPx())
                )
            }

            // High Gloss Specular Rainbow Reflective Arc Sweeps
            val shinePath1 = Path().apply {
                moveTo(center.x, center.y)
                arcTo(
                    rect = Rect(
                        center.x - maxR, center.y - maxR,
                        center.x + maxR, center.y + maxR
                    ),
                    startAngleDegrees = -35f,
                    sweepAngleDegrees = 45f,
                    forceMoveTo = false
                )
                close()
            }
            drawPath(
                path = shinePath1,
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.25f), Color.Transparent),
                    center = center,
                    radius = maxR
                )
            )

            val shinePath2 = Path().apply {
                moveTo(center.x, center.y)
                arcTo(
                    rect = Rect(
                        center.x - maxR, center.y - maxR,
                        center.x + maxR, center.y + maxR
                    ),
                    startAngleDegrees = 145f,
                    sweepAngleDegrees = 45f,
                    forceMoveTo = false
                )
                close()
            }
            drawPath(
                path = shinePath2,
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.25f), Color.Transparent),
                    center = center,
                    radius = maxR
                )
            )
        }

        // Center Album Art Label
        Box(
            modifier = Modifier
                .fillMaxSize(0.38f)
                .clip(CircleShape)
                .background(VintageAmber)
                .border(2.5.dp, BrassGold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Crossfade<Painter?>(
                targetState = albumArtPainter,
                animationSpec = tween(durationMillis = 400),
                label = "labelCrossfade"
            ) { painter ->
                if (painter != null) {
                    Image(
                        painter = painter,
                        contentDescription = "Vinyl Label",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        VintageAmber,
                                        Color(0xFFC47B00)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "4K HI-FI\nAUDIO",
                            color = MetalDarkBackground,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Center Hole Rim
            Box(
                modifier = Modifier
                    .size(13.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF101216))
                    .border(1.5.dp, MetalBevelLight, CircleShape)
            )
        }
    }
}

/**
 * 3D Volumetric Smoked Acrylic Dust Cover Box (Cúpula Acrílica 3D)
 * Renders 3D perspective depth, 4 lateral walls with thickness, lapidated edges,
 * specular reflection sweeps, and internal optical refraction.
 */
@Composable
private fun DustCover3D(
    lidRotationX: Float,
    lidAlpha: Float,
    lidReflectionFactor: Float,
    isLidClosed: Boolean,
    onToggleLid: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .graphicsLayer {
                cameraDistance = 18f * density
                transformOrigin = TransformOrigin(0.5f, 0.04f) // Pivot at top rear hinge line
                rotationX = lidRotationX
                alpha = lidAlpha
            }
            .clip(RoundedCornerShape(14.dp))
            .clickable { onToggleLid() }
    ) {
        // Volumetric 3D Box Canvas (Top face + 4 thick lateral walls + specular bevels)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val wallThickness = 12.dp.toPx() // 6-8mm acrylic wall thickness illusion

            // 1. Smoked Acrylic Box Base Top Surface
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0x48353C4D),
                        Color(0x221A1E26),
                        Color(0x420E1117),
                        Color(0x302C3646)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(w, h)
                ),
                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
            )

            // 2. Thick Front Wall
            val frontPath = Path().apply {
                moveTo(0f, h - wallThickness)
                lineTo(w, h - wallThickness)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = frontPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x704B5466), Color(0x901A1D24))
                )
            )

            // 3. Thick Left Side Wall
            val leftPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(wallThickness, 0f)
                lineTo(wallThickness, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = leftPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0x805C667B), Color(0x30202530))
                )
            )

            // 4. Thick Right Side Wall
            val rightPath = Path().apply {
                moveTo(w - wallThickness, 0f)
                lineTo(w, 0f)
                lineTo(w, h)
                lineTo(w - wallThickness, h)
                close()
            }
            drawPath(
                path = rightPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0x30202530), Color(0x805C667B))
                )
            )

            // 5. Polished Bevelled Edge Outlines (Specular Highlights)
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.85f * lidReflectionFactor),
                        Color.White.copy(alpha = 0.20f * lidReflectionFactor),
                        Color.White.copy(alpha = 0.65f * lidReflectionFactor)
                    )
                ),
                style = Stroke(width = 2.dp.toPx()),
                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
            )
        }

        // Sliding Specular Reflection Streak 1
        Box(
            modifier = Modifier
                .fillMaxWidth(1.5f)
                .height(45.dp)
                .offset(x = (-35).dp, y = 25.dp)
                .rotate(-32f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.38f * lidReflectionFactor),
                            Color.Transparent
                        )
                    )
                )
                .blur(2.dp)
        )

        // Sliding Specular Reflection Streak 2
        Box(
            modifier = Modifier
                .fillMaxWidth(1.3f)
                .height(22.dp)
                .offset(x = (-25).dp, y = 90.dp)
                .rotate(-32f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.28f * lidReflectionFactor),
                            Color.Transparent
                        )
                    )
                )
                .blur(2.dp)
        )

        // Front Glass Latch Handle
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF383E4A), Color(0xFF1B1E26))
                    )
                )
                .border(1.dp, MetalBevelLight, RoundedCornerShape(5.dp))
                .padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isLidClosed) "CÚPULA ACRÍLICA 3D FECHADA (REFLEXO • LUZ)" else "CÚPULA ACRÍLICA 3D ABERTA (72°)",
                color = if (isLidClosed) FluorescentGreen else VintageAmber,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun ScrewHead() {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(8.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF8A95A5), Color(0xFF2B303A))
                )
            )
            .border(0.8.dp, Color.Black, CircleShape)
    )
}

@Composable
private fun DeckHingeMount() {
    Box(
        modifier = Modifier
            .width(26.dp)
            .height(12.dp)
            .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF9AA5B5), Color(0xFF21252D))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
    )
}
