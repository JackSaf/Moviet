package com.jacks.moviet.search.data

import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import com.jacks.moviet.search.domain.MovieSearchResult
import com.jacks.moviet.core.network.data.OmdbApi
import com.jacks.moviet.core.network.data.dto.OmdbMovieDto
import com.jacks.moviet.core.network.data.dto.OmdbSearchResponseDto
import com.jacks.moviet.testutil.omdbApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class OmdbSearchPagingTest {
    @Test
    fun `paged search is lazy and clearing a query does not fetch`() = runTest {
        val requests = mutableListOf<Pair<String, Int>>()
        val api = object : OmdbApi by omdbApi() {
            override suspend fun searchMovies(query: String, page: Int): Response<OmdbSearchResponseDto> {
                requests += query to page
                return Response.success(
                    OmdbSearchResponseDto(
                        movies = listOf(OmdbMovieDto(imdbId = "tt1234567", title = "Batman")),
                        totalResults = "1",
                        response = "True",
                    ),
                )
            }
        }
        val repository = OmdbSearchRepository(api)
        val results = repository.observeSearchMovies("  batman  ")
        runCurrent()
        assertTrue(requests.isEmpty())

        val presenter = object : PagingDataPresenter<MovieSearchResult>(StandardTestDispatcher(testScheduler)) {
            override suspend fun presentPagingDataEvent(event: PagingDataEvent<MovieSearchResult>) = Unit
        }
        val collection = backgroundScope.launch { results.collectLatest(presenter::collectFrom) }
        runCurrent()
        assertEquals(listOf("batman" to 1), requests)
        assertEquals(listOf("tt1234567"), presenter.snapshot().items.map { it.movie.imdbId })
        assertEquals(1, presenter.snapshot().items.first().totalResults)

        collection.cancelAndJoin()
        backgroundScope.launch {
            repository.observeSearchMovies("  ").collectLatest(presenter::collectFrom)
        }
        runCurrent()
        assertTrue(presenter.snapshot().items.isEmpty())
        assertEquals(1, requests.size)
    }
}
