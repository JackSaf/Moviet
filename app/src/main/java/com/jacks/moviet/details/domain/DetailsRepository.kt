package com.jacks.moviet.details.domain

import com.jacks.moviet.core.domain.NetworkResult

interface DetailsRepository {
    suspend fun getMovie(imdbId: String): NetworkResult<MovieDetails>
}
