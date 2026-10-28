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
    Text(
        modifier = Modifier.movieSharedTextBounds(
            key = "title-${movie.imdbId}",
            textAlign = textAlign,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        ).then(modifier),
        text = movie.title,
        style = style,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}
