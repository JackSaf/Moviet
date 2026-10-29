package com.jacks.moviet.details.presentation

import com.jacks.moviet.core.domain.NetworkError
import com.jacks.moviet.details.domain.MovieDetails

sealed interface DetailsState {
    data object Loading : DetailsState
    data class Error(val error: NetworkError) : DetailsState
    data class Loaded(
        val details: MovieDetails,
        val favorite: FavoriteState = FavoriteState.Loading,
    ) : DetailsState
}

sealed interface FavoriteState {
    data object Loading : FavoriteState
    data object Error : FavoriteState
    data class Ready(
        val isFavorite: Boolean,
        val update: FavoriteUpdate = FavoriteUpdate.IDLE,
    ) : FavoriteState
}

enum class FavoriteUpdate { IDLE, SAVING, FAILED }

sealed interface DetailsAction {
    data class LoadMovie(val imdbId: String) : DetailsAction
    data object Retry : DetailsAction
    data object ToggleFavorite : DetailsAction
    data object RetryFavorite : DetailsAction
}
