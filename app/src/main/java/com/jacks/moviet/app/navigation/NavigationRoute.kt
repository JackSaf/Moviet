package com.jacks.moviet.app.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface NavigationRoute : NavKey {
    @Serializable
    data object Search : NavigationRoute

    @Serializable
    data object Favorites : NavigationRoute

    @Serializable
    data class Details(val imdbId: String) : NavigationRoute
}
