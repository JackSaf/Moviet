package com.jacks.moviet.search.presentation.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.paging.compose.LazyPagingItems
import com.jacks.moviet.core.designsystem.components.LocalMovieSharedOverlayClip
import com.jacks.moviet.core.designsystem.components.rememberMovieListOverlayClip
import com.jacks.moviet.core.designsystem.theme.Spacing
import com.jacks.moviet.search.domain.MovieSearchResult
import com.jacks.moviet.search.presentation.SearchAction
import com.jacks.moviet.search.presentation.SearchState
import com.jacks.moviet.sdk.AdsService

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SearchResults(
    modifier: Modifier = Modifier,
    state: SearchState,
    movies: LazyPagingItems<MovieSearchResult>,
    contentPadding: PaddingValues,
    selectedMovieId: String?,
    compactLayout: Boolean,
    sharedTransitionScope: SharedTransitionScope?,
    sharedTransitionRootCoordinates: LayoutCoordinates?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    onAction: (SearchAction) -> Unit,
    ads: AdsService?,
) {
    val direction = LocalLayoutDirection.current
    val listContentPadding = PaddingValues(
        start = contentPadding.calculateStartPadding(direction) + Spacing.medium,
        end = contentPadding.calculateEndPadding(direction) + Spacing.medium,
        top = contentPadding.calculateTopPadding() + Spacing.small,
        bottom = contentPadding.calculateBottomPadding() + Spacing.medium,
    )
    if (state.query.isBlank()) {
        Box(
            modifier = modifier.fillMaxSize()
                .consumeWindowInsets(contentPadding)
                .padding(listContentPadding)
                .testTag("search_results"),
            contentAlignment = Alignment.Center,
        ) {
            EmptySearch(compact = compactLayout)
        }
        return
    }
    val density = LocalDensity.current
    val overlayClip = rememberMovieListOverlayClip(
        animatedVisibilityScope = animatedVisibilityScope.takeIf { sharedTransitionScope != null },
        rootCoordinates = sharedTransitionRootCoordinates,
        headerBottomPx = with(density) { contentPadding.calculateTopPadding().toPx() },
    )
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    LaunchedEffect(listState, focusManager) {
        listState.interactionSource.interactions.collect { interaction ->
            if (interaction is DragInteraction.Start) focusManager.clearFocus()
        }
    }
    LazyColumn(
        modifier = modifier.fillMaxSize().consumeWindowInsets(contentPadding).testTag("search_results"),
        state = listState,
        contentPadding = listContentPadding,
    ) {
        searchResultItems(
            state = state,
            movies = movies,
            compact = compactLayout,
            ads = ads,
            onRetry = {
                focusManager.clearFocus()
                movies.retry()
            },
        ) { movie ->
            CompositionLocalProvider(LocalMovieSharedOverlayClip provides overlayClip) {
                MovieResultRow(
                    movie = movie,
                    selected = selectedMovieId == movie.imdbId,
                    compact = compactLayout,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onClick = { onAction(SearchAction.MovieClick(movie)) },
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}
