package com.jacks.moviet.search.domain

import com.jacks.moviet.core.domain.model.Movie

/** Keeps the API total attached to the movies presented by the same paging generation. */
data class MovieSearchResult(
    val movie: Movie,
    val totalResults: Int,
)
