package com.jacks.moviet.search.presentation.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.jacks.moviet.R
import com.jacks.moviet.core.designsystem.components.ScreenMessage
import com.jacks.moviet.core.designsystem.theme.Spacing
import com.jacks.moviet.core.presentation.util.asUiText
import com.jacks.moviet.search.domain.MovieSearchResult
import com.jacks.moviet.search.domain.SearchPagingException
import com.jacks.moviet.search.presentation.SearchAction
import com.jacks.moviet.search.presentation.SearchContent
import com.jacks.moviet.search.presentation.SearchState
import com.jacks.moviet.search.presentation.contentFor

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
    animatedVisibilityScope: AnimatedVisibilityScope?,
    onAction: (SearchAction) -> Unit,
) {
    val direction = LocalLayoutDirection.current
    val listState = rememberLazyListState()
    LazyColumn(
        modifier = modifier.fillMaxSize().consumeWindowInsets(contentPadding).testTag("search_results"),
        state = listState,
        contentPadding = PaddingValues(
            start = contentPadding.calculateStartPadding(direction) + Spacing.medium,
            end = contentPadding.calculateEndPadding(direction) + Spacing.medium,
            top = contentPadding.calculateTopPadding() + Spacing.small,
            bottom = contentPadding.calculateBottomPadding() + Spacing.medium,
        ),
    ) {
        when (val content = state.contentFor(movies.itemCount, movies.loadState.refresh)) {
            SearchContent.Start -> item(key = "start") {
                ScreenMessage(
                    modifier = Modifier.fillParentMaxSize(),
                    title = stringResource(R.string.search_start_title),
                    message = stringResource(R.string.search_start_message),
                )
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
                    onRetry = movies::retry,
                )
            }
            is SearchContent.Results -> {
                // Read presented metadata without triggering Paging prefetch from the heading.
                val totalResults = movies.itemSnapshotList.items.first().totalResults
                item(key = "results_heading") {
                    Column {
                        Text(
                            modifier = Modifier.padding(vertical = Spacing.small),
                            text = pluralStringResource(R.plurals.search_results, totalResults, totalResults),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        if (content.isLoading) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
                items(count = movies.itemCount, key = movies.itemKey { it.movie.imdbId }, contentType = { "movie" }) { index ->
                    movies[index]?.movie?.let { movie ->
                        MovieResultRow(
                            movie = movie,
                            selected = selectedMovieId == movie.imdbId,
                            compact = compactLayout,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                            onClick = { onAction(SearchAction.MovieClick(movie)) },
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
                when (val append = movies.loadState.append) {
                    is LoadState.Loading -> item(key = "append_loading") { LoadingResults() }
                    is LoadState.Error -> item(key = "append_error") {
                        ScreenMessage(
                            title = stringResource(R.string.search_error_title),
                            message = (append.error as? SearchPagingException)?.error?.asUiText()?.asString(),
                            onRetry = movies::retry,
                        )
                    }
                    is LoadState.NotLoading -> Unit
                }
            }
        }
    }
}

@Composable
private fun LoadingResults(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(Spacing.large), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
