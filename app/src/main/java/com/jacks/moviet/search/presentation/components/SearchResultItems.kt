package com.jacks.moviet.search.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.jacks.moviet.R
import com.jacks.moviet.core.designsystem.components.ScreenMessage
import com.jacks.moviet.core.designsystem.theme.Spacing
import com.jacks.moviet.core.domain.model.Movie
import com.jacks.moviet.core.presentation.util.asUiText
import com.jacks.moviet.search.domain.MovieSearchResult
import com.jacks.moviet.search.domain.SearchPagingException
import com.jacks.moviet.search.presentation.SearchContent
import com.jacks.moviet.search.presentation.SearchState
import com.jacks.moviet.search.presentation.contentFor
import com.jacks.moviet.sdk.AdsService
import com.jacks.moviet.sdk.NativeAdSlot

internal fun LazyListScope.searchResultItems(
    state: SearchState,
    movies: LazyPagingItems<MovieSearchResult>,
    compact: Boolean,
    ads: AdsService?,
    onRetry: () -> Unit,
    movieContent: @Composable (Movie) -> Unit,
) {
    // Decide inside the lazy builder from its current Paging snapshot, including after a clear.
    when (val content = state.contentFor(movies.itemSnapshotList.items.firstOrNull()?.query, movies.loadState.refresh)) {
        SearchContent.Start -> item(key = "start") { EmptySearch(compact = compact) }
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
                onRetry = onRetry,
            )
        }
        is SearchContent.Results -> {
            val totalResults = movies.itemSnapshotList.items.first().totalResults
            item(key = "results_heading") {
                ResultsHeading(totalResults = totalResults, isLoading = content.isLoading)
            }
            movieItems(
                movies = movies,
                totalResults = totalResults,
                compact = compact,
                ads = ads,
                movieContent = movieContent,
            )
            val append = movies.loadState.append
            if (append is LoadState.Loading || append is LoadState.Error) {
                item(key = "append_status") { AppendStatus(loadState = append, onRetry = onRetry) }
            }
        }
    }
}

private fun LazyListScope.movieItems(
    movies: LazyPagingItems<MovieSearchResult>,
    totalResults: Int,
    compact: Boolean,
    ads: AdsService?,
    movieContent: @Composable (Movie) -> Unit,
) {
    val movieKey = movies.itemKey { "movie_${it.movie.imdbId}" }
    val movieRow: @Composable (Int) -> Unit = { index -> movies[index]?.movie?.let { movieContent(it) } }
    val showAds = ads != null && totalResults >= MIN_MOVIES_FOR_ADS && movies.itemCount >= MIN_MOVIES_FOR_ADS
    if (showAds) {
        item(key = movieKey(0), contentType = "movie") { movieRow(0) }
        item(key = "native_ad_1", contentType = "ad") {
            ads.NativeAd(modifier = Modifier.fillMaxWidth(), slot = NativeAdSlot.FIRST, compact = compact)
        }
        item(key = movieKey(1), contentType = "movie") { movieRow(1) }
        item(key = "native_ad_3", contentType = "ad") {
            ads.NativeAd(modifier = Modifier.fillMaxWidth(), slot = NativeAdSlot.SECOND, compact = compact)
        }
    }
    val remainingStart = if (showAds) 2 else 0
    items(
        count = movies.itemCount - remainingStart,
        key = { offset -> movieKey(offset + remainingStart) },
        contentType = { "movie" },
    ) { offset -> movieRow(offset + remainingStart) }
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
private fun LoadingResults(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(Spacing.large), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

private const val MIN_MOVIES_FOR_ADS = 3
