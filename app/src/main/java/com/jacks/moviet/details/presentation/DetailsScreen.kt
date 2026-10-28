package com.jacks.moviet.details.presentation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jacks.moviet.R
import com.jacks.moviet.core.designsystem.components.ScreenMessage
import com.jacks.moviet.core.designsystem.theme.Spacing
import com.jacks.moviet.core.domain.model.Movie
import com.jacks.moviet.core.domain.model.MovieType
import com.jacks.moviet.core.presentation.util.asUiText
import com.jacks.moviet.details.presentation.components.DetailSection
import com.jacks.moviet.details.presentation.components.DetailsHeader
import com.jacks.moviet.details.presentation.components.DetailsRatings
import com.jacks.moviet.details.presentation.components.FavoriteMovieButton
import com.jacks.moviet.details.presentation.components.MoviePlot
import com.jacks.moviet.details.presentation.components.detailsContentReveal
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DetailsScreenRoot(
    modifier: Modifier = Modifier,
    imdbId: String,
    preview: Movie? = null,
    showBackButton: Boolean = true,
    compactHeader: Boolean = false,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    onBackClick: () -> Unit,
    viewModel: DetailsViewModel = koinViewModel(),
) {
    LaunchedEffect(imdbId, viewModel) { viewModel.onAction(DetailsAction.LoadMovie(imdbId)) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    DetailsScreen(
        modifier = modifier,
        state = state,
        preview = preview,
        showBackButton = showBackButton,
        compactHeader = compactHeader,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
        onBackClick = onBackClick,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    modifier: Modifier = Modifier,
    state: DetailsState,
    preview: Movie? = null,
    showBackButton: Boolean = true,
    compactHeader: Boolean = false,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    onBackClick: () -> Unit,
    onAction: (DetailsAction) -> Unit,
) {
    val movie = state.details?.movie ?: preview
    val contentRevealModifier = Modifier.detailsContentReveal(
        imdbId = movie?.imdbId,
        isLoaded = state.details != null,
    )
    Scaffold(
        modifier = modifier.testTag("details_screen"),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.details_title)) },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onBackClick) {
                            Icon(painterResource(R.drawable.ic_back), contentDescription = stringResource(R.string.navigate_back))
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        val direction = LocalLayoutDirection.current
        LazyColumn(
            modifier = Modifier.fillMaxSize().consumeWindowInsets(innerPadding),
            contentPadding = PaddingValues(
                start = innerPadding.calculateStartPadding(direction) + Spacing.large,
                end = innerPadding.calculateEndPadding(direction) + Spacing.large,
                top = innerPadding.calculateTopPadding() + Spacing.medium,
                bottom = innerPadding.calculateBottomPadding() + Spacing.large,
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            if (movie != null) {
                item(key = "identity") {
                    DetailsHeader(
                        movie = movie,
                        compactHeader = compactHeader,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                }
            }
            if (state.isLoading) {
                item(key = "loading") {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(Spacing.large),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }
                }
            }
            state.error?.let { error ->
                item(key = "error") {
                    ScreenMessage(
                        title = stringResource(R.string.details_error_title),
                        message = error.asUiText().asString(),
                        onRetry = { onAction(DetailsAction.Retry) },
                    )
                }
            }
            state.details?.let { details ->
                if (details.imdbRating != null || details.contentRating != null) {
                    item(key = "ratings") {
                        DetailsRatings(
                            modifier = contentRevealModifier,
                            imdbRating = details.imdbRating,
                            contentRating = details.contentRating,
                        )
                    }
                }
                item(key = "favorite") {
                    FavoriteMovieButton(
                        modifier = contentRevealModifier,
                        isFavorite = state.isFavorite,
                        isSaving = state.isSavingFavorite,
                        hasError = state.favoriteError,
                        enabled = state.isFavoriteLoaded,
                        onClick = { onAction(DetailsAction.ToggleFavorite) },
                    )
                }
                if (state.favoriteLoadError) {
                    item(key = "favorite_read_error") {
                        ScreenMessage(
                            title = stringResource(R.string.favorites_load_error),
                            message = stringResource(R.string.favorites_load_error_message),
                            onRetry = { onAction(DetailsAction.RetryFavorite) },
                        )
                    }
                }
                details.genres?.let { item(key = "genres") { DetailSection(modifier = contentRevealModifier, title = stringResource(R.string.movie_genres), text = it) } }
                details.plot?.let { plot ->
                    item(key = "plot") {
                        MoviePlot(modifier = contentRevealModifier, imdbId = details.movie.imdbId, plot = plot)
                    }
                }
                details.runtime?.let { runtime ->
                    item(key = "runtime") {
                        DetailSection(
                            modifier = contentRevealModifier,
                            title = stringResource(if (details.movie.type == MovieType.SERIES) R.string.movie_series_runtime else R.string.movie_runtime),
                            text = runtime,
                        )
                    }
                }
                details.director?.let { item(key = "director") { DetailSection(modifier = contentRevealModifier, title = stringResource(R.string.movie_director), text = it) } }
                details.actors?.let { item(key = "actors") { DetailSection(modifier = contentRevealModifier, title = stringResource(R.string.movie_actors), text = it) } }
                details.seasons?.let { item(key = "seasons") { DetailSection(modifier = contentRevealModifier, title = stringResource(R.string.movie_seasons), text = it) } }
            }
        }
    }
}
