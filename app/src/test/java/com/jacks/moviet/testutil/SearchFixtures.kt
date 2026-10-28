package com.jacks.moviet.testutil

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.jacks.moviet.core.domain.NetworkResult
import com.jacks.moviet.core.domain.model.Movie
import com.jacks.moviet.core.domain.model.MovieType
import com.jacks.moviet.search.data.SearchPagingSource
import com.jacks.moviet.search.domain.MovieSearchPage
import com.jacks.moviet.search.domain.MovieSearchResult
import com.jacks.moviet.search.domain.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal fun searchRepository(
    search: suspend (String, Int) -> NetworkResult<MovieSearchPage>,
): SearchRepository = object : SearchRepository {
    override fun observeSearchMovies(query: String): Flow<PagingData<MovieSearchResult>> =
        if (query.isBlank()) {
            flowOf(PagingData.empty())
        } else {
            Pager(
                config = PagingConfig(pageSize = 10, initialLoadSize = 10, enablePlaceholders = false),
                pagingSourceFactory = { SearchPagingSource(this, query) },
            ).flow
        }

    override suspend fun searchMovies(query: String, page: Int) = search(query, page)
}

internal fun searchPage(page: Int, total: Int = 10, count: Int = 10) =
    NetworkResult.Success(MovieSearchPage(searchMovies(count, startIndex = (page - 1) * 10), total, page))

internal fun searchMovies(count: Int, startIndex: Int = 0) = List(count) { offset ->
    val index = startIndex + offset
    Movie("tt$index", "Movie $index", null, MovieType.MOVIE)
}
