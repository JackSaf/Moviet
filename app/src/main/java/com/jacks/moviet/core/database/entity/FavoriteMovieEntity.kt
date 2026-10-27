package com.jacks.moviet.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_movies")
data class FavoriteMovieEntity(
    @PrimaryKey val imdbId: String,
    val title: String,
    val posterUrl: String?,
    val type: String,
    val year: String? = null,
)
