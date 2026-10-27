package com.jacks.moviet.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator

@Composable
internal fun rememberMovieNavigationState(): MovieNavigationState {
    val search = rememberNavBackStack(NavigationRoute.Search)
    val favorites = rememberNavBackStack(NavigationRoute.Favorites)
    val favoritesSelected = rememberSaveable { mutableStateOf(false) }
    return remember(search, favorites, favoritesSelected) {
        MovieNavigationState(search, favorites, favoritesSelected)
    }
}

/** Each tab retains its entries even when only the other tab is displayed. */
internal class MovieNavigationState(
    search: MutableList<NavKey>,
    favorites: MutableList<NavKey>,
    favoritesSelected: MutableState<Boolean>,
) {
    private val stacks = mapOf(NavigationRoute.Search to search, NavigationRoute.Favorites to favorites)
    var favoritesSelected by favoritesSelected
        private set

    private val activeTab get() = if (favoritesSelected) NavigationRoute.Favorites else NavigationRoute.Search
    private val activeStack get() = stacks.getValue(activeTab)

    // Keep Search underneath Favorites so system Back still exits through Search.
    val backStack: List<NavKey>
        get() = if (favoritesSelected) stacks.getValue(NavigationRoute.Search) + activeStack else activeStack.toList()

    fun selectTab(tab: NavigationRoute.MovieList) {
        favoritesSelected = tab == NavigationRoute.Favorites
    }

    fun openDetails(imdbId: String) {
        activeStack.openDetails(imdbId)
    }

    fun selectedMovieId(tab: NavigationRoute.MovieList): String? =
        (stacks.getValue(tab).lastOrNull() as? NavigationRoute.Details)?.imdbId

    fun goBack() {
        if (activeStack.size > 1) activeStack.removeAt(activeStack.lastIndex)
        else selectTab(NavigationRoute.Search)
    }

    @Composable
    fun decoratedEntries(entryProvider: (NavKey) -> NavEntry<NavKey>): List<NavEntry<NavKey>> {
        // Decorate both stacks unconditionally. Removing inactive keys from a shared decorator
        // would clear their ViewModels and saveable scroll/plot state on every tab switch.
        val entries = stacks.mapValues { (_, stack) ->
            rememberDecoratedNavEntries(
                backStack = stack,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider,
            )
        }
        return if (favoritesSelected) entries.getValue(NavigationRoute.Search) + entries.getValue(NavigationRoute.Favorites)
        else entries.getValue(NavigationRoute.Search)
    }
}

internal fun MutableList<NavKey>.openDetails(imdbId: String) {
    val source = filterIsInstance<NavigationRoute.MovieList>().lastOrNull() ?: NavigationRoute.Search
    val route = NavigationRoute.Details(imdbId, source)
    if (lastOrNull() is NavigationRoute.Details) this[lastIndex] = route else add(route)
}
