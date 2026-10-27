package com.jacks.moviet.favorites.presentation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.jacks.moviet.core.designsystem.components.MovieNavigation
import com.jacks.moviet.core.designsystem.theme.MovietTheme

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun EmptyFavoritesPhonePreview() = EmptyFavoritesWithNavigation()

@Preview(showBackground = true, widthDp = 320, heightDp = 640, fontScale = 1.5f)
@Composable
private fun EmptyFavoritesLargeTextPreview() = EmptyFavoritesWithNavigation()

@Preview(showBackground = true, widthDp = 240, heightDp = 400)
@Preview(name = "Short list-detail pane", showBackground = true, widthDp = 240, heightDp = 360)
@Preview(name = "Compact large text", showBackground = true, widthDp = 240, heightDp = 360, fontScale = 1.5f)
@Composable
private fun EmptyFavoritesCompactPreview() = EmptyFavoritesWithNavigation(compact = true)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun EmptyFavoritesWithNavigation(compact: Boolean = false) {
    MovietTheme {
        Scaffold(
            contentWindowInsets = WindowInsets(0),
            bottomBar = {
                MovieNavigation(favoritesSelected = true, onSearchClick = {}, onFavoritesClick = {})
            },
        ) { padding ->
            FavoritesScreen(
                state = FavoritesState(isLoading = false),
                compactLayout = compact,
                navigationBarHeight = padding.calculateBottomPadding(),
                onAction = {},
            )
        }
    }
}
