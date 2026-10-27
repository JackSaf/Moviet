package com.jacks.moviet.search.presentation

import com.jacks.moviet.core.domain.model.Movie

sealed interface SearchEvent {
    data object OpenFavorites : SearchEvent
    data class OpenMovie(val movie: Movie) : SearchEvent
}
