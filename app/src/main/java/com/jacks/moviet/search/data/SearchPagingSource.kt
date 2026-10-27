package com.jacks.moviet.search.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.jacks.moviet.core.domain.NetworkResult
import com.jacks.moviet.core.network.OmdbConfig
import com.jacks.moviet.search.domain.MovieSearchResult
import com.jacks.moviet.search.domain.SearchPagingException
import com.jacks.moviet.search.domain.SearchRepository

/** Loads the repository's paged search one OMDb page at a time. */
internal class SearchPagingSource(
    private val repository: SearchRepository,
    private val query: String,
) : PagingSource<Int, MovieSearchResult>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MovieSearchResult> {
        val page = params.key ?: OmdbConfig.FIRST_PAGE
        return when (val result = repository.searchMovies(query, page)) {
            is NetworkResult.Failure -> LoadResult.Error(SearchPagingException(result.error))
            is NetworkResult.Success -> {
                val movies = result.data.movies
                val endReached = movies.isEmpty() ||
                    page * PAGE_SIZE >= result.data.totalResults || page >= OmdbConfig.LAST_PAGE
                LoadResult.Page(
                    data = movies.map { MovieSearchResult(it, result.data.totalResults) },
                    prevKey = (page - 1).takeIf { page > OmdbConfig.FIRST_PAGE },
                    nextKey = (page + 1).takeUnless { endReached },
                )
            }
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MovieSearchResult>): Int? =
        state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.let { page ->
                page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
            }
        }

    companion object {
        // OMDb uses fixed ten-item pages, regardless of Paging's requested loadSize.
        const val PAGE_SIZE = 10
    }
}
