package com.jacks.moviet.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.jacks.moviet.core.designsystem.theme.MovietMotion
import com.jacks.moviet.core.domain.model.Movie
import com.jacks.moviet.core.designsystem.components.LocalMovieSharedContentNamespace
import com.jacks.moviet.details.presentation.DetailsScreenRoot
import com.jacks.moviet.favorites.presentation.FavoritesScreenRoot
import com.jacks.moviet.search.presentation.SearchScreenRoot

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NavigationRoot(modifier: Modifier = Modifier) {
    val navigation = rememberMovieNavigationState()
    val windowWidth = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.width.toDp() }
    val useTwoPanes = useListDetail(windowWidth)
    val layoutDirection = LocalLayoutDirection.current
    val listDetailStrategy = remember(useTwoPanes) { ListDetailScene.Strategy<NavKey>(useTwoPanes) }
    val moviePreviews = remember { mutableStateMapOf<NavigationRoute.MovieList, Movie>() }
    var sharedLayoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val onBack = navigation::goBack

    NavigationScaffold(
        modifier = modifier,
        useTwoPanes = useTwoPanes,
        showNavigation = showMovieNavigation(navigation.backStack, useTwoPanes),
        favoritesSelected = navigation.favoritesSelected,
        onSearchClick = dropUnlessResumed { navigation.selectTab(NavigationRoute.Search) },
        onFavoritesClick = dropUnlessResumed { navigation.selectTab(NavigationRoute.Favorites) },
    ) { navigationBarHeight ->
        val currentNavigationBarHeight by rememberUpdatedState(navigationBarHeight)
        SharedTransitionLayout(
            modifier = Modifier.fillMaxSize().onGloballyPositioned { sharedLayoutCoordinates = it },
        ) {
            NavDisplay(
                onBack = onBack,
                sceneStrategies = listOf(listDetailStrategy),
                sharedTransitionScope = this,
                transitionSpec = {
                    if (isTopLevelSwitch(initialState, targetState)) {
                        fadeThrough()
                    } else {
                        horizontalSharedAxis(forward = true, layoutDirection = layoutDirection)
                    }
                },
                popTransitionSpec = {
                    if (isTopLevelSwitch(initialState, targetState)) {
                        fadeThrough()
                    } else {
                        horizontalSharedAxis(forward = false, layoutDirection = layoutDirection)
                    }
                },
                // Keep the full-screen surface stationary while Navigation 3 seeks the back gesture.
                predictivePopTransitionSpec = {
                    (fadeIn(MovietMotion.defaultEffects()) togetherWith fadeOut(MovietMotion.defaultEffects()))
                        .using(null)
                },
                entries = navigation.decoratedEntries(entryProvider {
                    entry<NavigationRoute.Search>(metadata = ListDetailScene.listPane() + topLevelNavigation(NavigationRoute.Search)) {
                        CompositionLocalProvider(LocalMovieSharedContentNamespace provides NavigationRoute.Search) {
                            SearchScreenRoot(
                                navigationBarHeight = currentNavigationBarHeight,
                                selectedMovieId = navigation.selectedMovieId(NavigationRoute.Search),
                                compactLayout = LocalTwoPane.current,
                                sharedTransitionScope = if (LocalTwoPane.current) null else this@SharedTransitionLayout,
                                sharedTransitionRootCoordinates = sharedLayoutCoordinates,
                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                onMovieClick = { movie ->
                                    moviePreviews[NavigationRoute.Search] = movie
                                    navigation.openDetails(movie.imdbId)
                                },
                                onFavoritesClick = dropUnlessResumed {
                                    navigation.selectTab(NavigationRoute.Favorites)
                                },
                            )
                        }
                    }
                    entry<NavigationRoute.Details>(metadata = { route ->
                        ListDetailScene.detailPane() + topLevelNavigation(route.source)
                    }) { route ->
                        CompositionLocalProvider(LocalMovieSharedContentNamespace provides route.source) {
                            DetailsScreenRoot(
                                imdbId = route.imdbId,
                                preview = moviePreviews[route.source]?.takeIf { it.imdbId == route.imdbId },
                                showBackButton = !LocalTwoPane.current,
                                compactHeader = LocalTwoPane.current,
                                sharedTransitionScope = if (LocalTwoPane.current) null else this@SharedTransitionLayout,
                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                onBackClick = dropUnlessResumed(block = onBack),
                            )
                        }
                    }
                    entry<NavigationRoute.Favorites>(metadata = ListDetailScene.listPane() + topLevelNavigation(NavigationRoute.Favorites)) {
                        CompositionLocalProvider(LocalMovieSharedContentNamespace provides NavigationRoute.Favorites) {
                            FavoritesScreenRoot(
                                navigationBarHeight = currentNavigationBarHeight,
                                selectedMovieId = navigation.selectedMovieId(NavigationRoute.Favorites),
                                compactLayout = LocalTwoPane.current,
                                sharedTransitionScope = if (LocalTwoPane.current) null else this@SharedTransitionLayout,
                                sharedTransitionRootCoordinates = sharedLayoutCoordinates,
                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                onMovieClick = { movie ->
                                    moviePreviews[NavigationRoute.Favorites] = movie
                                    navigation.openDetails(movie.imdbId)
                                },
                                onSearchClick = dropUnlessResumed { navigation.selectTab(NavigationRoute.Search) },
                            )
                        }
                    }
                }),
            )
        }
    }
}

internal fun useListDetail(windowWidth: Dp): Boolean = windowWidth >= ListDetailBreakpoint

internal fun showMovieNavigation(backStack: List<NavKey>, useTwoPanes: Boolean): Boolean =
    useTwoPanes || backStack.lastOrNull() == NavigationRoute.Search || backStack.lastOrNull() == NavigationRoute.Favorites

private val ListDetailBreakpoint = 600.dp
