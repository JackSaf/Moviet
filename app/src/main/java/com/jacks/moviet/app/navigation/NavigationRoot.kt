package com.jacks.moviet.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.jacks.moviet.favorites.presentation.FavoritesScreen
import com.jacks.moviet.search.presentation.SearchScreenRoot

@Composable
fun NavigationRoot(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(NavigationRoute.Search)

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<NavigationRoute.Search> {
                SearchScreenRoot(
                    onFavoritesClick = dropUnlessResumed {
                        backStack.add(NavigationRoute.Favorites)
                    },
                )
            }
            entry<NavigationRoute.Favorites> {
                FavoritesScreen(onBackClick = dropUnlessResumed { backStack.removeLastOrNull() })
            }
        },
    )
}
