package com.jacks.moviet.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.jacks.moviet.search.domain.MovieSearchResult
import com.jacks.moviet.search.domain.SearchRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: SearchRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(SearchState())
    val state = mutableState.asStateFlow()

    private val eventChannel = Channel<SearchEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    @OptIn(FlowPreview::class)
    private val searchQuery = state
        .map { it.query.trim() }
        .distinctUntilChanged()
        .debounce { query -> if (query.isEmpty()) 0L else QUERY_DEBOUNCE_MILLIS }
        // Edits can settle back to the query whose results are already displayed.
        .distinctUntilChanged()
        .onEach { query -> mutableState.update { it.copy(activeQuery = query) } }

    @OptIn(ExperimentalCoroutinesApi::class)
    val movies: Flow<PagingData<MovieSearchResult>> = searchQuery
        .flatMapLatest(repository::observeSearchMovies)
        .cachedIn(viewModelScope)

    fun onAction(action: SearchAction) {
        when (action) {
            is SearchAction.QueryChanged -> mutableState.update { it.copy(query = action.query) }
            SearchAction.ClearQuery -> mutableState.update { it.copy(query = "") }
            SearchAction.FavoritesClick -> viewModelScope.launch {
                eventChannel.send(SearchEvent.OpenFavorites)
            }
            is SearchAction.MovieClick -> viewModelScope.launch {
                eventChannel.send(SearchEvent.OpenMovie(action.movie))
            }
        }
    }

    private companion object {
        const val QUERY_DEBOUNCE_MILLIS = 300L
    }
}
