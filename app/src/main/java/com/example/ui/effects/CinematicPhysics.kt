package com.example.ui.effects

import kotlin.math.abs
import kotlin.math.sin

/**
 * Estado Físico e Óptico Calculado Centralizado.
 * Todas as propriedades derivam da mesma simulação física de mola e velocidade.
 */
data class CinematicMotionState(
    val progress: Float = 0.0f,
    val velocity: Float = 0.0f,
    val angularVelocity: Float = 0.0f,
    val scale: Float = 1.0f,
    val rotation: Float = 0.0f,
    val skewX: Float = 0.0f,
    val blur: Float = 0.0f,
    val chromaticShift: Float = 0.0f,
    val vignette: Float = 0.0f,
    val perspectiveY: Float = 0.0f
)

/**
 * FÍSICA REALÍSTICA DE MOLAS (SPRING DYNAMICS ENGINE)
 * Simula massa, tensão da mola (stiffness), amortecimento (damping) e impulsos cinéticos.
 */
class SpringSimulation(
    var mass: Float = 1.0f,
    var stiffness: Float = 200.0f,
    var damping: Float = 12.0f,
    var initialPosition: Float = 0.0f,
    var targetPosition: Float = 1.0f
) {
    var position: Float = initialPosition
    var velocity: Float = 0.0f

    fun reset(initialPos: Float = 0f, targetPos: Float = 1f, initialVel: Float = 0f) {
        position = initialPos
        targetPosition = targetPos
        velocity = initialVel
    }

    fun applyImpulse(impulse: Float) {
        velocity += impulse / mass
    }

    fun update(dt: Float): Boolean {
        if (dt <= 0f) return isAtRest()

        val displacement = position - targetPosition
        val springForce = -stiffness * displacement
        val dampingForce = -damping * velocity
        val acceleration = (springForce + dampingForce) / mass

        velocity += acceleration * dt
        position += velocity * dt

        return isAtRest()
    }

    fun isAtRest(posTolerance: Float = 0.001f, velTolerance: Float = 0.01f): Boolean {
        return abs(position - targetPosition) < posTolerance && abs(velocity) < velTolerance
    }

    val kineticEnergy: Float
        get() = 0.5f * mass * velocity * velocity

    /**
     * Calcula o estado óptico/cinematográfico para a simulação atual.
     */
    fun calculateMotionState(spec: CinematicProfileSpec): CinematicMotionState {
        val currentVel = velocity
        val isMoving = !isAtRest()

        val scale = if (isMoving) {
            val env = sin(position.coerceIn(0f, 1f) * Math.PI.toFloat())
            1.0f + (spec.overshootScale - 1.0f) * env * (1f - position.coerceIn(0f, 1f))
        } else {
            1.0f
        }

        val rotation = if (isMoving) {
            (1f - position.coerceIn(0f, 1f)) * spec.maxRotationDegrees
        } else {
            0.0f
        }

        val blurVal = if (isMoving) {
            abs(currentVel) * 0.08f * spec.blurMultiplier
        } else 0f

        val chromaticVal = if (isMoving) {
            blurVal * 0.8f * spec.chromaticMultiplier
        } else 0f

        val vignetteVal = if (isMoving) {
            blurVal * 0.25f * spec.vignetteMultiplier
        } else 0f

        val skewVal = if (isMoving) {
            sin(position * Math.PI.toFloat() * 2f).toFloat() * 0.12f * spec.perspectiveMultiplier
        } else 0f

        return CinematicMotionState(
            progress = position,
            velocity = currentVel,
            angularVelocity = currentVel * 2f,
            scale = scale,
            rotation = rotation,
            skewX = skewVal,
            blur = blurVal.coerceIn(0f, 40f),
            chromaticShift = chromaticVal.coerceIn(0f, 2.5f),
            vignette = vignetteVal.coerceIn(0f, 0.9f),
            perspectiveY = skewVal * 15f
        )
    }
}
