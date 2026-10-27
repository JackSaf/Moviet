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
import com.jacks.moviet.core.domain.model.Movie
import com.jacks.moviet.core.presentation.util.displayName

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MovieMetadata(
    modifier: Modifier = Modifier,
    movie: Movie,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    textAlign: TextAlign = TextAlign.Start,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val metadata = listOfNotNull(movie.year, movie.type.displayName())
    if (metadata.isEmpty()) return

    val namespace = LocalMovieSharedContentNamespace.current
    val transitionModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            // Keep details-only metadata outside these shared text bounds.
            Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState(key = namespace to "metadata-${movie.imdbId}"),
                animatedVisibilityScope = animatedVisibilityScope,
                boundsTransform = MovieBoundsTransform,
                resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(
                    contentScale = ContentScale.Fit,
                    alignment = if (textAlign == TextAlign.Center) Alignment.Center else Alignment.CenterStart,
                ),
            )
        }
    } else Modifier

    Text(
        modifier = transitionModifier.then(modifier),
        text = metadata.joinToString(" · "),
        style = style,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = textAlign,
    )
}
