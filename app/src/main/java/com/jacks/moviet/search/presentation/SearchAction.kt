package com.jacks.moviet.search.presentation

import com.jacks.moviet.core.domain.model.Movie

sealed interface SearchAction {
    data class QueryChanged(val query: String) : SearchAction
    data object ClearQuery : SearchAction
    data object FavoritesClick : SearchAction
    data class MovieClick(val movie: Movie) : SearchAction
}
