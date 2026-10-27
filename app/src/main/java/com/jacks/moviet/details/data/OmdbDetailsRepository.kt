package com.jacks.moviet.details.data

import com.jacks.moviet.core.domain.NetworkError
import com.jacks.moviet.core.domain.NetworkResult
import com.jacks.moviet.core.domain.model.Movie
import com.jacks.moviet.core.network.OmdbConfig
import com.jacks.moviet.core.network.data.OmdbApi
import com.jacks.moviet.core.network.data.mapper.normalizedOmdbValue
import com.jacks.moviet.core.network.data.mapper.toDomain
import com.jacks.moviet.core.network.data.omdbRequest
import com.jacks.moviet.details.domain.DetailsRepository

class OmdbDetailsRepository(private val api: OmdbApi) : DetailsRepository {
    override suspend fun getMovie(imdbId: String): NetworkResult<Movie> {
        val id = imdbId.trim()
        if (id.isEmpty()) return NetworkResult.Failure(NetworkError.InvalidRequest)

        return omdbRequest(request = { api.getMovie(id) }) { response ->
            when (response.response) {
                OmdbConfig.RESPONSE_TRUE -> NetworkResult.Success(response.toDomain())
                OmdbConfig.RESPONSE_FALSE -> NetworkResult.Failure(
                    NetworkError.Omdb(response.error.normalizedOmdbValue()),
                )
                else -> NetworkResult.Failure(NetworkError.InvalidResponse)
            }
        }
    }
}
