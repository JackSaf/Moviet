package com.jacks.moviet.search.presentation

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.SavedStateHandle
import androidx.paging.LoadState
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import com.jacks.moviet.core.domain.NetworkError
import com.jacks.moviet.core.domain.NetworkResult
import com.jacks.moviet.search.domain.MovieSearchResult
import com.jacks.moviet.search.domain.SearchPagingException
import com.jacks.moviet.search.domain.SearchRepository
import com.jacks.moviet.testutil.searchPage
import com.jacks.moviet.testutil.searchRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        store.clear()
        dispatcher.scheduler.runCurrent()
        Dispatchers.resetMain()
    }

    @Test
    fun `construction and query actions do not fetch without a result collector`() = runTest(dispatcher) {
        var calls = 0
        val viewModel = viewModel(searchRepository { _, page ->
            calls++
            searchPage(page)
        })

        viewModel.onAction(SearchAction.QueryChanged("batman"))
        advanceTimeBy(1_000)
        runCurrent()

        assertEquals("batman", viewModel.state.value.query)
        assertEquals(0, calls)
    }

    @Test
    fun `blank input never fetches and rapid edits only fetch the final trimmed query`() = runTest(dispatcher) {
        val requests = mutableListOf<String>()
        val viewModel = viewModel(searchRepository { query, page ->
            requests += query
            searchPage(page)
        })
        present(viewModel)
        viewModel.onAction(SearchAction.QueryChanged("   "))
        advanceTimeBy(500)
        runCurrent()
        assertTrue(requests.isEmpty())

        viewModel.onAction(SearchAction.QueryChanged("bat"))
        runCurrent()
        advanceTimeBy(200)
        viewModel.onAction(SearchAction.QueryChanged(" batman "))
        runCurrent()
        advanceTimeBy(299)
        runCurrent()
        assertTrue(requests.isEmpty())
        advanceTimeBy(1)
        runCurrent()

        assertEquals(listOf("batman"), requests)
        assertEquals(" batman ", viewModel.state.value.query)
    }

    @Test
    fun `equivalent trimmed query does not reload and clearing removes results immediately`() = runTest(dispatcher) {
        var calls = 0
        val viewModel = viewModel(searchRepository { _, page ->
            calls++
            searchPage(page)
        })
        val presenter = present(viewModel)
        viewModel.onAction(SearchAction.QueryChanged("batman"))
        runCurrent()
        advanceTimeBy(300)
        runCurrent()
        assertEquals(10, presenter.snapshot().items.size)

        viewModel.onAction(SearchAction.QueryChanged(" batman "))
        runCurrent()
        advanceTimeBy(500)
        runCurrent()
        assertEquals(1, calls)

        viewModel.onAction(SearchAction.ClearQuery)
        runCurrent()
        assertEquals("", viewModel.state.value.query)
        assertTrue(presenter.snapshot().items.isEmpty())
        assertEquals(1, calls)
    }

    @Test
    fun `switching the debounced query cancels the previous active request`() = runTest(dispatcher) {
        var cancelled = false
        val requests = mutableListOf<String>()
        val viewModel = viewModel(searchRepository { query, page ->
            requests += query
            if (query == "batman") {
                try {
                    awaitCancellation()
                } finally {
                    cancelled = true
                }
            }
            searchPage(page)
        })
        present(viewModel)
        viewModel.onAction(SearchAction.QueryChanged("batman"))
        runCurrent()
        advanceTimeBy(300)
        runCurrent()

        viewModel.onAction(SearchAction.QueryChanged("superman"))
        runCurrent()

        assertFalse(cancelled)
        assertEquals(listOf("batman"), requests)
        advanceTimeBy(300)
        runCurrent()
        assertTrue(cancelled)
        assertEquals(listOf("batman", "superman"), requests)
    }

    @Test
    fun `typing shows loading while retaining the snapshot and returning to the active query does not reload`() = runTest(dispatcher) {
        val requests = mutableListOf<String>()
        val viewModel = viewModel(searchRepository { query, page ->
            requests += query
            searchPage(page)
        })
        val presenter = present(viewModel)
        viewModel.onAction(SearchAction.QueryChanged("batman"))
        runCurrent()
        advanceTimeBy(300)
        runCurrent()

        viewModel.onAction(SearchAction.QueryChanged("batmans"))
        runCurrent()
        assertEquals(SearchContent.Loading, viewModel.content(presenter))
        advanceTimeBy(200)
        assertEquals(10, presenter.snapshot().items.size)
        viewModel.onAction(SearchAction.QueryChanged("batman"))
        runCurrent()
        advanceTimeBy(300)
        runCurrent()

        assertEquals(listOf("batman"), requests)
        assertEquals(10, presenter.snapshot().items.size)
        assertEquals(SearchContent.Results(isLoading = false), viewModel.content(presenter))
    }

    @Test
    fun `previous movies and total stay hidden until the new query finishes loading`() = runTest(dispatcher) {
        val newResponse = CompletableDeferred<Unit>()
        val viewModel = viewModel(searchRepository { query, page ->
            if (query == "batman") {
                newResponse.await()
                searchPage(page, total = 1, count = 1)
            } else {
                searchPage(page, total = 123)
            }
        })
        val presenter = present(viewModel)
        viewModel.onAction(SearchAction.QueryChanged("bat"))
        runCurrent()
        advanceTimeBy(300)
        runCurrent()
        assertEquals(123, presenter.snapshot().items.first().totalResults)

        viewModel.onAction(SearchAction.QueryChanged("batman"))
        runCurrent()
        assertEquals(123, presenter.snapshot().items.first().totalResults)
        advanceTimeBy(300)
        runCurrent()
        assertEquals(SearchContent.Loading, viewModel.content(presenter))
        assertEquals(123, presenter.snapshot().items.first().totalResults)

        newResponse.complete(Unit)
        runCurrent()
        assertEquals(1, presenter.snapshot().items.size)
        assertEquals(1, presenter.snapshot().items.first().totalResults)
        assertEquals(SearchContent.Results(isLoading = false), viewModel.content(presenter))

        viewModel.onAction(SearchAction.ClearQuery)
        runCurrent()
        assertTrue(presenter.snapshot().items.isEmpty())
    }

    @Test
    fun `appending pages preserves the API total instead of counting loaded movies`() = runTest(dispatcher) {
        val viewModel = viewModel(searchRepository { _, page ->
            searchPage(page, total = 123)
        })
        val presenter = present(viewModel)
        viewModel.onAction(SearchAction.QueryChanged("batman"))
        runCurrent()
        advanceTimeBy(300)
        runCurrent()
        assertEquals(10, presenter.snapshot().items.size)
        assertEquals(123, presenter.snapshot().items.first().totalResults)

        presenter[9]
        runCurrent()

        assertEquals(20, presenter.snapshot().items.size)
        assertEquals((0 until 20).map { "tt$it" }, presenter.snapshot().items.map { it.movie.imdbId })
        assertTrue(presenter.snapshot().items.all { it.totalResults == 123 })
    }

    @Test
    fun `Paging exposes a typed failure and retries the same query successfully`() = runTest(dispatcher) {
        var calls = 0
        val viewModel = viewModel(searchRepository { _, page ->
            calls++
            if (calls == 1) NetworkResult.Failure(NetworkError.Connection) else searchPage(page)
        })
        val presenter = present(viewModel)
        viewModel.onAction(SearchAction.QueryChanged("batman"))
        runCurrent()
        advanceTimeBy(300)
        runCurrent()

        val error = presenter.loadStateFlow.value?.refresh as LoadState.Error
        assertEquals(NetworkError.Connection, (error.error as SearchPagingException).error)
        presenter.retry()
        runCurrent()

        assertEquals(2, calls)
        assertEquals(10, presenter.snapshot().items.size)
        assertTrue(presenter.loadStateFlow.value?.refresh is LoadState.NotLoading)
    }

    @Test
    fun `query handoff never reveals a snapshot from another query`() {
        val state = SearchState(query = "batma", activeQuery = "batma")

        // The previous generation can still report NotLoading before the new one starts.
        assertEquals(SearchContent.Loading, state.contentFor("bat", LoadState.NotLoading(false)))
        assertEquals(SearchContent.Loading, state.contentFor("bat", LoadState.Loading))
        // Refreshing the same query can keep its own movies visible.
        assertEquals(SearchContent.Results(isLoading = true), state.contentFor("batma", LoadState.Loading))
    }

    @Test
    fun `no matches after a successful search replaces retained movies with the empty state`() = runTest(dispatcher) {
        val emptyResponse = CompletableDeferred<Unit>()
        val viewModel = viewModel(searchRepository { query, page ->
            if (query == "batma") {
                emptyResponse.await()
                NetworkResult.Failure(NetworkError.Omdb("Movie not found!"))
            } else {
                searchPage(page)
            }
        })
        val presenter = present(viewModel)
        viewModel.onAction(SearchAction.QueryChanged("bat"))
        runCurrent()
        advanceTimeBy(300)
        runCurrent()
        assertEquals(SearchContent.Results(isLoading = false), viewModel.content(presenter))

        viewModel.onAction(SearchAction.QueryChanged("batma"))
        runCurrent()
        assertEquals(SearchContent.Loading, viewModel.content(presenter))
        advanceTimeBy(300)
        runCurrent()

        assertEquals(LoadState.Loading, presenter.loadStateFlow.value?.refresh)
        assertEquals(SearchContent.Loading, viewModel.content(presenter))
        emptyResponse.complete(Unit)
        runCurrent()

        // This retained snapshot is what made the screenshot show movies under an error.
        assertEquals(10, presenter.snapshot().items.size)
        assertEquals(SearchContent.Empty, viewModel.content(presenter))

        viewModel.onAction(SearchAction.QueryChanged("batman"))
        runCurrent()
        assertEquals(SearchContent.Loading, viewModel.content(presenter))
        advanceTimeBy(300)
        runCurrent()
        assertEquals(SearchContent.Results(isLoading = false), viewModel.content(presenter))

        viewModel.onAction(SearchAction.ClearQuery)
        assertEquals(SearchContent.Start, viewModel.content(presenter))
        runCurrent()
        assertTrue(presenter.snapshot().items.isEmpty())
    }

    @Test
    fun `failed new search hides retained movies and retry replaces them with the current query`() = runTest(dispatcher) {
        val requests = mutableListOf<String>()
        var failNewQuery = true
        val retryResponse = CompletableDeferred<Unit>()
        val viewModel = viewModel(searchRepository { query, page ->
            requests += query
            when {
                query == "bat" -> searchPage(page)
                failNewQuery -> NetworkResult.Failure(NetworkError.Connection)
                else -> {
                    retryResponse.await()
                    searchPage(page, total = 3, count = 3)
                }
            }
        })
        val presenter = present(viewModel)
        viewModel.onAction(SearchAction.QueryChanged("bat"))
        runCurrent()
        advanceTimeBy(300)
        runCurrent()
        viewModel.onAction(SearchAction.QueryChanged("batman"))
        runCurrent()
        advanceTimeBy(300)
        runCurrent()

        assertEquals(10, presenter.snapshot().items.size)
        assertEquals(SearchContent.Error(NetworkError.Connection), viewModel.content(presenter))

        failNewQuery = false
        presenter.retry()
        runCurrent()

        assertEquals(10, presenter.snapshot().items.size)
        assertEquals(SearchContent.Loading, viewModel.content(presenter))
        retryResponse.complete(Unit)
        runCurrent()

        assertEquals(listOf("bat", "batman", "batman"), requests)
        assertEquals(3, presenter.snapshot().items.size)
        assertEquals(SearchContent.Results(isLoading = false), viewModel.content(presenter))
    }

    @Test
    fun `append failure keeps the current query results visible`() = runTest(dispatcher) {
        val viewModel = viewModel(searchRepository { _, page ->
            if (page == 1) searchPage(page, total = 20) else NetworkResult.Failure(NetworkError.Connection)
        })
        val presenter = present(viewModel)
        viewModel.onAction(SearchAction.QueryChanged("batman"))
        runCurrent()
        advanceTimeBy(300)
        runCurrent()
        presenter[9]
        runCurrent()

        assertTrue(presenter.loadStateFlow.value?.append is LoadState.Error)
        assertEquals(10, presenter.snapshot().items.size)
        assertEquals(SearchContent.Results(isLoading = false), viewModel.content(presenter))
    }

    @Test
    fun `a new collector reuses the cached query results`() = runTest(dispatcher) {
        var calls = 0
        val viewModel = viewModel(searchRepository { _, page ->
            calls++
            searchPage(page)
        })
        val firstPresenter = presenter()
        val firstCollector = backgroundScope.launch {
            viewModel.movies.collectLatest(firstPresenter::collectFrom)
        }
        viewModel.onAction(SearchAction.QueryChanged("batman"))
        runCurrent()
        advanceTimeBy(300)
        runCurrent()
        assertEquals(10, firstPresenter.snapshot().items.size)
        firstCollector.cancel()
        runCurrent()

        val secondPresenter = present(viewModel)
        runCurrent()

        assertEquals(10, secondPresenter.snapshot().items.size)
        assertEquals(1, calls)
    }

    @Test
    fun `favorites action emits a one-off navigation event without changing query`() = runTest(dispatcher) {
        val viewModel = viewModel(searchRepository { _, page -> searchPage(page) })
        viewModel.onAction(SearchAction.QueryChanged("batman"))

        viewModel.onAction(SearchAction.FavoritesClick)

        assertEquals(SearchEvent.OpenFavorites, viewModel.events.first())
        assertEquals("batman", viewModel.state.value.query)
    }

    private fun viewModel(repository: SearchRepository, savedState: SavedStateHandle = SavedStateHandle()) =
        SearchViewModel(repository, savedState).also {
            store.put("search", it)
        }

    @Test
    fun `restored query fetches lazily and clearing persists across recreation`() = runTest(dispatcher) {
        val requests = mutableListOf<String>()
        val repository = searchRepository { query, page ->
            requests += query
            searchPage(page)
        }
        val savedState = SavedStateHandle()
        viewModel(repository, savedState).onAction(SearchAction.QueryChanged(" batman "))
        val restoredState = SavedStateHandle(savedState.keys().associateWith { savedState.get<Any?>(it) })
        val restored = viewModel(repository, restoredState)
        assertEquals(" batman ", restored.state.value.query)
        runCurrent()
        assertTrue(requests.isEmpty())
        present(restored)
        advanceTimeBy(300)
        runCurrent()
        assertEquals(listOf("batman"), requests)

        restored.onAction(SearchAction.ClearQuery)
        val clearedState = SavedStateHandle(restoredState.keys().associateWith { restoredState.get<Any?>(it) })
        val cleared = viewModel(repository, clearedState)
        present(cleared)
        advanceTimeBy(300)
        runCurrent()
        assertEquals("", cleared.state.value.query)
        assertEquals(listOf("batman"), requests)
    }

    @Test
    fun `movie selection emits the selected result without changing the query`() = runTest(dispatcher) {
        val viewModel = viewModel(searchRepository { _, page -> searchPage(page) })
        val movie = com.jacks.moviet.core.domain.model.Movie("tt123", "Example", null, com.jacks.moviet.core.domain.model.MovieType.MOVIE)
        viewModel.onAction(SearchAction.QueryChanged("example"))
        viewModel.onAction(SearchAction.MovieClick(movie))
        assertEquals(SearchEvent.OpenMovie(movie), viewModel.events.first())
        assertEquals("example", viewModel.state.value.query)
    }

    private fun presenter() = object : PagingDataPresenter<MovieSearchResult>(mainContext = dispatcher) {
        override suspend fun presentPagingDataEvent(event: PagingDataEvent<MovieSearchResult>) = Unit
    }

    private fun SearchViewModel.content(presenter: PagingDataPresenter<MovieSearchResult>): SearchContent =
        state.value.contentFor(
            snapshotQuery = presenter.snapshot().items.firstOrNull()?.query,
            refresh = requireNotNull(presenter.loadStateFlow.value).refresh,
        )

    private fun TestScope.present(viewModel: SearchViewModel): PagingDataPresenter<MovieSearchResult> = presenter().also {
        backgroundScope.launch { viewModel.movies.collectLatest(it::collectFrom) }
        runCurrent()
    }
}
