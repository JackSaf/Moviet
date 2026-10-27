package com.jacks.moviet.core.designsystem.theme

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

/**
 * Material 3 standard motion tokens, matching the pinned Material 3 1.4.0 components.
 * Its MotionScheme API is internal, so custom UI shares these specs without accessing internals.
 * Values: androidx.compose.material3.tokens.StandardMotionTokens.
 */
object MovietMotion {
    fun <T> fastSpatial(): SpringSpec<T> = spring(
        dampingRatio = SpatialDamping,
        stiffness = FastSpatialStiffness,
    )

    fun <T> defaultSpatial(): SpringSpec<T> = spring(
        dampingRatio = SpatialDamping,
        stiffness = DefaultSpatialStiffness,
    )

    fun <T> slowSpatial(): SpringSpec<T> = spring(
        dampingRatio = SpatialDamping,
        stiffness = SlowSpatialStiffness,
    )

    fun <T> fastEffects(): SpringSpec<T> = spring(
        dampingRatio = EffectsDamping,
        stiffness = FastEffectsStiffness,
    )

    fun <T> defaultEffects(): SpringSpec<T> = spring(
        dampingRatio = EffectsDamping,
        stiffness = DefaultEffectsStiffness,
    )

    private const val SpatialDamping = 0.9f
    private const val EffectsDamping = 1f
    private const val FastSpatialStiffness = 1400f
    private const val DefaultSpatialStiffness = 700f
    private const val SlowSpatialStiffness = 300f
    private const val FastEffectsStiffness = 3800f
    private const val DefaultEffectsStiffness = 1600f
}
