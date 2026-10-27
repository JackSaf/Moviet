package com.jacks.moviet.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jacks.moviet.core.database.entity.FavoriteMovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteMovieDao {
    @Query("SELECT * FROM favorite_movies ORDER BY title COLLATE NOCASE, imdbId")
    fun observeFavorites(): Flow<List<FavoriteMovieEntity>>

    @Upsert
    suspend fun upsert(movie: FavoriteMovieEntity)

    @Query("DELETE FROM favorite_movies WHERE imdbId = :imdbId")
    suspend fun delete(imdbId: String)
}
