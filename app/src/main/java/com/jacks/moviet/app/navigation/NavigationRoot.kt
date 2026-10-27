package com.jacks.moviet.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.jacks.moviet.core.domain.model.Movie
import com.jacks.moviet.details.presentation.DetailsScreenRoot
import com.jacks.moviet.favorites.presentation.FavoritesScreen
import com.jacks.moviet.search.presentation.SearchScreenRoot

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NavigationRoot(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(NavigationRoute.Search)
    val windowWidth = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.width.toDp() }
    val useTwoPanes = useListDetail(windowWidth)
    val listDetailStrategy = remember(useTwoPanes) { ListDetailScene.Strategy<NavKey>(useTwoPanes) }
    var moviePreview by remember { mutableStateOf<Movie?>(null) }
    val selectedMovieId = (backStack.lastOrNull() as? NavigationRoute.Details)?.imdbId
    val onBack = { if (backStack.size > 1) { backStack.removeLastOrNull() }; Unit }

    SharedTransitionLayout(modifier = modifier) {
      NavDisplay(
        backStack = backStack,
        onBack = onBack,
        sceneStrategies = listOf(listDetailStrategy),
        sharedTransitionScope = this,
        transitionSpec = {
            (slideInHorizontally(tween(NavigationDurationMillis)) { it / SlideDistanceDivisor } + fadeIn(tween(NavigationDurationMillis))) togetherWith
                fadeOut(tween(NavigationDurationMillis))
        },
        popTransitionSpec = {
            fadeIn(tween(NavigationDurationMillis)) togetherWith
                (slideOutHorizontally(tween(NavigationDurationMillis)) { it / SlideDistanceDivisor } + fadeOut(tween(NavigationDurationMillis)))
        },
        predictivePopTransitionSpec = {
            fadeIn() togetherWith (slideOutHorizontally { it / SlideDistanceDivisor } + fadeOut())
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<NavigationRoute.Search>(metadata = ListDetailScene.listPane()) {
                SearchScreenRoot(
                    selectedMovieId = selectedMovieId,
                    compactLayout = LocalTwoPane.current,
                    sharedTransitionScope = if (LocalTwoPane.current) null else this@SharedTransitionLayout,
                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                    onMovieClick = { movie ->
                        moviePreview = movie
                        backStack.openDetails(movie.imdbId)
                    },
                    onFavoritesClick = dropUnlessResumed {
                        backStack.add(NavigationRoute.Favorites)
                    },
                )
            }
            entry<NavigationRoute.Details>(metadata = ListDetailScene.detailPane()) { route ->
                DetailsScreenRoot(
                    imdbId = route.imdbId,
                    preview = moviePreview?.takeIf { it.imdbId == route.imdbId },
                    showBackButton = !LocalTwoPane.current,
                    compactHeader = LocalTwoPane.current,
                    sharedTransitionScope = if (LocalTwoPane.current) null else this@SharedTransitionLayout,
                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                    onBackClick = dropUnlessResumed(block = onBack),
                )
            }
            entry<NavigationRoute.Favorites> {
                FavoritesScreen(onBackClick = dropUnlessResumed(block = onBack))
            }
        },
      )
    }
}

internal fun useListDetail(windowWidth: Dp): Boolean = windowWidth >= ListDetailBreakpoint

internal fun MutableList<NavKey>.openDetails(imdbId: String) {
    val route = NavigationRoute.Details(imdbId)
    if (lastOrNull() is NavigationRoute.Details) this[lastIndex] = route else add(route)
}

private val ListDetailBreakpoint = 600.dp
private const val NavigationDurationMillis = 300
private const val SlideDistanceDivisor = 5
