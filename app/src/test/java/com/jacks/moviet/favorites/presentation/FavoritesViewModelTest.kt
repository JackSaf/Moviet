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
        assertEquals(FavoritesContent.Loading, viewModel.state.value.content)
        backgroundScope.launch { viewModel.state.collect {} }
        runCurrent()
        assertTrue(viewModel.loadedMovies().isEmpty())
        repository.movies.value = listOf(movie)
        runCurrent()
        assertEquals(listOf(movie), viewModel.loadedMovies())
        repository.movies.value = emptyList()
        runCurrent()
        assertTrue(viewModel.loadedMovies().isEmpty())
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
        assertEquals(listOf(movie), viewModel.loadedMovies())
    }

    @Test
    fun `read errors are distinct from an empty collection and retry resubscribes`() = runTest(dispatcher) {
        repository.failReads = true
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        runCurrent()
        assertEquals(FavoritesContent.Error, viewModel.state.value.content)
        repository.failReads = false
        repository.movies.value = listOf(movie)
        viewModel.onAction(FavoritesAction.Retry)
        runCurrent()
        assertEquals(listOf(movie), viewModel.loadedMovies())
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
        assertEquals(listOf(movie), viewModel.loadedMovies())
        repository.readGate!!.complete(Unit)
        runCurrent()
        assertTrue(viewModel.loadedMovies().isEmpty())
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
        assertEquals(mapOf(movie.imdbId to FavoriteWriteStatus.PENDING), viewModel.state.value.movieWrites)
        assertEquals(1, repository.removals)
        assertEquals(listOf(movie), viewModel.loadedMovies())
        repository.removeGate!!.complete(Unit)
        runCurrent()
        assertTrue(viewModel.loadedMovies().isEmpty())
        assertTrue(viewModel.state.value.movieWrites.isEmpty())
        assertEquals(movie, viewModel.state.value.undoRemoval?.movie)
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
        assertEquals(listOf(movie), viewModel.loadedMovies())
        assertEquals(mapOf(movie.imdbId to FavoriteWriteStatus.FAILED), viewModel.state.value.movieWrites)
        assertNull(viewModel.state.value.undoRemoval)
        assertFalse(FavoriteWriteStatus.PENDING in viewModel.state.value.movieWrites.values)
        repository.failWrites = false
        viewModel.onAction(FavoritesAction.RemoveMovie(movie.imdbId))
        runCurrent()
        assertTrue(viewModel.loadedMovies().isEmpty())
        assertFalse(FavoriteWriteStatus.FAILED in viewModel.state.value.movieWrites.values)
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
        assertFalse(FavoriteWriteStatus.FAILED in viewModel.state.value.movieWrites.values)
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

    @Test
    fun `undo restores the removed movie once and clears the prompt`() = runTest(dispatcher) {
        val viewModel = loadedViewModel(movie)
        viewModel.onAction(FavoritesAction.RemoveMovie(movie.imdbId))
        runCurrent()
        val undo = viewModel.state.value.undoRemoval!!
        repository.addGate = CompletableDeferred()
        viewModel.onAction(FavoritesAction.UndoRemove(undo.id))
        viewModel.onAction(FavoritesAction.UndoRemove(undo.id))
        viewModel.onAction(FavoritesAction.DismissUndo(undo.id))
        runCurrent()
        assertEquals(UndoStatus.RESTORING, viewModel.state.value.undoRemoval!!.status)
        assertEquals(1, repository.additions)
        repository.addGate!!.complete(Unit)
        runCurrent()
        assertEquals(listOf(movie), viewModel.loadedMovies())
        assertNull(viewModel.state.value.undoRemoval)
        assertFalse(FavoriteWriteStatus.PENDING in viewModel.state.value.movieWrites.values)
    }

    @Test
    fun `failed undo retains the movie for a successful retry`() = runTest(dispatcher) {
        val viewModel = loadedViewModel(movie)
        viewModel.onAction(FavoritesAction.RemoveMovie(movie.imdbId))
        runCurrent()
        val id = viewModel.state.value.undoRemoval!!.id
        repository.failWrites = true
        viewModel.onAction(FavoritesAction.UndoRemove(id))
        runCurrent()
        assertTrue(viewModel.loadedMovies().isEmpty())
        assertEquals(UndoStatus.FAILED, viewModel.state.value.undoRemoval!!.status)
        assertFalse(FavoriteWriteStatus.PENDING in viewModel.state.value.movieWrites.values)
        repository.failWrites = false
        viewModel.onAction(FavoritesAction.UndoRemove(id))
        runCurrent()
        assertEquals(listOf(movie), viewModel.loadedMovies())
        assertNull(viewModel.state.value.undoRemoval)
    }

    @Test
    fun `new removal replaces undo and ignores stale snackbar callbacks`() = runTest(dispatcher) {
        val second = movie.copy(imdbId = "tt456", title = "Second movie")
        val viewModel = loadedViewModel(movie, second)
        viewModel.onAction(FavoritesAction.RemoveMovie(movie.imdbId))
        runCurrent()
        val firstId = viewModel.state.value.undoRemoval!!.id
        viewModel.onAction(FavoritesAction.RemoveMovie(second.imdbId))
        runCurrent()
        val latest = viewModel.state.value.undoRemoval!!
        viewModel.onAction(FavoritesAction.DismissUndo(firstId))
        viewModel.onAction(FavoritesAction.UndoRemove(firstId))
        runCurrent()
        assertEquals(latest, viewModel.state.value.undoRemoval)
        assertEquals(0, repository.additions)
        viewModel.onAction(FavoritesAction.UndoRemove(latest.id))
        runCurrent()
        assertEquals(listOf(second), viewModel.loadedMovies())
    }

    @Test
    fun `dismissing undo keeps deletion persisted and prevents late restore`() = runTest(dispatcher) {
        val viewModel = loadedViewModel(movie)
        viewModel.onAction(FavoritesAction.RemoveMovie(movie.imdbId))
        runCurrent()
        val id = viewModel.state.value.undoRemoval!!.id
        viewModel.onAction(FavoritesAction.DismissUndo(id))
        viewModel.onAction(FavoritesAction.UndoRemove(id))
        runCurrent()
        assertNull(viewModel.state.value.undoRemoval)
        assertTrue(repository.movies.value.isEmpty())
        assertEquals(0, repository.additions)
    }

    @Test
    fun `finishing an older restore preserves the newer undo prompt`() = runTest(dispatcher) {
        val second = movie.copy(imdbId = "tt456", title = "Second movie")
        val viewModel = loadedViewModel(movie, second)
        viewModel.onAction(FavoritesAction.RemoveMovie(movie.imdbId))
        runCurrent()
        repository.addGate = CompletableDeferred()
        viewModel.onAction(FavoritesAction.UndoRemove(viewModel.state.value.undoRemoval!!.id))
        runCurrent()
        viewModel.onAction(FavoritesAction.RemoveMovie(second.imdbId))
        runCurrent()
        val latest = viewModel.state.value.undoRemoval
        repository.addGate!!.complete(Unit)
        runCurrent()
        assertEquals(listOf(movie), viewModel.loadedMovies())
        assertEquals(latest, viewModel.state.value.undoRemoval)
        assertEquals(second, latest!!.movie)
    }

    private fun kotlinx.coroutines.test.TestScope.loadedViewModel(vararg movies: Movie): FavoritesViewModel {
        repository.movies.value = movies.toList()
        return viewModel().also {
            backgroundScope.launch { it.state.collect {} }
            runCurrent()
        }
    }

    private fun viewModel() = FavoritesViewModel(repository).also { store.put("favorites", it) }

    private fun FavoritesViewModel.loadedMovies() = (state.value.content as FavoritesContent.Loaded).movies

    private class FakeFavorites : FavoritesRepository {
        val movies = MutableStateFlow<List<Movie>>(emptyList())
        var reads = 0
        var activeCollectors = 0
        var removals = 0
        var additions = 0
        var failReads = false
        var failWrites = false
        var removeGate: CompletableDeferred<Unit>? = null
        var readGate: CompletableDeferred<Unit>? = null
        var addGate: CompletableDeferred<Unit>? = null
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
        override suspend fun addFavorite(movie: Movie) {
            additions++
            addGate?.await()
            check(!failWrites)
            movies.value = movies.value.filterNot { it.imdbId == movie.imdbId } + movie
        }
        override suspend fun removeFavorite(imdbId: String) {
            removals++
            removeGate?.await()
            check(!failWrites)
            movies.value = movies.value.filterNot { it.imdbId == imdbId }
        }
    }
}
