package com.jacks.moviet.favorites.presentation.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jacks.moviet.R
import com.jacks.moviet.core.designsystem.components.ScreenMessage
import com.jacks.moviet.core.designsystem.theme.MovietMotion
import com.jacks.moviet.core.designsystem.theme.Spacing
import com.jacks.moviet.favorites.presentation.FavoritesAction
import com.jacks.moviet.favorites.presentation.FavoritesState

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun FavoriteTickets(
    modifier: Modifier = Modifier,
    state: FavoritesState,
    contentPadding: PaddingValues,
    selectedMovieId: String?,
    compactLayout: Boolean,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    onAction: (FavoritesAction) -> Unit,
) {
    val direction = LocalLayoutDirection.current
    val listState = rememberLazyListState()
    LazyColumn(
        modifier = modifier.fillMaxSize().consumeWindowInsets(contentPadding).testTag("favorite_tickets"),
        state = listState,
        contentPadding = PaddingValues(
            start = contentPadding.calculateStartPadding(direction) + Spacing.medium,
            end = contentPadding.calculateEndPadding(direction) + Spacing.medium,
            top = contentPadding.calculateTopPadding() + Spacing.medium,
            bottom = contentPadding.calculateBottomPadding() + Spacing.medium,
        ),
        verticalArrangement = if (!state.isLoading && !state.hasLoadError && state.movies.isEmpty()) {
            Arrangement.Center
        } else {
            Arrangement.spacedBy(Spacing.medium)
        },
    ) {
        when {
            // The first local read needs neither a spinner nor a premature empty state.
            state.isLoading -> Unit
            state.hasLoadError -> item(key = "error") {
                ScreenMessage(
                    modifier = Modifier.fillParentMaxSize(),
                    title = stringResource(R.string.favorites_load_error),
                    message = stringResource(R.string.favorites_load_error_message),
                    onRetry = { onAction(FavoritesAction.Retry) },
                )
            }
            state.movies.isEmpty() -> item(key = "empty") {
                EmptyFavorites(
                    compact = compactLayout,
                    onSearchClick = { onAction(FavoritesAction.SearchClick) },
                )
            }
            else -> items(state.movies, key = { it.imdbId }, contentType = { "ticket" }) { movie ->
                Column(
                    modifier = Modifier.animateItem(
                        fadeInSpec = MovietMotion.defaultEffects(),
                        placementSpec = MovietMotion.defaultSpatial(),
                        fadeOutSpec = MovietMotion.defaultEffects(),
                    ),
                ) {
                    FavoriteTicket(
                        movie = movie,
                        selected = movie.imdbId == selectedMovieId,
                        compact = compactLayout,
                        removing = movie.imdbId in state.removingIds,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        onClick = { onAction(FavoritesAction.MovieClick(movie)) },
                        onRemove = { onAction(FavoritesAction.RemoveMovie(movie.imdbId)) },
                    )
                    if (movie.imdbId in state.removalErrors) {
                        ScreenMessage(
                            modifier = Modifier.padding(top = Spacing.small),
                            title = stringResource(R.string.favorite_update_failed),
                            onRetry = { onAction(FavoritesAction.RemoveMovie(movie.imdbId)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFavorites(modifier: Modifier = Modifier, compact: Boolean, onSearchClick: () -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth().padding(
            horizontal = if (compact) 0.dp else Spacing.small,
            vertical = if (compact) 0.dp else Spacing.large,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier.width(if (compact) CompactEmptyTicketsWidth else EmptyTicketsWidth)
                .aspectRatio(EmptyTicketsAspectRatio),
            painter = painterResource(R.drawable.illustration_empty_tickets),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.height(if (compact) Spacing.small else Spacing.large))
        Text(
            modifier = Modifier.widthIn(max = EmptyTitleMaxWidth),
            text = stringResource(R.string.favorites_empty_title),
            style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(if (compact) Spacing.extraSmall else Spacing.compact))
        Text(
            text = stringResource(R.string.favorites_empty),
            style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(if (compact) Spacing.compact else Spacing.large))
        Button(
            modifier = Modifier.widthIn(min = EmptyButtonMinWidth).heightIn(min = EmptyButtonMinHeight),
            onClick = onSearchClick,
        ) {
            Text(text = stringResource(R.string.favorites_find_movie), textAlign = TextAlign.Center)
        }
    }
}

private val EmptyTicketsWidth = 176.dp
private val CompactEmptyTicketsWidth = 72.dp
private const val EmptyTicketsAspectRatio = 180f / 120f
private val EmptyTitleMaxWidth = 240.dp
private val EmptyButtonMinWidth = 160.dp
private val EmptyButtonMinHeight = 48.dp
