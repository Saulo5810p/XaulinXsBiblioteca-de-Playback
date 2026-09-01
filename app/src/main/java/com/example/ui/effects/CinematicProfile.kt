package com.example.ui.effects

/**
 * Perfis de física e renderização para o Sistema Cinematográfico Global.
 * Cada perfil define massa, tensão da mola, amortecimento, escala, rotação
 * e multiplicadores de desfoque/aberração cromática específicos para o seu contexto.
 */
enum class CinematicProfile {
    MICRO_INTERACTION,
    BUTTON_IMPACT,
    MEDIA_CONTROL,
    LIST_ITEM,
    TAB_TRANSITION,
    SCREEN_TRANSITION,
    NOW_PLAYING_ENTER,
    NOW_PLAYING_EXIT,
    SEEKBAR_DRAG,
    DIALOG_OPEN,
    DIALOG_CLOSE,
    HERO_TRANSITION
}

data class CinematicProfileSpec(
    val mass: Float = 1.0f,
    val stiffness: Float = 220.0f,
    val damping: Float = 14.0f,
    val maxRotationDegrees: Float = 360f,
    val pressScale: Float = 0.90f,
    val overshootScale: Float = 1.15f,
    val blurMultiplier: Float = 1.0f,
    val chromaticMultiplier: Float = 1.0f,
    val vignetteMultiplier: Float = 0.5f,
    val perspectiveMultiplier: Float = 0.2f,
    val durationMs: Long = 420L
)

object CinematicProfiles {

    fun getSpec(profile: CinematicProfile): CinematicProfileSpec {
        return when (profile) {
            CinematicProfile.MICRO_INTERACTION -> CinematicProfileSpec(
                mass = 0.8f,
                stiffness = 320.0f,
                damping = 18.0f,
                maxRotationDegrees = 15f,
                pressScale = 0.96f,
                overshootScale = 1.04f,
                blurMultiplier = 0.4f,
                chromaticMultiplier = 0.3f,
                vignetteMultiplier = 0.1f,
                durationMs = 200L
            )

            CinematicProfile.BUTTON_IMPACT -> CinematicProfileSpec(
                mass = 1.0f,
                stiffness = 240.0f,
                damping = 11.0f,
                maxRotationDegrees = 360f,
                pressScale = 0.82f,
                overshootScale = 1.30f,
                blurMultiplier = 1.8f,
                chromaticMultiplier = 1.6f,
                vignetteMultiplier = 0.6f,
                durationMs = 420L
            )

            CinematicProfile.MEDIA_CONTROL -> CinematicProfileSpec(
                mass = 1.1f,
                stiffness = 200.0f,
                damping = 9.0f,
                maxRotationDegrees = 360f,
                pressScale = 0.78f,
                overshootScale = 1.38f,
                blurMultiplier = 2.2f,
                chromaticMultiplier = 2.0f,
                vignetteMultiplier = 0.7f,
                durationMs = 500L
            )

            CinematicProfile.LIST_ITEM -> CinematicProfileSpec(
                mass = 0.9f,
                stiffness = 260.0f,
                damping = 12.0f,
                maxRotationDegrees = 45f,
                pressScale = 0.90f,
                overshootScale = 1.15f,
                blurMultiplier = 1.4f,
                chromaticMultiplier = 1.2f,
                vignetteMultiplier = 0.4f,
                durationMs = 320L
            )

            CinematicProfile.TAB_TRANSITION -> CinematicProfileSpec(
                mass = 1.2f,
                stiffness = 160.0f,
                damping = 8.0f,
                maxRotationDegrees = 360f,
                pressScale = 0.78f,
                overshootScale = 1.35f,
                blurMultiplier = 2.6f,
                chromaticMultiplier = 2.2f,
                vignetteMultiplier = 0.8f,
                durationMs = 560L
            )

            CinematicProfile.SCREEN_TRANSITION -> CinematicProfileSpec(
                mass = 1.4f,
                stiffness = 140.0f,
                damping = 7.0f,
                maxRotationDegrees = 720f,
                pressScale = 0.65f,
                overshootScale = 1.45f,
                blurMultiplier = 3.2f,
                chromaticMultiplier = 2.8f,
                vignetteMultiplier = 0.95f,
                durationMs = 680L
            )

            CinematicProfile.NOW_PLAYING_ENTER -> CinematicProfileSpec(
                mass = 1.5f,
                stiffness = 130.0f,
                damping = 6.5f,
                maxRotationDegrees = 720f,
                pressScale = 0.60f,
                overshootScale = 1.50f,
                blurMultiplier = 3.6f,
                chromaticMultiplier = 3.0f,
                vignetteMultiplier = 1.0f,
                durationMs = 750L
            )

            CinematicProfile.NOW_PLAYING_EXIT -> CinematicProfileSpec(
                mass = 1.3f,
                stiffness = 170.0f,
                damping = 8.5f,
                maxRotationDegrees = -360f,
                pressScale = 0.75f,
                overshootScale = 1.25f,
                blurMultiplier = 2.6f,
                chromaticMultiplier = 2.2f,
                vignetteMultiplier = 0.8f,
                durationMs = 600L
            )

            CinematicProfile.SEEKBAR_DRAG -> CinematicProfileSpec(
                mass = 0.8f,
                stiffness = 300.0f,
                damping = 16.0f,
                maxRotationDegrees = 25f,
                pressScale = 0.92f,
                overshootScale = 1.12f,
                blurMultiplier = 1.2f,
                chromaticMultiplier = 1.1f,
                vignetteMultiplier = 0.3f,
                durationMs = 250L
            )

            CinematicProfile.DIALOG_OPEN -> CinematicProfileSpec(
                mass = 1.1f,
                stiffness = 190.0f,
                damping = 12.0f,
                maxRotationDegrees = 10f,
                pressScale = 0.80f,
                overshootScale = 1.15f,
                blurMultiplier = 1.1f,
                chromaticMultiplier = 1.0f,
                vignetteMultiplier = 0.5f,
                durationMs = 400L
            )

            CinematicProfile.DIALOG_CLOSE -> CinematicProfileSpec(
                mass = 0.9f,
                stiffness = 250.0f,
                damping = 15.0f,
                maxRotationDegrees = -10f,
                pressScale = 0.85f,
                overshootScale = 1.05f,
                blurMultiplier = 0.8f,
                chromaticMultiplier = 0.7f,
                vignetteMultiplier = 0.3f,
                durationMs = 300L
            )

            CinematicProfile.HERO_TRANSITION -> CinematicProfileSpec(
                mass = 1.6f,
                stiffness = 120.0f,
                damping = 8.0f,
                maxRotationDegrees = 360f,
                pressScale = 0.65f,
                overshootScale = 1.40f,
                blurMultiplier = 2.8f,
                chromaticMultiplier = 2.2f,
                vignetteMultiplier = 1.0f,
                durationMs = 700L
            )
        }
    }
}
