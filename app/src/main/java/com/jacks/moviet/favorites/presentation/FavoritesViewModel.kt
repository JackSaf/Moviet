package com.jacks.moviet.favorites.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jacks.moviet.core.presentation.util.StateFlowPolicy
import com.jacks.moviet.favorites.domain.FavoritesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModel(private val repository: FavoritesRepository) : ViewModel() {
    private data class Writes(val pending: Set<String> = emptySet(), val errors: Set<String> = emptySet())

    private val retry = MutableStateFlow(0)
    private var hasLoadedFavorites = false
    private val writes = MutableStateFlow(Writes())
    private val eventChannel = Channel<FavoritesEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    private val content = retry.flatMapLatest {
        repository.observeFavorites()
            .map {
                hasLoadedFavorites = true
                FavoritesState(movies = it, isLoading = false)
            }
            // Keep the presented list/scroll anchor while Room reconnects after details.
            .onStart { if (!hasLoadedFavorites) emit(FavoritesState()) }
            .catch { emit(FavoritesState(isLoading = false, hasLoadError = true)) }
    }

    val state = combine(content, writes) { content, write ->
        content.copy(removingIds = write.pending, removalErrors = write.errors)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(StateFlowPolicy.STOP_TIMEOUT_MILLIS),
        FavoritesState(),
    )

    fun onAction(action: FavoritesAction) {
        when (action) {
            FavoritesAction.Retry -> {
                hasLoadedFavorites = false
                retry.update { it + 1 }
            }
            FavoritesAction.SearchClick -> viewModelScope.launch { eventChannel.send(FavoritesEvent.OpenSearch) }
            is FavoritesAction.MovieClick -> viewModelScope.launch { eventChannel.send(FavoritesEvent.OpenMovie(action.movie)) }
            is FavoritesAction.RemoveMovie -> removeMovie(action.imdbId)
        }
    }

    private fun removeMovie(imdbId: String) {
        if (imdbId in writes.value.pending || state.value.movies.none { it.imdbId == imdbId }) return
        writes.update { it.copy(pending = it.pending + imdbId, errors = it.errors - imdbId) }
        viewModelScope.launch {
            try {
                repository.removeFavorite(imdbId)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                writes.update { it.copy(errors = it.errors + imdbId) }
            } finally {
                writes.update { it.copy(pending = it.pending - imdbId) }
            }
        }
    }
}
