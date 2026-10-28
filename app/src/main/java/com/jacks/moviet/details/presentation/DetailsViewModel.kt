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
        val imdbId: String? = null,
        val saving: Boolean = false,
        val failed: Boolean = false,
    )
    private data class FavoriteStatus(
        val isFavorite: Boolean = false,
        val isLoaded: Boolean = false,
        val hasError: Boolean = false,
    )

    private val request = MutableStateFlow<Request?>(null)
    private val favoriteWrite = MutableStateFlow(FavoriteWrite())
    private val favoriteRetry = MutableStateFlow(0)
    private var cachedDetails: MovieDetails? = null

    // Successful responses survive collection restarts; retry explicitly invalidates the cache.
    private fun loadDetails(current: Request) = flow {
        val cached = cachedDetails?.takeIf { it.movie.imdbId == current.id }
        if (cached != null) {
            emit(DetailsState(isLoading = false, details = cached))
        } else {
            emit(DetailsState())
            emit(when (val result = repository.getMovie(current.id)) {
                is NetworkResult.Success -> {
                    cachedDetails = result.data
                    DetailsState(isLoading = false, details = result.data)
                }
                is NetworkResult.Failure -> DetailsState(isLoading = false, error = result.error)
            })
        }
    }

    private fun observeFavorite(imdbId: String) = favoriteRetry.flatMapLatest {
        favorites.observeIsFavorite(imdbId)
            .map { FavoriteStatus(isFavorite = it, isLoaded = true) }
            .onStart { emit(FavoriteStatus()) }
            .catch { emit(FavoriteStatus(hasError = true)) }
    }

    // Scope both sources to one identity so a new movie never inherits the previous
    // movie's favorite status. Local reads cannot block or terminate network content.
    val state = request.filterNotNull().flatMapLatest { current ->
        combine(loadDetails(current), observeFavorite(current.id), favoriteWrite) { content, favorite, write ->
            content.copy(
                isFavorite = favorite.isFavorite,
                isFavoriteLoaded = favorite.isLoaded,
                favoriteLoadError = favorite.hasError,
                isSavingFavorite = write.saving,
                favoriteError = write.imdbId == current.id && write.failed,
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(StateFlowPolicy.STOP_TIMEOUT_MILLIS),
        DetailsState(),
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
        val current = state.value
        val movie = current.details?.movie ?: return
        if (movie.imdbId != request.value?.id || !current.isFavoriteLoaded || favoriteWrite.value.saving) return
        val remove = current.isFavorite
        favoriteWrite.value = FavoriteWrite(imdbId = movie.imdbId, saving = true)
        viewModelScope.launch {
            try {
                if (remove) favorites.removeFavorite(movie.imdbId) else favorites.addFavorite(movie)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                favoriteWrite.value = FavoriteWrite(imdbId = movie.imdbId, failed = true)
            } finally {
                favoriteWrite.update { it.copy(saving = false) }
            }
        }
    }
}
