package com.jacks.moviet.favorites.presentation

import com.jacks.moviet.core.domain.model.Movie

data class FavoritesState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = true,
    val hasLoadError: Boolean = false,
    val removingIds: Set<String> = emptySet(),
    val removalErrors: Set<String> = emptySet(),
    val undoRemoval: UndoRemoval? = null,
)

data class UndoRemoval(
    val id: Long,
    val movie: Movie,
    val isRestoring: Boolean = false,
    val hasError: Boolean = false,
)

sealed interface FavoritesAction {
    data object SearchClick : FavoritesAction
    data object Retry : FavoritesAction
    data class MovieClick(val movie: Movie) : FavoritesAction
    data class RemoveMovie(val imdbId: String) : FavoritesAction
    data class UndoRemove(val id: Long) : FavoritesAction
    data class DismissUndo(val id: Long) : FavoritesAction
}

sealed interface FavoritesEvent {
    data object OpenSearch : FavoritesEvent
    data class OpenMovie(val movie: Movie) : FavoritesEvent
}
