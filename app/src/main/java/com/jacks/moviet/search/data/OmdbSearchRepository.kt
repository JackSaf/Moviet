package com.jacks.moviet.search.data

import com.jacks.moviet.core.domain.NetworkError
import com.jacks.moviet.core.domain.NetworkResult
import com.jacks.moviet.core.network.OmdbConfig
import com.jacks.moviet.core.network.data.OmdbApi
import com.jacks.moviet.core.network.data.mapper.normalizedOmdbValue
import com.jacks.moviet.core.network.data.mapper.toDomain
import com.jacks.moviet.core.network.data.omdbRequest
import com.jacks.moviet.search.domain.MovieSearchPage
import com.jacks.moviet.search.domain.SearchRepository

class OmdbSearchRepository(private val api: OmdbApi) : SearchRepository {
    override suspend fun searchMovies(query: String, page: Int): NetworkResult<MovieSearchPage> {
        val keyword = query.trim()
        if (keyword.isEmpty() || page !in OmdbConfig.FIRST_PAGE..OmdbConfig.LAST_PAGE) {
            return NetworkResult.Failure(NetworkError.InvalidRequest)
        }
        return omdbRequest(request = { api.searchMovies(keyword, page) }) { response ->
            when (response.response) {
                OmdbConfig.RESPONSE_FALSE -> NetworkResult.Failure(
                    NetworkError.Omdb(response.error.normalizedOmdbValue()),
                )
                OmdbConfig.RESPONSE_TRUE -> {
                    val movies = response.movies
                    val total = response.totalResults?.toIntOrNull()
                    if (movies == null || total == null || total < movies.size) {
                        NetworkResult.Failure(NetworkError.InvalidResponse)
                    } else {
                        NetworkResult.Success(MovieSearchPage(movies.map { it.toDomain() }, total, page))
                    }
                }
                else -> NetworkResult.Failure(NetworkError.InvalidResponse)
            }
        }
    }
}
