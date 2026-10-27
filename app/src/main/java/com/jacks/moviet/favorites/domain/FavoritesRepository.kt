package com.jacks.moviet.favorites.domain

import com.jacks.moviet.core.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun observeFavorites(): Flow<List<Movie>>
    fun observeIsFavorite(imdbId: String): Flow<Boolean>
    suspend fun addFavorite(movie: Movie)
    suspend fun removeFavorite(imdbId: String)
}
