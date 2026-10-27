package com.jacks.moviet.search.presentation

import androidx.paging.LoadState
import com.jacks.moviet.core.domain.NetworkError
import com.jacks.moviet.core.presentation.util.isMovieNotFound
import com.jacks.moviet.search.domain.SearchPagingException

internal sealed interface SearchContent {
    data object Start : SearchContent
    data object Loading : SearchContent
    data object Empty : SearchContent
    data class Error(val error: NetworkError?) : SearchContent
    data class Results(val isLoading: Boolean) : SearchContent
}

internal fun SearchState.contentFor(itemCount: Int, refresh: LoadState): SearchContent {
    if (query.isBlank()) return SearchContent.Start

    val queryPending = query.trim() != activeQuery
    // Paging retains the previous query's snapshot when a new refresh fails.
    // Its item count must not turn an empty/error outcome into current results.
    if (refresh is LoadState.Error) {
        if (queryPending) return SearchContent.Loading
        val error = (refresh.error as? SearchPagingException)?.error
        return if (error?.isMovieNotFound() == true) SearchContent.Empty else SearchContent.Error(error)
    }

    val isLoading = refresh is LoadState.Loading || queryPending
    return when {
        itemCount > 0 -> SearchContent.Results(isLoading)
        isLoading -> SearchContent.Loading
        else -> SearchContent.Empty
    }
}
