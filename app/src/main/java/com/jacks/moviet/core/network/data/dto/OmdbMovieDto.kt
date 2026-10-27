package com.jacks.moviet.core.network.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Search items and the by-ID response share these movie fields. */
@Serializable
data class OmdbMovieDto(
    @SerialName("imdbID") val imdbId: String? = null,
    @SerialName("Title") val title: String? = null,
    @SerialName("Poster") val poster: String? = null,
    @SerialName("Type") val type: String? = null,
    @SerialName("Year") val year: String? = null,
    @SerialName("Plot") val plot: String? = null,
    @SerialName("Genre") val genre: String? = null,
    @SerialName("Runtime") val runtime: String? = null,
    @SerialName("Rated") val rated: String? = null,
    @SerialName("imdbRating") val imdbRating: String? = null,
    @SerialName("Director") val director: String? = null,
    @SerialName("Actors") val actors: String? = null,
    @SerialName("totalSeasons") val totalSeasons: String? = null,
    @SerialName("Response") val response: String? = null,
    @SerialName("Error") val error: String? = null,
)
