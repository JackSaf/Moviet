package com.jacks.moviet.core.designsystem.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

    Text(
        modifier = Modifier.movieSharedTextBounds(
            key = "metadata-${movie.imdbId}",
            textAlign = textAlign,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        ).then(modifier),
        text = metadata.joinToString(" · "),
        style = style,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = textAlign,
    )
}
