package com.jacks.moviet.core.designsystem.components

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateRect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.jacks.moviet.core.designsystem.theme.MovietMotion

// Lists and their details share a namespace; separate tabs must never match each other.
internal val LocalMovieSharedContentNamespace = compositionLocalOf<Any> { Unit }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Modifier.movieSharedTextBounds(
    key: String,
    textAlign: TextAlign,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
): Modifier {
    if (sharedTransitionScope == null || animatedVisibilityScope == null) return this
    val namespace = LocalMovieSharedContentNamespace.current
    val overlayClip = LocalMovieSharedOverlayClip.current
    return with(sharedTransitionScope) {
        sharedBounds(
            sharedContentState = rememberSharedContentState(key = namespace to key),
            animatedVisibilityScope = animatedVisibilityScope,
            boundsTransform = MovieBoundsTransform,
            clipInOverlayDuringTransition = overlayClip,
            // Scale each endpoint's text layout without rewrapping it on every frame.
            resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(
                contentScale = ContentScale.Fit,
                alignment = if (textAlign == TextAlign.Center) Alignment.Center else Alignment.CenterStart,
            ),
        )
    }
}

// Lists animate their overlay boundary with navigation, allowing shared content to travel freely.
@OptIn(ExperimentalSharedTransitionApi::class)
internal val LocalMovieSharedOverlayClip = compositionLocalOf<SharedTransitionScope.OverlayClip> {
    NoMovieSharedOverlayClip
}

@OptIn(ExperimentalSharedTransitionApi::class)
private object NoMovieSharedOverlayClip : SharedTransitionScope.OverlayClip {
    override fun getClipPath(
        sharedContentState: SharedTransitionScope.SharedContentState,
        bounds: Rect,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Path? = null
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun rememberMovieListOverlayClip(
    animatedVisibilityScope: AnimatedVisibilityScope?,
    rootCoordinates: LayoutCoordinates?,
    headerBottomPx: Float,
): SharedTransitionScope.OverlayClip {
    // Phone destinations fill this root; the list header includes the screen's top system inset.
    val fullBounds = rootCoordinates?.takeIf { it.isAttached }?.size?.let { size ->
        Rect(0f, 0f, size.width.toFloat(), size.height.toFloat())
    } ?: Rect.Zero
    val clipBounds = if (animatedVisibilityScope != null) {
        animatedVisibilityScope.transition.animateRect(
            // Matching the poster's spring keeps the boundary and image moving together when sought.
            transitionSpec = {
                MovieBoundsTransform.createAnimationSpec(
                    movieListClipBounds(fullBounds, headerBottomPx, initialState == EnterExitState.Visible),
                    movieListClipBounds(fullBounds, headerBottomPx, targetState == EnterExitState.Visible),
                )
            },
            label = "Movie list header clip",
        ) { state ->
            movieListClipBounds(fullBounds, headerBottomPx, state == EnterExitState.Visible)
        }
    } else {
        rememberUpdatedState(movieListClipBounds(fullBounds, headerBottomPx, listVisible = true))
    }
    return remember(clipBounds) { MovieListOverlayClip(clipBounds) }
}

internal fun movieListClipBounds(fullBounds: Rect, headerBottomPx: Float, listVisible: Boolean): Rect =
    fullBounds.copy(
        top = if (listVisible) headerBottomPx.coerceIn(fullBounds.top, fullBounds.bottom) else fullBounds.top,
    )

@OptIn(ExperimentalSharedTransitionApi::class)
private class MovieListOverlayClip(private val clipBounds: State<Rect>) : SharedTransitionScope.OverlayClip {
    private val path = Path()

    override fun getClipPath(
        sharedContentState: SharedTransitionScope.SharedContentState,
        bounds: Rect,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Path? {
        val visibleBounds = clipBounds.value
        if (visibleBounds.isEmpty) return null
        path.reset()
        path.addRect(visibleBounds)
        return path
    }
}

// Keep the poster, title and metadata moving together between list and details layouts.
@OptIn(ExperimentalSharedTransitionApi::class)
internal val MovieBoundsTransform = BoundsTransform { _, _ -> MovietMotion.slowSpatial() }
