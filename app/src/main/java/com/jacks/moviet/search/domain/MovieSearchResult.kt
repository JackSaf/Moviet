package com.jacks.moviet.search.domain

import com.jacks.moviet.core.domain.model.Movie

/** Keeps query identity and the API total attached to the presented paging snapshot. */
data class MovieSearchResult(
    val movie: Movie,
    val totalResults: Int,
    val query: String,
)
