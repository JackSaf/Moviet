package com.jacks.moviet.search.presentation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.jacks.moviet.core.designsystem.theme.MovietTheme
import com.jacks.moviet.core.domain.model.Movie
import com.jacks.moviet.core.domain.model.MovieType
import com.jacks.moviet.core.presentation.util.ObserveAsEvents
import com.jacks.moviet.search.domain.MovieSearchResult
import com.jacks.moviet.search.presentation.components.SearchHeader
import com.jacks.moviet.search.presentation.components.SearchResults
import kotlinx.coroutines.flow.flowOf
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SearchScreenRoot(
    modifier: Modifier = Modifier,
    onFavoritesClick: () -> Unit,
    onMovieClick: (Movie) -> Unit,
    selectedMovieId: String? = null,
    compactLayout: Boolean = false,
    navigationBarHeight: Dp = 0.dp,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val movies = viewModel.movies.collectAsLazyPagingItems()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            SearchEvent.OpenFavorites -> onFavoritesClick()
            is SearchEvent.OpenMovie -> onMovieClick(event.movie)
        }
    }
    SearchScreen(
        modifier = modifier,
        state = state,
        movies = movies,
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
fun SearchScreen(
    modifier: Modifier = Modifier,
    state: SearchState,
    movies: LazyPagingItems<MovieSearchResult>,
    selectedMovieId: String? = null,
    compactLayout: Boolean = false,
    navigationBarHeight: Dp = 0.dp,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    onAction: (SearchAction) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    Scaffold(
        modifier = modifier
            .testTag("search_screen")
            .pointerInput(focusManager) {
                // Child controls consume their taps; only background taps dismiss input here.
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { SearchHeader(query = state.query, compact = compactLayout, onAction = onAction) },
        bottomBar = {
            if (navigationBarHeight > 0.dp) Spacer(modifier = Modifier.height(navigationBarHeight))
        },
    ) { innerPadding ->
        SearchResults(
            state = state,
            movies = movies,
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
private fun SearchScreenPreview() {
    val movies = remember {
        flowOf(PagingData.from(listOf(
            Movie("tt0372784", "Batman Begins", null, MovieType.MOVIE, "2005"),
            Movie("tt0103359", "Batman: The Animated Series", null, MovieType.SERIES, "1992–1995"),
        ).map { MovieSearchResult(movie = it, totalResults = 123) }))
    }.collectAsLazyPagingItems()
    MovietTheme {
        SearchScreen(state = SearchState(query = "Batman", activeQuery = "Batman"), movies = movies, onAction = {})
    }
}
