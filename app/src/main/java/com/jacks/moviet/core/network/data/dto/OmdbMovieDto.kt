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
    @SerialName("Response") val response: String? = null,
    @SerialName("Error") val error: String? = null,
)
