package com.jacks.moviet.search.domain

import com.jacks.moviet.core.domain.model.Movie

data class MovieSearchPage(
    val movies: List<Movie>,
    val totalResults: Int,
    val page: Int,
)
