package com.jacks.moviet.core.presentation.util

import com.jacks.moviet.R
import com.jacks.moviet.core.domain.NetworkError

fun NetworkError.asUiText(): UiText = when (this) {
    NetworkError.Connection -> UiText.StringResource(R.string.error_connection)
    NetworkError.InvalidRequest -> UiText.StringResource(R.string.error_invalid_request)
    NetworkError.InvalidResponse -> UiText.StringResource(R.string.error_invalid_response)
    is NetworkError.Http -> UiText.StringResource(R.string.error_server)
    is NetworkError.Omdb -> when {
        isMovieNotFound() -> UiText.StringResource(R.string.search_no_results)
        message.equals("Too many results.", ignoreCase = true) -> UiText.StringResource(R.string.search_too_broad)
        else -> UiText.StringResource(R.string.error_service)
    }
}

fun NetworkError.isMovieNotFound(): Boolean =
    this is NetworkError.Omdb && message?.trim().equals("Movie not found!", ignoreCase = true)
