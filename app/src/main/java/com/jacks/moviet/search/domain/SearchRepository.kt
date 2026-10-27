package com.jacks.moviet.search.domain

import com.jacks.moviet.core.domain.NetworkResult

interface SearchRepository {
    /** Fetches one OMDb page (1..100). Blank queries and invalid pages fail locally. */
    suspend fun searchMovies(query: String, page: Int = 1): NetworkResult<MovieSearchPage>
}
