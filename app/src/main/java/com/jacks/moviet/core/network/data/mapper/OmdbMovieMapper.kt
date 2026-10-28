package com.jacks.moviet.core.network.data.mapper

import com.jacks.moviet.core.data.mapper.toMovieType
import com.jacks.moviet.core.domain.model.Movie
import com.jacks.moviet.core.network.data.dto.OmdbMovieDto
import kotlinx.serialization.SerializationException
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

internal fun String?.normalizedOmdbValue(): String? = this?.trim()
    ?.takeUnless { it.isEmpty() || it.equals("N/A", ignoreCase = true) }

internal fun OmdbMovieDto.toDomain(): Movie = Movie(
    imdbId = imdbId.normalizedOmdbValue()
        ?: throw SerializationException("Missing movie identity"),
    title = title.normalizedOmdbValue()
        ?: throw SerializationException("Missing movie title"),
    // Remote metadata must not select Coil's file/content/resource URI loaders.
    posterUrl = poster.normalizedOmdbValue()?.toHttpUrlOrNull()?.takeIf { it.isHttps }?.toString(),
    type = type.toMovieType(),
    year = year.normalizedOmdbValue(),
)
