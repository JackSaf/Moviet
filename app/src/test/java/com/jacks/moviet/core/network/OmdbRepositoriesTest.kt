package com.jacks.moviet.core.network

import com.jacks.moviet.core.domain.NetworkError
import com.jacks.moviet.core.domain.NetworkResult
import com.jacks.moviet.core.domain.model.Movie
import com.jacks.moviet.core.domain.model.MovieType
import com.jacks.moviet.core.network.data.OmdbApi
import com.jacks.moviet.core.network.data.dto.OmdbMovieDto
import com.jacks.moviet.core.network.data.dto.OmdbSearchResponseDto
import com.jacks.moviet.details.data.OmdbDetailsRepository
import com.jacks.moviet.search.data.OmdbSearchRepository
import com.jacks.moviet.search.domain.MovieSearchPage
import com.jacks.moviet.testutil.omdbApi
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.Response

class OmdbRepositoriesTest {
    @Test
    fun `search encodes keyword and page and normalizes unavailable values`() = runBlocking {
        val api = omdbApi(
            body = """{"Response":"True","totalResults":"21","Search":[
                {"imdbID":"tt1234567","Title":"A & B","Poster":" N/A ","Type":"N/A","Year":"2026"}
            ]}""",
            onRequest = { request ->
                assertEquals("/", request.url.encodedPath)
                assertEquals("A & B", request.url.queryParameter("s"))
                assertEquals("2", request.url.queryParameter("page"))
            },
        )
        assertEquals(
            NetworkResult.Success(
                MovieSearchPage(
                    movies = listOf(Movie("tt1234567", "A & B", null, MovieType.UNKNOWN)),
                    totalResults = 21,
                    page = 2,
                ),
            ),
            OmdbSearchRepository(api).searchMovies("  A & B  ", 2),
        )
    }

    @Test
    fun `search defaults to the first page`() = runBlocking {
        val repository = OmdbSearchRepository(omdbApi(
            body = """{"Response":"True","totalResults":"0","Search":[]}""",
            onRequest = { assertEquals("1", it.url.queryParameter("page")) },
        ))
        assertEquals(
            NetworkResult.Success(MovieSearchPage(emptyList(), 0, 1)),
            repository.searchMovies("example"),
        )
    }

    @Test
    fun `blank keywords and out of range pages do not request the network`() = runBlocking {
        val repository = OmdbSearchRepository(omdbApi(onRequest = { fail("Unexpected request") }))
        listOf(" " to 1, "movie" to 0, "movie" to 101).forEach { (query, page) ->
            assertEquals(
                NetworkResult.Failure(NetworkError.InvalidRequest),
                repository.searchMovies(query, page),
            )
        }
    }

    @Test
    fun `HTTP 200 OMDb failure is distinct from HTTP failure`() = runBlocking {
        val response = """{"Response":"False","Error":"Movie not found!"}"""
        assertEquals(
            NetworkResult.Failure(NetworkError.Omdb("Movie not found!")),
            OmdbSearchRepository(omdbApi(response)).searchMovies("missing"),
        )
        assertEquals(
            NetworkResult.Failure(NetworkError.Http(429)),
            OmdbSearchRepository(omdbApi(response, statusCode = 429)).searchMovies("movie"),
        )
    }

    @Test
    fun `invalid successful search envelopes fail instead of returning incomplete content`() = runBlocking {
        listOf(
            """{}""",
            """{"Response":"True","totalResults":"1"}""",
            """{"Response":"True","totalResults":"N/A","Search":[]}""",
            """{"Response":"True","totalResults":"-1","Search":[]}""",
            """{"Response":"True","totalResults":"1","Search":[{"Title":"Missing ID"}]}""",
            """{"Response":"True","totalResults":"1","Search":[{"imdbID":"tt1234567"}]}""",
            """{"Response":"True","totalResults":"0","Search":[{"imdbID":"tt1234567","Title":"Movie"}]}""",
        ).forEach { body ->
            assertEquals(
                body,
                NetworkResult.Failure(NetworkError.InvalidResponse),
                OmdbSearchRepository(omdbApi(body)).searchMovies("movie"),
            )
        }
    }

    @Test
    fun `malformed JSON and empty HTTP success are invalid responses`() = runBlocking {
        assertEquals(
            NetworkResult.Failure(NetworkError.InvalidResponse),
            OmdbSearchRepository(omdbApi("{broken")).searchMovies("movie"),
        )
        assertEquals(
            NetworkResult.Failure(NetworkError.InvalidResponse),
            OmdbSearchRepository(omdbApi("", statusCode = 204)).searchMovies("movie"),
        )
    }

    @Test
    fun `details requests the IMDb ID and maps a movie`() = runBlocking {
        val api = omdbApi(
            body = """{"Response":"True","imdbID":"tt1234567","Title":"Example",
                "Poster":"https://example.com/poster.jpg","Type":"series"}""",
            onRequest = { request ->
                assertEquals("tt1234567", request.url.queryParameter("i"))
                assertEquals(null, request.url.queryParameter("s"))
            },
        )
        assertEquals(
            NetworkResult.Success(
                Movie("tt1234567", "Example", "https://example.com/poster.jpg", MovieType.SERIES),
            ),
            OmdbDetailsRepository(api).getMovie(" tt1234567 "),
        )
    }

    @Test
    fun `blank IMDb ID fails locally`() = runBlocking {
        val repository = OmdbDetailsRepository(omdbApi(onRequest = { fail("Unexpected request") }))
        assertEquals(NetworkResult.Failure(NetworkError.InvalidRequest), repository.getMovie(" "))
    }

    @Test
    fun `details maps OMDb failure and invalid content`() = runBlocking {
        assertEquals(
            NetworkResult.Failure(NetworkError.Omdb("Invalid API key!")),
            OmdbDetailsRepository(omdbApi("""{"Response":"False","Error":"Invalid API key!"}"""))
                .getMovie("tt1234567"),
        )
        listOf("{}", """{"Response":"True","Title":"Missing ID"}""").forEach { body ->
            assertEquals(
                NetworkResult.Failure(NetworkError.InvalidResponse),
                OmdbDetailsRepository(omdbApi(body)).getMovie("tt1234567"),
            )
        }
    }

    @Test
    fun `connection failures become typed errors`() = runBlocking {
        val api = object : OmdbApi by omdbApi() {
            override suspend fun searchMovies(query: String, page: Int): Response<OmdbSearchResponseDto> {
                throw IOException("Offline")
            }
        }
        assertEquals(
            NetworkResult.Failure(NetworkError.Connection),
            OmdbSearchRepository(api).searchMovies("movie"),
        )
    }

    @Test
    fun `search and details preserve coroutine cancellation`() = runBlocking {
        val cancellation = CancellationException("Request superseded")
        val api = object : OmdbApi by omdbApi() {
            override suspend fun searchMovies(query: String, page: Int): Response<OmdbSearchResponseDto> {
                throw cancellation
            }

            override suspend fun getMovie(imdbId: String): Response<OmdbMovieDto> {
                throw cancellation
            }
        }
        try {
            OmdbSearchRepository(api).searchMovies("movie")
            fail("Cancellation must propagate")
        } catch (actual: CancellationException) {
            assertSame(cancellation, actual)
        }
        try {
            OmdbDetailsRepository(api).getMovie("tt1234567")
            fail("Cancellation must propagate")
        } catch (actual: CancellationException) {
            assertSame(cancellation, actual)
        }
    }
}
