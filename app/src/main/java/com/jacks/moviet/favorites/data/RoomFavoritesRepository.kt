package com.jacks.moviet.favorites.data

import com.jacks.moviet.core.database.dao.FavoriteMovieDao
import com.jacks.moviet.core.domain.model.Movie
import com.jacks.moviet.favorites.domain.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class RoomFavoritesRepository(private val dao: FavoriteMovieDao) : FavoritesRepository {
    override fun observeFavorites(): Flow<List<Movie>> = dao.observeFavorites()
        .map { entities -> entities.map { it.toDomain() } }
        .distinctUntilChanged()

    override fun observeIsFavorite(imdbId: String): Flow<Boolean> =
        dao.observeIsFavorite(imdbId).distinctUntilChanged()

    override suspend fun addFavorite(movie: Movie) {
        dao.upsert(movie.toEntity())
    }

    override suspend fun removeFavorite(imdbId: String) {
        dao.delete(imdbId)
    }
}
