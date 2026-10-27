package com.jacks.moviet.app.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.Scene
import com.jacks.moviet.core.designsystem.theme.MovietMotion

internal fun topLevelNavigation(route: NavigationRoute) = metadata { put(TopLevelRouteKey, route) }

private object TopLevelRouteKey : NavMetadataKey<NavigationRoute>

internal fun isTopLevelSwitch(initialScene: Scene<*>, targetScene: Scene<*>): Boolean {
    val initialRoute = initialScene.topLevelRoute()
    val targetRoute = targetScene.topLevelRoute()
    return initialRoute != null && targetRoute != null && initialRoute != targetRoute
}

private fun Scene<*>.topLevelRoute(): NavigationRoute? {
    val entry = if (this is ListDetailScene<*>) listEntry else entries.lastOrNull()
    return entry?.metadata?.get(TopLevelRouteKey)
}

// Material fade-through pattern with the app's standard spatial spring.
// MaterialTransitions is unavailable in pinned Material 3 1.4.
// https://github.com/androidx/androidx/blob/androidx-main/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/MaterialTransitions.kt
internal fun fadeThrough(): ContentTransform =
    (fadeIn(tween(FadeInMillis, delayMillis = FadeOutMillis, easing = LinearEasing)) +
        scaleIn(animationSpec = MovietMotion.slowSpatial(), initialScale = IncomingScale)) togetherWith
        (fadeOut(tween(FadeOutMillis, easing = LinearEasing)) +
            scaleOut(animationSpec = MovietMotion.slowSpatial(), targetScale = OutgoingScale))

/** Material horizontal shared-axis pattern; posters and both screens share the spatial spec. */
internal fun horizontalSharedAxis(forward: Boolean, layoutDirection: LayoutDirection): ContentTransform {
    val direction = (if (forward) 1 else -1) * (if (layoutDirection == LayoutDirection.Ltr) 1 else -1)
    val fadeSpec = tween<Float>(HorizontalFadeMillis, delayMillis = HorizontalFadeDelayMillis, easing = LinearEasing)
    return ((slideInHorizontally(MovietMotion.slowSpatial()) { (it * HorizontalSlideFraction * direction).toInt() } +
        fadeIn(fadeSpec)) togetherWith
        (slideOutHorizontally(MovietMotion.slowSpatial()) { (-it * HorizontalSlideFraction * direction).toInt() } +
            fadeOut(fadeSpec))).apply {
        if (!forward) targetContentZIndex = BackwardTargetContentZIndex
    }
}

private const val FadeOutMillis = 90
private const val FadeInMillis = 210
private const val IncomingScale = 0.8f
private const val OutgoingScale = 1.05f
private const val HorizontalSlideFraction = 0.2f
private const val HorizontalFadeMillis = 90
private const val HorizontalFadeDelayMillis = 50
private const val BackwardTargetContentZIndex = -10f
