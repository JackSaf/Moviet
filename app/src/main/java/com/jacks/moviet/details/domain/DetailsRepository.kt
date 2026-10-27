package com.jacks.moviet.details.domain

import com.jacks.moviet.core.domain.NetworkResult
import com.jacks.moviet.core.domain.model.Movie

interface DetailsRepository {
    suspend fun getMovie(imdbId: String): NetworkResult<Movie>
}
