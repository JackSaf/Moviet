package com.jacks.moviet.favorites.domain

import com.jacks.moviet.core.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    /** Cold local reads; storage failures terminate collection and can be retried by collecting again. */
    fun observeFavorites(): Flow<List<Movie>>

    /** Emits the persisted status for one movie, including subsequent changes from other screens. */
    fun observeIsFavorite(imdbId: String): Flow<Boolean>

    /** Idempotent upsert by IMDb ID. Storage failures propagate to the caller; cancellation is preserved. */
    suspend fun addFavorite(movie: Movie)

    /** Removing an absent ID is a no-op. Storage failures propagate to the caller. */
    suspend fun removeFavorite(imdbId: String)
}
