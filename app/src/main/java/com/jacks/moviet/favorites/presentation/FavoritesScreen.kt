package com.jacks.moviet.favorites.presentation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jacks.moviet.core.designsystem.theme.MovietTheme
import com.jacks.moviet.core.domain.model.Movie
import com.jacks.moviet.core.domain.model.MovieType
import com.jacks.moviet.core.presentation.util.ObserveAsEvents
import com.jacks.moviet.favorites.presentation.components.FavoriteTickets
import com.jacks.moviet.favorites.presentation.components.FavoritesHeader
import com.jacks.moviet.favorites.presentation.components.FavoritesSnackbarHost
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FavoritesScreenRoot(
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit,
    onMovieClick: (Movie) -> Unit,
    selectedMovieId: String? = null,
    compactLayout: Boolean = false,
    navigationBarHeight: Dp = 0.dp,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    viewModel: FavoritesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            FavoritesEvent.OpenSearch -> onSearchClick()
            is FavoritesEvent.OpenMovie -> onMovieClick(event.movie)
        }
    }
    FavoritesScreen(
        modifier = modifier,
        state = state,
        selectedMovieId = selectedMovieId,
        compactLayout = compactLayout,
        navigationBarHeight = navigationBarHeight,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FavoritesScreen(
    modifier: Modifier = Modifier,
    state: FavoritesState,
    selectedMovieId: String? = null,
    compactLayout: Boolean = false,
    navigationBarHeight: Dp = 0.dp,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    onAction: (FavoritesAction) -> Unit,
) {
    Scaffold(
        modifier = modifier.testTag("favorites_screen"),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { FavoritesHeader(compact = compactLayout) },
        bottomBar = {
            if (navigationBarHeight > 0.dp) Spacer(modifier = Modifier.height(navigationBarHeight))
        },
        snackbarHost = {
            FavoritesSnackbarHost(
                undoRemoval = state.undoRemoval,
                compactLayout = compactLayout,
                onAction = onAction,
            )
        },
    ) { innerPadding ->
        FavoriteTickets(
            state = state,
            contentPadding = innerPadding,
            selectedMovieId = selectedMovieId,
            compactLayout = compactLayout,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            onAction = onAction,
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Preview(showBackground = true, widthDp = 320, heightDp = 640, fontScale = 1.5f)
@Composable
private fun FavoritesScreenPreview() {
    MovietTheme {
        FavoritesScreen(
            state = FavoritesState(
                isLoading = false,
                movies = listOf(
                    Movie("tt0816692", "Interstellar", null, MovieType.MOVIE),
                    Movie("tt0083658", "Blade Runner", null, MovieType.MOVIE),
                    Movie("tt0903747", "Breaking Bad", null, MovieType.SERIES),
                ),
            ),
            onAction = {},
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true, widthDp = 240, heightDp = 400)
@Composable
private fun FavoritesPanePreview() {
    MovietTheme {
        FavoritesScreen(
            state = FavoritesState(
                isLoading = false,
                movies = listOf(Movie("tt18689424", "Batman v Superman: Dawn of Justice (Ultimate Edition)", null, MovieType.MOVIE)),
            ),
            selectedMovieId = "tt18689424",
            compactLayout = true,
            onAction = {},
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Preview(showBackground = true, widthDp = 320, heightDp = 640, fontScale = 1.5f)
@Composable
private fun EmptyFavoritesPreview() {
    MovietTheme { FavoritesScreen(state = FavoritesState(isLoading = false), onAction = {}) }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true, widthDp = 240, heightDp = 400)
@Composable
private fun EmptyFavoritesPanePreview() {
    MovietTheme {
        FavoritesScreen(state = FavoritesState(isLoading = false), compactLayout = true, onAction = {})
    }
}
