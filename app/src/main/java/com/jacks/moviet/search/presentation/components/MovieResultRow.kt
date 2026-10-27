package com.jacks.moviet.search.presentation.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.jacks.moviet.core.designsystem.components.MoviePoster
import com.jacks.moviet.core.designsystem.theme.Spacing
import com.jacks.moviet.core.domain.model.Movie
import com.jacks.moviet.core.presentation.util.displayName

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun MovieResultRow(
    movie: Movie,
    selected: Boolean,
    compact: Boolean,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    onClick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("movie_${movie.imdbId}").semantics { this.selected = selected },
        onClick = { focusManager.clearFocus(); onClick() },
        shape = MaterialTheme.shapes.medium,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(vertical = Spacing.compact, horizontal = Spacing.extraSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            MoviePoster(
                modifier = Modifier.width(if (compact) CompactResultPosterWidth else ResultPosterWidth).aspectRatio(PosterAspectRatio),
                movie = movie,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                Text(text = movie.title, style = MaterialTheme.typography.titleMedium)
                val metadata = listOfNotNull(movie.year, movie.type.displayName()).joinToString(" · ")
                if (metadata.isNotEmpty()) {
                    Text(text = metadata, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private val ResultPosterWidth = 72.dp
private val CompactResultPosterWidth = 56.dp
private const val PosterAspectRatio = 2f / 3f
