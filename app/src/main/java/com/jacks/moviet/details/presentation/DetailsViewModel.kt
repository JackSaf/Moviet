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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModel(
    private val repository: DetailsRepository,
    private val favorites: FavoritesRepository,
) : ViewModel() {
    private data class Request(val id: String, val retry: Int = 0)
    private data class FavoriteWrite(val saving: Boolean = false, val failed: Boolean = false)

    private val request = MutableStateFlow<Request?>(null)
    private val favoriteWrite = MutableStateFlow(FavoriteWrite())
    private var cachedDetails: MovieDetails? = null

    // Successful responses survive collection restarts; retry explicitly invalidates the cache.
    private val content = request.filterNotNull().flatMapLatest { current ->
        flow {
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
    }
    private val favorite = request.filterNotNull().map { it.id }.distinctUntilChanged()
        .flatMapLatest(favorites::observeIsFavorite)

    val state = combine(content, favorite, favoriteWrite) { contentState, isFavorite, write ->
        contentState.copy(isFavorite = isFavorite, isSavingFavorite = write.saving, favoriteError = write.failed)
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
        }
    }

    private fun toggleFavorite() {
        val movie = state.value.details?.movie ?: return
        if (favoriteWrite.value.saving) return
        val remove = state.value.isFavorite
        favoriteWrite.value = FavoriteWrite(saving = true)
        viewModelScope.launch {
            try {
                if (remove) favorites.removeFavorite(movie.imdbId) else favorites.addFavorite(movie)
                favoriteWrite.value = FavoriteWrite()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                favoriteWrite.value = FavoriteWrite(failed = true)
            }
        }
    }
}
