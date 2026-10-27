package com.jacks.moviet.core.designsystem.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import coil3.compose.AsyncImage
import com.jacks.moviet.R
import com.jacks.moviet.core.domain.model.Movie

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MoviePoster(
    modifier: Modifier = Modifier,
    movie: Movie,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val namespace = LocalMovieSharedContentNamespace.current
    val transitionModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(key = namespace to "poster-${movie.imdbId}"),
                animatedVisibilityScope = animatedVisibilityScope,
                boundsTransform = MovieBoundsTransform,
            )
        }
    } else Modifier
    AsyncImage(
        modifier = transitionModifier.then(modifier).clip(MaterialTheme.shapes.small),
        model = movie.posterUrl,
        contentDescription = stringResource(R.string.movie_poster, movie.title),
        placeholder = painterResource(R.drawable.ic_poster_placeholder),
        error = painterResource(R.drawable.ic_poster_placeholder),
        fallback = painterResource(R.drawable.ic_poster_placeholder),
        contentScale = ContentScale.Crop,
    )
}
