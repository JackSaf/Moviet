package com.jacks.moviet.details.domain

import com.jacks.moviet.core.domain.model.Movie

data class MovieDetails(
    val movie: Movie,
    val plot: String? = null,
    val genres: String? = null,
    val runtime: String? = null,
    val contentRating: String? = null,
    val imdbRating: String? = null,
    val director: String? = null,
    val actors: String? = null,
    val seasons: String? = null,
)
