package com.jacks.moviet.details.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jacks.moviet.core.domain.NetworkResult
import com.jacks.moviet.core.presentation.util.StateFlowPolicy
import com.jacks.moviet.details.domain.DetailsRepository
import com.jacks.moviet.details.domain.MovieDetails
import com.jacks.moviet.favorites.domain.FavoritesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModel(
    private val repository: DetailsRepository,
    private val favorites: FavoritesRepository,
) : ViewModel() {
    private data class Request(val id: String, val retry: Int = 0)
    private data class FavoriteWrite(
        val imdbId: String,
        val update: FavoriteUpdate,
    ) {
        // Keep the action disabled until an older movie's write finishes; errors stay with their movie.
        fun updateFor(movieId: String): FavoriteUpdate =
            if (update == FavoriteUpdate.SAVING || imdbId == movieId) update else FavoriteUpdate.IDLE
    }

    private val request = MutableStateFlow<Request?>(null)
    private val favoriteWrite = MutableStateFlow<FavoriteWrite?>(null)
    private val favoriteRetry = MutableStateFlow(0)
    private var cachedDetails: MovieDetails? = null

    // Successful responses survive collection restarts; retry explicitly invalidates the cache.
    private fun loadDetails(current: Request): Flow<DetailsState> = flow {
        val cached = cachedDetails?.takeIf { it.movie.imdbId == current.id }
        if (cached != null) {
            emit(DetailsState.Loaded(cached))
        } else {
            emit(DetailsState.Loading)
            emit(when (val result = repository.getMovie(current.id)) {
                is NetworkResult.Success -> {
                    cachedDetails = result.data
                    DetailsState.Loaded(result.data)
                }
                is NetworkResult.Failure -> DetailsState.Error(result.error)
            })
        }
    }

    private fun observeFavorite(imdbId: String) = favoriteRetry.flatMapLatest {
        favorites.observeIsFavorite(imdbId)
            .map<Boolean, FavoriteState> { FavoriteState.Ready(isFavorite = it) }
            .onStart { emit(FavoriteState.Loading) }
            .catch { emit(FavoriteState.Error) }
    }

    // Scope both sources to one identity so a new movie never inherits the previous
    // movie's favorite status. Local reads cannot block or terminate network content.
    val state = request.filterNotNull().flatMapLatest { current ->
        combine(loadDetails(current), observeFavorite(current.id), favoriteWrite) { content, favorite, write ->
            when (content) {
                is DetailsState.Loaded -> content.copy(
                    favorite = if (favorite is FavoriteState.Ready) {
                        favorite.copy(update = write?.updateFor(current.id) ?: FavoriteUpdate.IDLE)
                    } else favorite,
                )
                else -> content
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(StateFlowPolicy.STOP_TIMEOUT_MILLIS),
        DetailsState.Loading,
    )

    fun onAction(action: DetailsAction) {
        when (action) {
            is DetailsAction.LoadMovie -> if (request.value?.id != action.imdbId) {
                cachedDetails = null
                request.value = Request(action.imdbId)
            }
            DetailsAction.Retry -> {
                cachedDetails = null
                request.update { it?.copy(retry = it.retry + 1) }
            }
            DetailsAction.ToggleFavorite -> toggleFavorite()
            DetailsAction.RetryFavorite -> favoriteRetry.update { it + 1 }
        }
    }

    private fun toggleFavorite() {
        val current = state.value as? DetailsState.Loaded ?: return
        val favorite = current.favorite as? FavoriteState.Ready ?: return
        val movie = current.details.movie
        if (movie.imdbId != request.value?.id || favoriteWrite.value?.update == FavoriteUpdate.SAVING) return
        val remove = favorite.isFavorite
        favoriteWrite.value = FavoriteWrite(movie.imdbId, FavoriteUpdate.SAVING)
        viewModelScope.launch {
            try {
                if (remove) favorites.removeFavorite(movie.imdbId) else favorites.addFavorite(movie)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                favoriteWrite.value = FavoriteWrite(movie.imdbId, FavoriteUpdate.FAILED)
            } finally {
                favoriteWrite.update { if (it?.update == FavoriteUpdate.SAVING) null else it }
            }
        }
    }
}
