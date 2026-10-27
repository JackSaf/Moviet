package com.jacks.moviet.search.presentation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.jacks.moviet.core.designsystem.components.MovieNavigation
import com.jacks.moviet.core.designsystem.theme.MovietTheme
import com.jacks.moviet.search.domain.MovieSearchResult
import kotlinx.coroutines.flow.flowOf

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun EmptySearchPhonePreview() = EmptySearchWithNavigation()

@Preview(showBackground = true, widthDp = 320, heightDp = 640, fontScale = 1.5f)
@Composable
private fun EmptySearchLargeTextPreview() = EmptySearchWithNavigation()

@Preview(showBackground = true, widthDp = 240, heightDp = 400)
@Composable
private fun EmptySearchCompactPreview() = EmptySearchWithNavigation(compact = true)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun EmptySearchWithNavigation(compact: Boolean = false) {
    val movies = remember { flowOf(PagingData.empty<MovieSearchResult>()) }.collectAsLazyPagingItems()
    MovietTheme {
        Scaffold(
            contentWindowInsets = WindowInsets(0),
            bottomBar = {
                MovieNavigation(favoritesSelected = false, onSearchClick = {}, onFavoritesClick = {})
            },
        ) { padding ->
            SearchScreen(
                state = SearchState(),
                movies = movies,
                compactLayout = compact,
                navigationBarHeight = padding.calculateBottomPadding(),
                onAction = {},
            )
        }
    }
}
