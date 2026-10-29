package com.jacks.moviet.favorites.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jacks.moviet.core.domain.model.Movie
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
    private data class Writes(
        val movies: Map<String, FavoriteWriteStatus> = emptyMap(),
        val undo: UndoRemoval? = null,
    )

    private var nextUndoId = 0L
    private val retry = MutableStateFlow(0)
    private var hasLoadedFavorites = false
    private val writes = MutableStateFlow(Writes())
    private val eventChannel = Channel<FavoritesEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    private val content = retry.flatMapLatest {
        repository.observeFavorites()
            .map<List<Movie>, FavoritesContent> {
                hasLoadedFavorites = true
                FavoritesContent.Loaded(it)
            }
            // Keep the presented list/scroll anchor while Room reconnects after details.
            .onStart { if (!hasLoadedFavorites) emit(FavoritesContent.Loading) }
            .catch { emit(FavoritesContent.Error) }
    }

    val state = combine(content, writes) { content, write ->
        FavoritesState(content = content, movieWrites = write.movies, undoRemoval = write.undo)
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
            is FavoritesAction.UndoRemove -> undoRemove(action.id)
            is FavoritesAction.DismissUndo -> writes.update {
                if (it.undo?.id == action.id && it.undo.status != UndoStatus.RESTORING) it.copy(undo = null) else it
            }
        }
    }

    private fun removeMovie(imdbId: String) {
        if (writes.value.movies[imdbId] == FavoriteWriteStatus.PENDING) return
        val content = state.value.content as? FavoritesContent.Loaded ?: return
        val movie = content.movies.firstOrNull { it.imdbId == imdbId } ?: return
        writes.update { it.copy(movies = it.movies + (imdbId to FavoriteWriteStatus.PENDING)) }
        viewModelScope.launch {
            try {
                repository.removeFavorite(imdbId)
                val undo = UndoRemoval(id = ++nextUndoId, movie = movie)
                writes.update { it.copy(undo = undo) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                writes.update { it.copy(movies = it.movies + (imdbId to FavoriteWriteStatus.FAILED)) }
            } finally {
                clearPendingWrite(imdbId)
            }
        }
    }

    private fun undoRemove(id: Long) {
        val undo = writes.value.undo?.takeIf { it.id == id && it.status != UndoStatus.RESTORING } ?: return
        val imdbId = undo.movie.imdbId
        if (writes.value.movies[imdbId] == FavoriteWriteStatus.PENDING) return
        writes.update {
            it.copy(movies = it.movies + (imdbId to FavoriteWriteStatus.PENDING), undo = undo.copy(status = UndoStatus.RESTORING))
        }
        viewModelScope.launch {
            try {
                repository.addFavorite(undo.movie)
                writes.update { if (it.undo?.id == id) it.copy(undo = null) else it }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                writes.update {
                    if (it.undo?.id == id) it.copy(undo = undo.copy(status = UndoStatus.FAILED)) else it
                }
            } finally {
                clearPendingWrite(imdbId)
            }
        }
    }

    private fun clearPendingWrite(imdbId: String) {
        writes.update {
            if (it.movies[imdbId] == FavoriteWriteStatus.PENDING) it.copy(movies = it.movies - imdbId) else it
        }
    }
}
