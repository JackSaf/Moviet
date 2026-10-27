package com.jacks.moviet.search.domain

import androidx.paging.PagingData
import com.jacks.moviet.core.domain.NetworkResult
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    /** Observes movies with their API total. Blank queries emit an empty list without fetching. */
    fun observeSearchMovies(query: String): Flow<PagingData<MovieSearchResult>>

    /** Fetches one OMDb page (1..100). Blank queries and invalid pages fail locally. */
    suspend fun searchMovies(query: String, page: Int = 1): NetworkResult<MovieSearchPage>
}
