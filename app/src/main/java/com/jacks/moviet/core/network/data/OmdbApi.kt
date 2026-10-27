package com.jacks.moviet.core.network.data

import com.jacks.moviet.core.network.OmdbConfig
import com.jacks.moviet.core.network.data.dto.OmdbMovieDto
import com.jacks.moviet.core.network.data.dto.OmdbSearchResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OmdbApi {
    // OMDb pages are one-based and limited to 1..100. Authentication is in the client.
    @GET(".")
    suspend fun searchMovies(
        @Query("s") query: String,
        @Query("page") page: Int = OmdbConfig.FIRST_PAGE,
    ): Response<OmdbSearchResponseDto>

    @GET(".")
    suspend fun getMovie(@Query("i") imdbId: String): Response<OmdbMovieDto>
}
