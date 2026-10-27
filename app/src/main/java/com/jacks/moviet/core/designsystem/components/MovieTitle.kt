package com.jacks.moviet.core.designsystem.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.jacks.moviet.core.domain.model.Movie

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MovieTitle(
    modifier: Modifier = Modifier,
    movie: Movie,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val namespace = LocalMovieSharedContentNamespace.current
    val overlayClip = LocalMovieSharedOverlayClip.current
    val transitionModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState(key = namespace to "title-${movie.imdbId}"),
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
    } else Modifier
    Text(
        modifier = transitionModifier.then(modifier),
        text = movie.title,
        style = style,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}
