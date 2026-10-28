package com.jacks.moviet.search.data

import androidx.paging.PagingConfig
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingState
import com.jacks.moviet.core.domain.NetworkError
import com.jacks.moviet.core.domain.NetworkResult
import com.jacks.moviet.search.domain.MovieSearchResult
import com.jacks.moviet.search.domain.SearchPagingException
import com.jacks.moviet.testutil.searchMovies
import com.jacks.moviet.testutil.searchPage
import com.jacks.moviet.testutil.searchRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchPagingSourceTest {
    @Test
    fun `initial load fetches one API page even when Paging requests a larger load`() = runTest {
        val requests = mutableListOf<Pair<String, Int>>()
        val source = SearchPagingSource(searchRepository { query, page ->
            requests += query to page
            searchPage(page = page, total = 25)
        }, "batman")

        val result = source.load(LoadParams.Refresh(null, 30, false)) as LoadResult.Page

        assertEquals(listOf("batman" to 1), requests)
        assertEquals(10, result.data.size)
        assertEquals(searchMovies(10), result.data.map { it.movie })
        assertTrue(result.data.all { it.totalResults == 25 })
        assertNull(result.prevKey)
        assertEquals(2, result.nextKey)
    }

    @Test
    fun `last partial page stops at the total result count`() = runTest {
        val source = SearchPagingSource(searchRepository { _, page ->
            searchPage(page = page, total = 25, count = 5)
        }, "batman")

        val result = source.load(LoadParams.Append(3, 10, false)) as LoadResult.Page

        assertEquals(2, result.prevKey)
        assertNull(result.nextKey)
    }

    @Test
    fun `full last page and API page limit both end pagination`() = runTest {
        for ((page, total) in listOf(2 to 20, 100 to 1500)) {
            val source = SearchPagingSource(searchRepository { _, requestedPage ->
                searchPage(page = requestedPage, total = total)
            }, "batman")

            val result = source.load(LoadParams.Append(page, 10, false)) as LoadResult.Page

            assertNull(result.nextKey)
        }
    }

    @Test
    fun `empty successful page ends pagination even if total is stale`() = runTest {
        val source = SearchPagingSource(searchRepository { _, page ->
            searchPage(page = page, total = 25, count = 0)
        }, "batman")

        val result = source.load(LoadParams.Refresh(null, 10, false)) as LoadResult.Page

        assertTrue(result.data.isEmpty())
        assertNull(result.nextKey)
    }

    @Test
    fun `typed failures survive in Paging load state`() = runTest {
        for (error in listOf(
            NetworkError.Connection,
            NetworkError.Http(401),
            NetworkError.Omdb("Too many results."),
            NetworkError.InvalidResponse,
            NetworkError.InvalidRequest,
        )) {
            val source = SearchPagingSource(searchRepository { _, _ ->
                NetworkResult.Failure(error)
            }, "batman")

            val result = source.load(LoadParams.Refresh(null, 10, false)) as LoadResult.Error

            assertSame(error, (result.throwable as SearchPagingException).error)
        }
    }

    @Test
    fun `cancellation escapes instead of becoming a load error`() = runTest {
        val cancellation = CancellationException("Query replaced")
        val source = SearchPagingSource(searchRepository { _, _ -> throw cancellation }, "batman")

        val failure = runCatching { source.load(LoadParams.Refresh(null, 10, false)) }.exceptionOrNull()

        assertSame(cancellation, failure)
    }

    @Test
    fun `refresh key preserves the anchored page including the last page`() {
        val source = SearchPagingSource(searchRepository { _, page -> searchPage(page) }, "batman")
        val config = PagingConfig(pageSize = 10, enablePlaceholders = false)
        val pages = listOf(
            LoadResult.Page(searchMovies(10).map { MovieSearchResult(it, 20) }, prevKey = null, nextKey = 2),
            LoadResult.Page(searchMovies(10, startIndex = 10).map { MovieSearchResult(it, 20) }, prevKey = 1, nextKey = null),
        )

        assertEquals(1, source.getRefreshKey(PagingState(pages, 5, config, 0)))
        assertEquals(2, source.getRefreshKey(PagingState(pages, 15, config, 0)))
        assertNull(source.getRefreshKey(PagingState(pages, null, config, 0)))
    }
}
