package com.jacks.moviet.favorites.presentation

import androidx.lifecycle.ViewModelStore
import com.jacks.moviet.core.domain.model.Movie
import com.jacks.moviet.core.domain.model.MovieType
import com.jacks.moviet.core.presentation.util.StateFlowPolicy
import com.jacks.moviet.favorites.domain.FavoritesRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val movie = Movie("tt123", "Saved movie", null, MovieType.MOVIE)
    private val repository = FakeFavorites()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { store.clear(); dispatcher.scheduler.runCurrent(); Dispatchers.resetMain() }

    @Test
    fun `observation is lazy and Room changes update the collection`() = runTest(dispatcher) {
        val viewModel = viewModel()
        runCurrent()
        assertEquals(0, repository.reads)
        assertTrue(viewModel.state.value.isLoading)
        backgroundScope.launch { viewModel.state.collect {} }
        runCurrent()
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.movies.isEmpty())
        repository.movies.value = listOf(movie)
        runCurrent()
        assertEquals(listOf(movie), viewModel.state.value.movies)
        repository.movies.value = emptyList()
        runCurrent()
        assertTrue(viewModel.state.value.movies.isEmpty())
    }

    @Test
    fun `observation stops without subscribers and resumes with current favorites`() = runTest(dispatcher) {
        val viewModel = viewModel()
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        runCurrent()
        assertEquals(1, repository.activeCollectors)
        collector.cancel()
        advanceTimeBy(StateFlowPolicy.STOP_TIMEOUT_MILLIS + 1)
        runCurrent()
        assertEquals(0, repository.activeCollectors)
        repository.movies.value = listOf(movie)
        backgroundScope.launch { viewModel.state.collect {} }
        runCurrent()
        assertEquals(1, repository.activeCollectors)
        assertEquals(listOf(movie), viewModel.state.value.movies)
    }

    @Test
    fun `read errors are distinct from an empty collection and retry resubscribes`() = runTest(dispatcher) {
        repository.failReads = true
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        runCurrent()
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.hasLoadError)
        repository.failReads = false
        repository.movies.value = listOf(movie)
        viewModel.onAction(FavoritesAction.Retry)
        runCurrent()
        assertFalse(viewModel.state.value.hasLoadError)
        assertEquals(listOf(movie), viewModel.state.value.movies)
        assertEquals(2, repository.reads)
    }

    @Test
    fun `returning from details keeps presented tickets while Room reconnects`() = runTest(dispatcher) {
        repository.movies.value = listOf(movie)
        val viewModel = viewModel()
        val collector = backgroundScope.launch { viewModel.state.collect {} }
        runCurrent()
        collector.cancel()
        advanceTimeBy(StateFlowPolicy.STOP_TIMEOUT_MILLIS + 1)
        runCurrent()
        repository.readGate = CompletableDeferred()
        repository.movies.value = emptyList()
        backgroundScope.launch { viewModel.state.collect {} }
        runCurrent()
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(listOf(movie), viewModel.state.value.movies)
        repository.readGate!!.complete(Unit)
        runCurrent()
        assertTrue(viewModel.state.value.movies.isEmpty())
    }

    @Test
    fun `removal is persisted once while pending and the observed row disappears`() = runTest(dispatcher) {
        repository.movies.value = listOf(movie)
        repository.removeGate = CompletableDeferred()
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        runCurrent()
        viewModel.onAction(FavoritesAction.RemoveMovie(movie.imdbId))
        viewModel.onAction(FavoritesAction.RemoveMovie(movie.imdbId))
        runCurrent()
        assertEquals(setOf(movie.imdbId), viewModel.state.value.removingIds)
        assertEquals(1, repository.removals)
        assertEquals(listOf(movie), viewModel.state.value.movies)
        repository.removeGate!!.complete(Unit)
        runCurrent()
        assertTrue(viewModel.state.value.movies.isEmpty())
        assertTrue(viewModel.state.value.removingIds.isEmpty())
        assertTrue(viewModel.state.value.removalErrors.isEmpty())
    }

    @Test
    fun `failed removal keeps the ticket and can be retried`() = runTest(dispatcher) {
        repository.movies.value = listOf(movie)
        repository.failWrites = true
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        runCurrent()
        viewModel.onAction(FavoritesAction.RemoveMovie(movie.imdbId))
        runCurrent()
        assertEquals(listOf(movie), viewModel.state.value.movies)
        assertEquals(setOf(movie.imdbId), viewModel.state.value.removalErrors)
        assertTrue(viewModel.state.value.removingIds.isEmpty())
        repository.failWrites = false
        viewModel.onAction(FavoritesAction.RemoveMovie(movie.imdbId))
        runCurrent()
        assertTrue(viewModel.state.value.movies.isEmpty())
        assertTrue(viewModel.state.value.removalErrors.isEmpty())
    }

    @Test
    fun `clearing the entry cancels pending writes without reporting a failure`() = runTest(dispatcher) {
        repository.movies.value = listOf(movie)
        repository.removeGate = CompletableDeferred()
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        runCurrent()
        viewModel.onAction(FavoritesAction.RemoveMovie(movie.imdbId))
        runCurrent()
        store.clear()
        runCurrent()
        assertEquals(listOf(movie), repository.movies.value)
        assertTrue(viewModel.state.value.removalErrors.isEmpty())
    }

    @Test
    fun `movie and search actions emit navigation events`() = runTest(dispatcher) {
        val viewModel = viewModel()
        val movieEvent = async { viewModel.events.first() }
        viewModel.onAction(FavoritesAction.MovieClick(movie))
        runCurrent()
        assertEquals(FavoritesEvent.OpenMovie(movie), movieEvent.await())
        val searchEvent = async { viewModel.events.first() }
        viewModel.onAction(FavoritesAction.SearchClick)
        runCurrent()
        assertEquals(FavoritesEvent.OpenSearch, searchEvent.await())
        assertEquals(0, repository.reads)
    }

    private fun viewModel() = FavoritesViewModel(repository).also { store.put("favorites", it) }

    private class FakeFavorites : FavoritesRepository {
        val movies = MutableStateFlow<List<Movie>>(emptyList())
        var reads = 0
        var activeCollectors = 0
        var removals = 0
        var failReads = false
        var failWrites = false
        var removeGate: CompletableDeferred<Unit>? = null
        var readGate: CompletableDeferred<Unit>? = null
        override fun observeFavorites(): Flow<List<Movie>> = flow {
            reads++
            activeCollectors++
            try {
                check(!failReads)
                readGate?.await()
                emitAll(movies)
            } finally { activeCollectors-- }
        }
        override fun observeIsFavorite(imdbId: String): Flow<Boolean> = movies.map { list -> list.any { it.imdbId == imdbId } }
        override suspend fun addFavorite(movie: Movie) { movies.value = movies.value + movie }
        override suspend fun removeFavorite(imdbId: String) {
            removals++
            removeGate?.await()
            check(!failWrites)
            movies.value = movies.value.filterNot { it.imdbId == imdbId }
        }
    }
}
