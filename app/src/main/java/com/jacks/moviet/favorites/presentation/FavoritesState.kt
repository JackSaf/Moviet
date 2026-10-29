package com.jacks.moviet.favorites.presentation

import com.jacks.moviet.core.domain.model.Movie

data class FavoritesState(
    val content: FavoritesContent = FavoritesContent.Loading,
    val movieWrites: Map<String, FavoriteWriteStatus> = emptyMap(),
    val undoRemoval: UndoRemoval? = null,
)

sealed interface FavoritesContent {
    data object Loading : FavoritesContent
    data object Error : FavoritesContent
    data class Loaded(val movies: List<Movie>) : FavoritesContent
}

enum class FavoriteWriteStatus { PENDING, FAILED }

data class UndoRemoval(
    val id: Long,
    val movie: Movie,
    val status: UndoStatus = UndoStatus.AVAILABLE,
)

enum class UndoStatus { AVAILABLE, RESTORING, FAILED }

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
