package com.jacks.moviet.app.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface NavigationRoute : NavKey {
    @Serializable
    sealed interface MovieList : NavigationRoute

    @Serializable
    data object Search : MovieList

    @Serializable
    data object Favorites : MovieList

    @Serializable
    data class Details(val imdbId: String, val source: MovieList = Search) : NavigationRoute
}
