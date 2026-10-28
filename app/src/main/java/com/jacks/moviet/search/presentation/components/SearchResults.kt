package com.jacks.moviet.search.presentation.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.jacks.moviet.R
import com.jacks.moviet.core.designsystem.components.rememberMovieListOverlayClip
import com.jacks.moviet.core.designsystem.components.LocalMovieSharedOverlayClip
import com.jacks.moviet.core.designsystem.components.ScreenMessage
import com.jacks.moviet.core.designsystem.theme.Spacing
import com.jacks.moviet.core.presentation.util.asUiText
import com.jacks.moviet.search.domain.MovieSearchResult
import com.jacks.moviet.search.domain.SearchPagingException
import com.jacks.moviet.search.presentation.SearchAction
import com.jacks.moviet.search.presentation.SearchContent
import com.jacks.moviet.search.presentation.SearchState
import com.jacks.moviet.search.presentation.contentFor
import com.jacks.moviet.sdk.AdsService
import com.jacks.moviet.sdk.NativeAdSlot

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
    val retry = {
        focusManager.clearFocus()
        movies.retry()
    }
    val movieRow: @Composable (Int) -> Unit = { index ->
        movies[index]?.movie?.let { movie ->
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
    LazyColumn(
        modifier = modifier.fillMaxSize().consumeWindowInsets(contentPadding).testTag("search_results"),
        state = listState,
        contentPadding = PaddingValues(
            start = contentPadding.calculateStartPadding(direction) + Spacing.medium,
            end = contentPadding.calculateEndPadding(direction) + Spacing.medium,
            top = contentPadding.calculateTopPadding() + Spacing.small,
            bottom = contentPadding.calculateBottomPadding() + Spacing.medium,
        ),
        verticalArrangement = if (state.query.isBlank()) Arrangement.Center else Arrangement.Top,
    ) {
        // Lazy list content can be rebuilt before the enclosing composable recomposes.
        // Decide from the same Paging snapshot used below, including after a clear.
        val content = state.contentFor(movies.itemCount, movies.loadState.refresh)
        when (content) {
            SearchContent.Start -> item(key = "start") {
                EmptySearch(compact = compactLayout)
            }
            SearchContent.Loading -> item(key = "loading") {
                LoadingResults(modifier = Modifier.fillParentMaxSize())
            }
            SearchContent.Empty -> item(key = "empty") {
                ScreenMessage(
                    modifier = Modifier.fillParentMaxSize(),
                    title = stringResource(R.string.search_no_results),
                    message = stringResource(R.string.search_no_results_message),
                )
            }
            is SearchContent.Error -> item(key = "error") {
                ScreenMessage(
                    modifier = Modifier.fillParentMaxSize(),
                    title = stringResource(R.string.search_error_title),
                    message = content.error?.asUiText()?.asString() ?: stringResource(R.string.error_service),
                    onRetry = retry,
                )
            }
            is SearchContent.Results -> {
                val totalResults = movies.itemSnapshotList.items.first().totalResults
                val showAds = ads != null && totalResults >= 3 && movies.itemCount >= 3
                val movieKey = movies.itemKey { it.movie.imdbId }
                item(key = "results_heading") {
                    ResultsHeading(totalResults = totalResults, isLoading = content.isLoading)
                }
                if (showAds) {
                    item(key = movieKey(0), contentType = "movie") {
                        movieRow(0)
                    }
                    item(key = "native_ad_1", contentType = "ad") {
                        ads.NativeAd(modifier = Modifier.fillMaxWidth(), slot = NativeAdSlot.FIRST, compact = compactLayout)
                    }
                    item(key = movieKey(1), contentType = "movie") {
                        movieRow(1)
                    }
                    item(key = "native_ad_3", contentType = "ad") {
                        ads.NativeAd(modifier = Modifier.fillMaxWidth(), slot = NativeAdSlot.SECOND, compact = compactLayout)
                    }
                }
                val remainingStart = if (showAds) 2 else 0
                items(
                    count = movies.itemCount - remainingStart,
                    key = { offset -> movieKey(offset + remainingStart) },
                    contentType = { "movie" },
                ) { offset ->
                    movieRow(offset + remainingStart)
                }
                val append = movies.loadState.append
                if (append is LoadState.Loading || append is LoadState.Error) {
                    item(key = "append_status") {
                        AppendStatus(loadState = append, onRetry = retry)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppendStatus(loadState: LoadState, onRetry: () -> Unit) {
    val density = LocalDensity.current
    var errorHeightPx by remember(density) { mutableIntStateOf(0) }
    when (loadState) {
        is LoadState.Error -> ScreenMessage(
            modifier = Modifier.onSizeChanged { errorHeightPx = it.height },
            title = stringResource(R.string.search_error_title),
            message = (loadState.error as? SearchPagingException)?.error?.asUiText()?.asString(),
            onRetry = onRetry,
        )
        // Shrinking the last item at the scroll boundary makes LazyColumn clamp its position.
        // Retain the measured error height through retries, including wrapped/large text.
        is LoadState.Loading -> LoadingResults(
            modifier = Modifier.heightIn(min = with(density) { errorHeightPx.toDp() }),
        )
        is LoadState.NotLoading -> Unit
    }
}

@Composable
private fun ResultsHeading(totalResults: Int, isLoading: Boolean) {
    Column {
        Text(
            modifier = Modifier.padding(vertical = Spacing.small),
            text = pluralStringResource(R.plurals.search_results, totalResults, totalResults),
            style = MaterialTheme.typography.titleMedium,
        )
        if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun EmptySearch(modifier: Modifier = Modifier, compact: Boolean) {
    Column(
        modifier = modifier.fillMaxWidth().padding(
            horizontal = if (compact) 0.dp else Spacing.small,
            vertical = if (compact) 0.dp else Spacing.large,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier.width(if (compact) CompactEmptyFilmWidth else EmptyFilmWidth)
                .aspectRatio(EmptyFilmAspectRatio),
            painter = painterResource(R.drawable.illustration_empty_search),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.height(if (compact) Spacing.small else Spacing.large))
        Text(
            text = stringResource(R.string.search_start_title),
            style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(if (compact) Spacing.extraSmall else Spacing.compact))
        Text(
            text = stringResource(R.string.search_start_message),
            style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LoadingResults(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(Spacing.large), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

private val EmptyFilmWidth = 176.dp
private val CompactEmptyFilmWidth = 72.dp
private const val EmptyFilmAspectRatio = 180f / 120f
