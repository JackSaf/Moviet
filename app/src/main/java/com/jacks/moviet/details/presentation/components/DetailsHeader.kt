package com.jacks.moviet.details.presentation.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.jacks.moviet.core.designsystem.components.MoviePoster
import com.jacks.moviet.core.designsystem.theme.Spacing
import com.jacks.moviet.core.domain.model.Movie
import com.jacks.moviet.core.presentation.util.displayName

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun DetailsHeader(
    modifier: Modifier = Modifier,
    movie: Movie,
    contentRating: String?,
    compactHeader: Boolean,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
) {
    if (compactHeader) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MoviePoster(
                modifier = Modifier.width(DetailsPanePosterWidth).aspectRatio(PosterAspectRatio),
                movie = movie,
            )
            MovieIdentity(modifier = Modifier.weight(1f), movie = movie, contentRating = contentRating)
        }
    } else Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            MoviePoster(
                modifier = Modifier.width(DetailsPosterWidth).aspectRatio(PosterAspectRatio),
                movie = movie,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
        MovieIdentity(movie = movie, contentRating = contentRating)
    }
}

@Composable
private fun MovieIdentity(modifier: Modifier = Modifier, movie: Movie, contentRating: String?) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        Text(
            modifier = Modifier.testTag("details_movie_title"),
            text = movie.title,
            style = MaterialTheme.typography.headlineMedium,
        )
        val metadata = listOfNotNull(movie.year, movie.type.displayName(), contentRating)
        if (metadata.isNotEmpty()) {
            Text(
                text = metadata.joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val DetailsPosterWidth = 180.dp
private val DetailsPanePosterWidth = 104.dp
private const val PosterAspectRatio = 2f / 3f
