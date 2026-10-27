package com.jacks.moviet.core.domain.model

data class Movie(
    val imdbId: String,
    val title: String,
    val posterUrl: String?,
    val type: MovieType,
)
