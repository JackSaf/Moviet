package com.jacks.moviet.favorites.data

import com.jacks.moviet.core.data.mapper.toDataValue
import com.jacks.moviet.core.data.mapper.toMovieType
import com.jacks.moviet.core.database.entity.FavoriteMovieEntity
import com.jacks.moviet.core.domain.model.Movie

internal fun FavoriteMovieEntity.toDomain(): Movie = Movie(
    imdbId = imdbId,
    title = title,
    posterUrl = posterUrl,
    type = type.toMovieType(),
)

internal fun Movie.toEntity(): FavoriteMovieEntity = FavoriteMovieEntity(
    imdbId = imdbId,
    title = title,
    posterUrl = posterUrl,
    type = type.toDataValue(),
)
