package com.example.ui.effects

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator

/**
 * MOTOR GLOBAL DE INTERCEPTAÇÃO E ANIMAÇÃO PARA ANDROID NATIVO / CUSTOM VIEWS
 *
 * Aplica o sistema cinematográfico de mola, rotação 3D, desfoque AGSL e aberração
 * cromática para qualquer hierarquia de Views Android.
 */
object GlobalMotionEngine {

    private val defaultInterpolator = OvershootInterpolator(2.2f)

    /**
     * Aplica o efeito cinematográfico recursivamente a uma View ou ViewGroup inteiro.
     */
    fun applyToAllViews(rootView: View, profile: CinematicProfile = CinematicProfile.BUTTON_IMPACT) {
        if (rootView is ViewGroup) {
            for (i in 0 until rootView.childCount) {
                applyToAllViews(rootView.getChildAt(i), profile)
            }
        } else {
            if (rootView.isClickable || rootView.hasOnClickListeners()) {
                applyGlobalCinematicMotion(rootView, profile)
            }
        }
    }

    /**
     * Injeta interceptação de toque com compressão, física de mola e RenderEffect AGSL.
     */
    fun applyGlobalCinematicMotion(view: View, profile: CinematicProfile = CinematicProfile.BUTTON_IMPACT) {
        val spec = CinematicProfiles.getSpec(profile)

        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate()
                        .scaleX(spec.pressScale)
                        .scaleY(spec.pressScale)
                        .rotation(-spec.maxRotationDegrees * 0.08f)
                        .setDuration(80)
                        .start()
                    false
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    triggerCinematicAnimation(v, profile)
                    false
                }
                else -> false
            }
        }
    }

    /**
     * Dispara a animação elástica de giro, desfoque e recuperação com base no perfil.
     */
    fun triggerCinematicAnimation(
        view: View,
        profile: CinematicProfile = CinematicProfile.BUTTON_IMPACT,
        onComplete: (() -> Unit)? = null
    ) {
        val spec = CinematicProfiles.getSpec(profile)

        val springSim = SpringSimulation(
            mass = spec.mass,
            stiffness = spec.stiffness,
            damping = spec.damping,
            initialPosition = 0f,
            targetPosition = 1f
        )

        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = spec.durationMs
            interpolator = defaultInterpolator

            addUpdateListener { anim ->
                val progress = anim.animatedValue as Float
                springSim.position = progress
                val state = springSim.calculateMotionState(spec)

                view.scaleX = state.scale
                view.scaleY = state.scale
                view.rotation = state.rotation

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (state.blur > 0.05f) {
                        val w = view.width.toFloat().coerceAtLeast(100f)
                        val h = view.height.toFloat().coerceAtLeast(100f)

                        val effect = CinematicShader.createCinematicEffect(
                            width = w,
                            height = h,
                            rotationSpeed = state.angularVelocity,
                            scaleFactor = state.scale,
                            blurIntensity = state.blur / 20f,
                            chromaticShift = state.chromaticShift,
                            vignetteIntensity = state.vignette
                        )
                        view.setRenderEffect(effect)
                    } else {
                        view.setRenderEffect(null)
                    }
                }
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.scaleX = 1.0f
                    view.scaleY = 1.0f
                    view.rotation = 0.0f

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        view.setRenderEffect(null)
                    }
                    onComplete?.invoke()
                }
            })
        }
        animator.start()
    }
}
