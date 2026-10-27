package com.jacks.moviet.details.presentation

import androidx.lifecycle.ViewModelStore
import com.jacks.moviet.core.domain.NetworkError
import com.jacks.moviet.core.domain.NetworkResult
import com.jacks.moviet.core.domain.model.Movie
import com.jacks.moviet.core.domain.model.MovieType
import com.jacks.moviet.details.domain.DetailsRepository
import com.jacks.moviet.details.domain.MovieDetails
import com.jacks.moviet.favorites.domain.FavoritesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val movie = Movie("tt123", "Example", null, MovieType.MOVIE, "2024")
    private val favorites = FakeFavorites()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { store.clear(); dispatcher.scheduler.runCurrent(); Dispatchers.resetMain() }

    @Test
    fun `loading is lazy until the state is collected`() = runTest(dispatcher) {
        var calls = 0
        val viewModel = viewModel { calls++; NetworkResult.Success(MovieDetails(movie)) }
        viewModel.onAction(DetailsAction.LoadMovie(movie.imdbId))
        runCurrent()
        assertEquals(0, calls)
        backgroundScope.launch { viewModel.state.collect {} }
        runCurrent()
        assertEquals(1, calls)
        assertEquals(movie, viewModel.state.value.details?.movie)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `failed detail load exposes error and retry succeeds`() = runTest(dispatcher) {
        var calls = 0
        val viewModel = viewModel {
            calls++
            if (calls == 1) NetworkResult.Failure(NetworkError.Connection) else NetworkResult.Success(MovieDetails(movie))
        }
        backgroundScope.launch { viewModel.state.collect {} }
        viewModel.onAction(DetailsAction.LoadMovie(movie.imdbId))
        runCurrent()
        assertEquals(NetworkError.Connection, viewModel.state.value.error)
        viewModel.onAction(DetailsAction.Retry)
        runCurrent()
        assertEquals(2, calls)
        assertEquals(movie, viewModel.state.value.details?.movie)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `successful details survive subscriber restarts without another request`() = runTest(dispatcher) {
        var calls = 0
        val viewModel = viewModel { calls++; NetworkResult.Success(MovieDetails(movie)) }
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        viewModel.onAction(DetailsAction.LoadMovie(movie.imdbId))
        runCurrent()
        collector.cancel()
        advanceTimeBy(6_000)
        runCurrent()
        backgroundScope.launch { viewModel.state.collect {} }
        viewModel.onAction(DetailsAction.LoadMovie(movie.imdbId))
        runCurrent()
        assertEquals(1, calls)
        assertEquals(movie, viewModel.state.value.details?.movie)
    }

    @Test
    fun `changing the movie cancels an obsolete details request`() = runTest(dispatcher) {
        var cancelled = false
        val viewModel = viewModel { id ->
            if (id == "old") {
                try { awaitCancellation() } finally { cancelled = true }
            }
            NetworkResult.Success(MovieDetails(movie))
        }
        backgroundScope.launch { viewModel.state.collect {} }
        viewModel.onAction(DetailsAction.LoadMovie("old"))
        runCurrent()
        viewModel.onAction(DetailsAction.LoadMovie(movie.imdbId))
        runCurrent()
        assertTrue(cancelled)
        assertEquals(movie, viewModel.state.value.details?.movie)
    }

    @Test
    fun `favorite action persists and observes add and remove`() = runTest(dispatcher) {
        val viewModel = viewModel { NetworkResult.Success(MovieDetails(movie)) }
        backgroundScope.launch { viewModel.state.collect {} }
        viewModel.onAction(DetailsAction.LoadMovie(movie.imdbId))
        runCurrent()
        viewModel.onAction(DetailsAction.ToggleFavorite)
        runCurrent()
        assertTrue(viewModel.state.value.isFavorite)
        assertEquals(listOf(movie), favorites.movies.value)
        viewModel.onAction(DetailsAction.ToggleFavorite)
        runCurrent()
        assertFalse(viewModel.state.value.isFavorite)
        assertTrue(favorites.movies.value.isEmpty())
    }

    @Test
    fun `favorite write failures stay visible and can be retried`() = runTest(dispatcher) {
        favorites.failWrites = true
        val viewModel = viewModel { NetworkResult.Success(MovieDetails(movie)) }
        backgroundScope.launch { viewModel.state.collect {} }
        viewModel.onAction(DetailsAction.LoadMovie(movie.imdbId))
        runCurrent()
        viewModel.onAction(DetailsAction.ToggleFavorite)
        runCurrent()
        assertTrue(viewModel.state.value.favoriteError)
        assertFalse(viewModel.state.value.isSavingFavorite)
        assertFalse(viewModel.state.value.isFavorite)
        favorites.failWrites = false
        viewModel.onAction(DetailsAction.ToggleFavorite)
        runCurrent()
        assertTrue(viewModel.state.value.isFavorite)
        assertFalse(viewModel.state.value.favoriteError)
    }

    private fun viewModel(load: suspend (String) -> NetworkResult<MovieDetails>): DetailsViewModel =
        DetailsViewModel(object : DetailsRepository {
            override suspend fun getMovie(imdbId: String) = load(imdbId)
        }, favorites).also { store.put("details", it) }

    private class FakeFavorites : FavoritesRepository {
        val movies = MutableStateFlow<List<Movie>>(emptyList())
        var failWrites = false
        override fun observeFavorites(): Flow<List<Movie>> = movies
        override fun observeIsFavorite(imdbId: String): Flow<Boolean> = movies.map { list -> list.any { it.imdbId == imdbId } }
        override suspend fun addFavorite(movie: Movie) {
            check(!failWrites)
            movies.value = movies.value + movie
        }
        override suspend fun removeFavorite(imdbId: String) {
            check(!failWrites)
            movies.value = movies.value.filterNot { it.imdbId == imdbId }
        }
    }
}
