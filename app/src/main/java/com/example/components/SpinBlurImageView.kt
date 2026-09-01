package com.example.components

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import com.example.ui.effects.CinematicShader

/**
 * SEÇÃO 3: CUSTOM VIEW PREMIUM COM CANVAS RECOMPILADO (Tela de Reprodução)
 *
 * `SpinBlurImageView`: View customizada de alto impacto visual para o Player.
 * Recursos:
 * 1. Transformações de Matriz 3D (Escala + Rotação + Skew/Inclinação Perspectiva)
 * 2. Motion Tile Infinito via `BitmapShader` em `TileMode.MIRROR`
 * 3. Máscara de Vinheta Radial com Blend Mode `PorterDuff.Mode.MULTIPLY` para atmosfera de cinema
 * 4. Desfoque AGSL + Aberração Cromática em tempo real
 */
class SpinBlurImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

    var animDurationMs: Long = 480L
    var maxRotationDegrees: Float = 360f
    var maxBlurRadius: Float = 32f

    private val overshootInterpolator = OvershootInterpolator(2.4f)

    private var valueAnimator: ValueAnimator? = null
    private var isAnimating = false
    private var currentProgress = 1.0f
    private var currentScale = 1.0f
    private var currentRotation = 0.0f
    private var currentSkewX = 0.0f
    private var currentBlurRadius = 0.0f

    // Reutilização rigorosa de matrizes e objetos de desenho (Evita alocação no ciclo de 120 FPS)
    private val shaderMatrix = Matrix()
    private val tilePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var cachedBitmapShader: BitmapShader? = null
    private var cachedVignetteShader: RadialGradient? = null
    private var lastBitmap: Bitmap? = null
    private var lastWidth = 0
    private var lastHeight = 0

    init {
        vignettePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
        setOnClickListener {
            startSpinBlurAnimation()
        }
    }

    /**
     * Inicia a transição Cinematic Spin Blur.
     */
    fun startSpinBlurAnimation(onComplete: (() -> Unit)? = null) {
        valueAnimator?.cancel()
        isAnimating = true
        currentProgress = 0f

        valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = animDurationMs
            interpolator = overshootInterpolator

            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float
                currentProgress = progress

                // 1. Escala Elástica Overshoot Massiva (1.0 -> 1.25 -> 1.0)
                currentScale = if (progress < 1.0f) {
                    val overshoot = kotlin.math.sin(progress * Math.PI.toFloat()).toFloat() * 0.28f
                    1.0f + overshoot
                } else {
                    1.0f
                }

                // 2. Rotação Kinetic 360°
                currentRotation = progress * maxRotationDegrees

                // 3. Inclinação Skew Perspectiva 3D
                currentSkewX = if (progress < 1.0f) {
                    kotlin.math.sin(progress * Math.PI.toFloat() * 2f).toFloat() * 0.15f
                } else {
                    0f
                }

                // 4. Desfoque Dinâmico AGSL + Aberração Cromática RGB
                currentBlurRadius = if (progress < 1.0f) {
                    kotlin.math.sin(progress * Math.PI.toFloat()).toFloat() * maxBlurRadius
                } else {
                    0f
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (currentBlurRadius > 0.5f) {
                        val w = width.toFloat().coerceAtLeast(100f)
                        val h = height.toFloat().coerceAtLeast(100f)

                        val effect = CinematicShader.createCinematicEffect(
                            width = w,
                            height = h,
                            rotationSpeed = (1f - progress) * 3f,
                            scaleFactor = currentScale,
                            blurIntensity = currentBlurRadius / maxBlurRadius,
                            chromaticShift = (currentBlurRadius / maxBlurRadius) * 0.9f
                        )
                        setRenderEffect(effect)
                    } else {
                        setRenderEffect(null)
                    }
                }

                postInvalidateOnAnimation()
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isAnimating = false
                    currentProgress = 1.0f
                    currentScale = 1.0f
                    currentRotation = 0.0f
                    currentSkewX = 0.0f
                    currentBlurRadius = 0.0f

                    // Limpeza de recursos de GPU
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        setRenderEffect(null)
                    }

                    postInvalidate()
                    onComplete?.invoke()
                }
            })

            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (!isAnimating) {
            super.onDraw(canvas)
            return
        }

        val bitmap = (drawable as? BitmapDrawable)?.bitmap
        if (bitmap == null || width == 0 || height == 0) {
            super.onDraw(canvas)
            return
        }

        val cx = width / 2f
        val cy = height / 2f

        // Recria os shaders apenas quando o Bitmap ou tamanho da View muda
        if (cachedBitmapShader == null || lastBitmap != bitmap) {
            lastBitmap = bitmap
            cachedBitmapShader = BitmapShader(bitmap, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR)
        }

        if (cachedVignetteShader == null || lastWidth != width || lastHeight != height) {
            lastWidth = width
            lastHeight = height
            val radius = Math.hypot(cx.toDouble(), cy.toDouble()).toFloat()
            cachedVignetteShader = RadialGradient(
                cx, cy, radius,
                intArrayOf(0xFFFFFFFF.toInt(), 0xFF666666.toInt(), 0xFF000000.toInt()),
                floatArrayOf(0.0f, 0.65f, 1.0f),
                Shader.TileMode.CLAMP
            )
            vignettePaint.shader = cachedVignetteShader
        }

        val shader = cachedBitmapShader
        if (shader != null) {
            // MATEMÁTICA DE MATRIZES 3D (Escala + Rotação + Skew)
            shaderMatrix.reset()
            val scaleX = width.toFloat() / bitmap.width.toFloat()
            val scaleY = height.toFloat() / bitmap.height.toFloat()

            shaderMatrix.postScale(scaleX * currentScale, scaleY * currentScale, bitmap.width / 2f, bitmap.height / 2f)
            shaderMatrix.postSkew(currentSkewX, 0f, bitmap.width / 2f, bitmap.height / 2f)
            shaderMatrix.postRotate(currentRotation, bitmap.width / 2f, bitmap.height / 2f)
            shaderMatrix.postTranslate(cx - (bitmap.width / 2f), cy - (bitmap.height / 2f))

            shader.setLocalMatrix(shaderMatrix)
            tilePaint.shader = shader

            // 1. Desenha o fundo infinito com Motion Tile (MIRROR)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), tilePaint)

            // 2. Aplica a camada de Vinheta Sombreada com Blend Mode MULTIPLY
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), vignettePaint)
        } else {
            super.onDraw(canvas)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        valueAnimator?.cancel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            setRenderEffect(null)
        }
    }
}
