package com.jacks.moviet.favorites.presentation

import com.jacks.moviet.core.domain.model.Movie

data class FavoritesState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = true,
    val hasLoadError: Boolean = false,
    val removingIds: Set<String> = emptySet(),
    val removalErrors: Set<String> = emptySet(),
)

sealed interface FavoritesAction {
    data object SearchClick : FavoritesAction
    data object Retry : FavoritesAction
    data class MovieClick(val movie: Movie) : FavoritesAction
    data class RemoveMovie(val imdbId: String) : FavoritesAction
}

sealed interface FavoritesEvent {
    data object OpenSearch : FavoritesEvent
    data class OpenMovie(val movie: Movie) : FavoritesEvent
}
