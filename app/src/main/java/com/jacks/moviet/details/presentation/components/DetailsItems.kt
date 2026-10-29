package com.jacks.moviet.details.presentation.components

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jacks.moviet.R
import com.jacks.moviet.core.designsystem.components.ScreenMessage
import com.jacks.moviet.core.domain.model.MovieType
import com.jacks.moviet.details.presentation.DetailsAction
import com.jacks.moviet.details.presentation.DetailsState
import com.jacks.moviet.details.presentation.FavoriteState

internal fun LazyListScope.loadedDetailsItems(
    modifier: Modifier,
    state: DetailsState.Loaded,
    onAction: (DetailsAction) -> Unit,
) {
    val details = state.details
    if (details.imdbRating != null || details.contentRating != null) {
        item(key = "ratings") {
            DetailsRatings(
                modifier = modifier,
                imdbRating = details.imdbRating,
                contentRating = details.contentRating,
            )
        }
    }
    item(key = "favorite") {
        FavoriteMovieButton(
            modifier = modifier,
            state = state.favorite,
            onClick = { onAction(DetailsAction.ToggleFavorite) },
        )
    }
    if (state.favorite is FavoriteState.Error) {
        item(key = "favorite_read_error") {
            ScreenMessage(
                title = stringResource(R.string.favorites_load_error),
                message = stringResource(R.string.favorites_load_error_message),
                onRetry = { onAction(DetailsAction.RetryFavorite) },
            )
        }
    }
    details.genres?.let { genres ->
        item(key = "genres") {
            DetailSection(modifier = modifier, title = stringResource(R.string.movie_genres), text = genres)
        }
    }
    details.plot?.let { plot ->
        item(key = "plot") {
            MoviePlot(modifier = modifier, imdbId = details.movie.imdbId, plot = plot)
        }
    }
    details.runtime?.let { runtime ->
        item(key = "runtime") {
            DetailSection(
                modifier = modifier,
                title = stringResource(if (details.movie.type == MovieType.SERIES) R.string.movie_series_runtime else R.string.movie_runtime),
                text = runtime,
            )
        }
    }
    details.director?.let { director ->
        item(key = "director") {
            DetailSection(modifier = modifier, title = stringResource(R.string.movie_director), text = director)
        }
    }
    details.actors?.let { actors ->
        item(key = "actors") {
            DetailSection(modifier = modifier, title = stringResource(R.string.movie_actors), text = actors)
        }
    }
    details.seasons?.let { seasons ->
        item(key = "seasons") {
            DetailSection(modifier = modifier, title = stringResource(R.string.movie_seasons), text = seasons)
        }
    }
}
