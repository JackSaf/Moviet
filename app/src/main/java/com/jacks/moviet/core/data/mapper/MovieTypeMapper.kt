package com.jacks.moviet.core.data.mapper

import com.jacks.moviet.core.domain.model.MovieType

internal fun String?.toMovieType(): MovieType = when (this?.trim()?.lowercase()) {
    "movie" -> MovieType.MOVIE
    "series" -> MovieType.SERIES
    "episode" -> MovieType.EPISODE
    else -> MovieType.UNKNOWN
}

internal fun MovieType.toDataValue(): String = when (this) {
    MovieType.MOVIE -> "movie"
    MovieType.SERIES -> "series"
    MovieType.EPISODE -> "episode"
    MovieType.UNKNOWN -> "unknown"
}
