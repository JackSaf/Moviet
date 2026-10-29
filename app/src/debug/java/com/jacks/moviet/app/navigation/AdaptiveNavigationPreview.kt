package com.jacks.moviet.app.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.jacks.moviet.core.designsystem.theme.MovietTheme
import com.jacks.moviet.core.domain.model.Movie
import com.jacks.moviet.core.domain.model.MovieType
import com.jacks.moviet.details.domain.MovieDetails
import com.jacks.moviet.details.presentation.DetailsScreen
import com.jacks.moviet.details.presentation.DetailsState
import com.jacks.moviet.details.presentation.FavoriteState
import com.jacks.moviet.search.domain.MovieSearchResult
import com.jacks.moviet.search.presentation.SearchScreen
import com.jacks.moviet.search.presentation.SearchState
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(name = "Phone details", widthDp = 360, heightDp = 800)
@Preview(name = "Landscape list-detail", widthDp = 800, heightDp = 360)
@Preview(name = "Tablet list-detail", widthDp = 1024, heightDp = 768)
@Preview(name = "Narrow large text", widthDp = 320, heightDp = 640, fontScale = 1.5f)
@Composable
private fun AdaptiveNavigationPreview() {
    val movie = remember { Movie("tt0372784", "Batman Begins", null, MovieType.MOVIE, "2005") }
    val movies = remember {
        flowOf(PagingData.from(listOf(
            movie,
            Movie("tt1877830", "The Batman", null, MovieType.MOVIE, "2022"),
        ).map { MovieSearchResult(movie = it, totalResults = 123, query = "Batman") }))
    }.collectAsLazyPagingItems()
    val windowWidth = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.width.toDp() }
    val strategy = remember(windowWidth) { ListDetailScene.Strategy<NavKey>(useListDetail(windowWidth)) }
    val backStack = rememberNavBackStack(NavigationRoute.Search, NavigationRoute.Details(movie.imdbId))
    MovietTheme {
      NavigationScaffold(
        modifier = Modifier.fillMaxSize(),
        useTwoPanes = useListDetail(windowWidth),
        showNavigation = showMovieNavigation(backStack, useListDetail(windowWidth)),
        favoritesSelected = false,
        onSearchClick = {},
        onFavoritesClick = {},
      ) { navigationBarHeight ->
        NavDisplay(
            modifier = Modifier.fillMaxSize(),
            backStack = backStack,
            sceneStrategies = listOf(strategy),
            entryProvider = entryProvider {
                entry<NavigationRoute.Search>(metadata = ListDetailScene.listPane()) {
                    SearchScreen(
                        navigationBarHeight = navigationBarHeight,
                        state = SearchState(query = "Batman", activeQuery = "Batman"),
                        movies = movies,
                        selectedMovieId = movie.imdbId,
                        compactLayout = LocalTwoPane.current,
                        onAction = {},
                    )
                }
                entry<NavigationRoute.Details>(metadata = ListDetailScene.detailPane()) {
                    DetailsScreen(
                        state = DetailsState.Loaded(
                            favorite = FavoriteState.Ready(isFavorite = false),
                            details = MovieDetails(
                                movie = movie,
                                imdbRating = "8.2",
                                contentRating = "PG-13",
                                genres = "Action, Crime, Drama",
                                plot = "Bruce Wayne returns to Gotham City and becomes Batman.",
                                runtime = "140 min",
                            ),
                        ),
                        compactHeader = LocalTwoPane.current,
                        showBackButton = !LocalTwoPane.current,
                        onBackClick = {},
                        onAction = {},
                    )
                }
            },
        )
      }
    }
}
