package com.jacks.moviet.details.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.jacks.moviet.core.designsystem.theme.MovietMotion
import com.jacks.moviet.core.designsystem.theme.Spacing

@Composable
internal fun Modifier.detailsContentReveal(imdbId: String?, isLoaded: Boolean): Modifier = key(imdbId) {
    val inspectionMode = LocalInspectionMode.current
    var revealed by rememberSaveable { mutableStateOf(inspectionMode) }
    val opacity = animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = MovietMotion.defaultEffects(),
        label = "Details content opacity",
    )
    val offset = animateDpAsState(
        targetValue = if (revealed) 0.dp else Spacing.medium,
        animationSpec = MovietMotion.defaultSpatial(),
        label = "Details content offset",
    )
    LaunchedEffect(isLoaded) {
        if (isLoaded) revealed = true
    }
    // Share one reveal across the lazy items; scrolling and favorite updates must not replay it.
    // Keep their measured sizes stable so the shared header and scroll position do not move.
    graphicsLayer {
        alpha = opacity.value
        translationY = offset.value.toPx()
    }
}
