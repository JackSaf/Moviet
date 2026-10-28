package com.jacks.moviet.details.presentation

import com.jacks.moviet.core.domain.NetworkError
import com.jacks.moviet.details.domain.MovieDetails

data class DetailsState(
    val isLoading: Boolean = true,
    val details: MovieDetails? = null,
    val error: NetworkError? = null,
    val isFavorite: Boolean = false,
    val isFavoriteLoaded: Boolean = false,
    val favoriteLoadError: Boolean = false,
    val isSavingFavorite: Boolean = false,
    val favoriteError: Boolean = false,
)

sealed interface DetailsAction {
    data class LoadMovie(val imdbId: String) : DetailsAction
    data object Retry : DetailsAction
    data object ToggleFavorite : DetailsAction
    data object RetryFavorite : DetailsAction
}
