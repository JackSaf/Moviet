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

internal fun SearchState.contentFor(snapshotQuery: String?, refresh: LoadState): SearchContent {
    if (query.isBlank()) return SearchContent.Start

    val queryPending = query.trim() != activeQuery
    // Do not pair a newly edited query with the previous query's retained Paging snapshot.
    if (queryPending) return SearchContent.Loading

    // Paging retains the previous query's snapshot when a new refresh fails.
    // Its item count must not turn an empty/error outcome into current results.
    if (refresh is LoadState.Error) {
        val error = (refresh.error as? SearchPagingException)?.error
        return if (error?.isMovieNotFound() == true) SearchContent.Empty else SearchContent.Error(error)
    }

    // activeQuery changes before Paging replaces its snapshot (and even before Loading arrives).
    if (snapshotQuery != null && snapshotQuery != activeQuery) return SearchContent.Loading

    val isLoading = refresh is LoadState.Loading
    return when {
        snapshotQuery != null -> SearchContent.Results(isLoading)
        isLoading -> SearchContent.Loading
        else -> SearchContent.Empty
    }
}
