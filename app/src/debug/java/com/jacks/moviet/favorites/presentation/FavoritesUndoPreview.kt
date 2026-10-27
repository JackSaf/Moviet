package com.jacks.moviet.favorites.presentation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jacks.moviet.R
import com.jacks.moviet.core.designsystem.components.MovieNavigation
import com.jacks.moviet.core.designsystem.theme.MovietTheme
import com.jacks.moviet.core.domain.model.Movie
import com.jacks.moviet.core.domain.model.MovieType
import com.jacks.moviet.details.domain.MovieDetails
import com.jacks.moviet.details.presentation.DetailsScreen
import com.jacks.moviet.details.presentation.DetailsState
import com.jacks.moviet.favorites.presentation.components.FavoritesSnackbar

@Preview(name = "Undo phone", widthDp = 360, heightDp = 800)
@Preview(name = "Undo large text", widthDp = 320, heightDp = 640, fontScale = 1.5f)
@Composable
private fun FavoritesUndoPhonePreview() {
    MovietTheme { FavoritesUndoPane() }
}

@Preview(name = "Undo compact list-detail", widthDp = 600, heightDp = 400)
@Preview(name = "Undo compact large text", widthDp = 600, heightDp = 400, fontScale = 1.5f)
@Composable
private fun FavoritesUndoListDetailPreview() {
    MovietTheme {
        Row {
            FavoritesUndoPane(modifier = Modifier.weight(0.4f), compact = true)
            VerticalDivider()
            DetailsScreen(
                modifier = Modifier.weight(0.6f),
                state = DetailsState(isLoading = false, details = MovieDetails(movie = PreviewMovie)),
                compactHeader = true,
                onBackClick = {},
                onAction = {},
            )
        }
    }
}

@Preview(name = "Restore retry compact", widthDp = 240, heightDp = 400)
@Composable
private fun FavoritesUndoErrorPreview() {
    MovietTheme { FavoritesUndoPane(compact = true, hasError = true) }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun FavoritesUndoPane(modifier: Modifier = Modifier, compact: Boolean = false, hasError: Boolean = false) {
    val message = stringResource(if (hasError) R.string.favorites_restore_failed else R.string.favorites_removed)
    val label = stringResource(if (hasError) R.string.retry else R.string.favorites_undo)
    // Static data keeps the snackbar visible in the preview without a timer or coroutine.
    val data = object : SnackbarData {
        override val visuals = object : SnackbarVisuals {
            override val message = message
            override val actionLabel = label
            override val withDismissAction = true
            override val duration = SnackbarDuration.Long
        }
        override fun performAction() = Unit
        override fun dismiss() = Unit
    }
    Scaffold(
        modifier = modifier.fillMaxHeight(),
        contentWindowInsets = WindowInsets(0),
        bottomBar = { MovieNavigation(favoritesSelected = true, onSearchClick = {}, onFavoritesClick = {}) },
        snackbarHost = { FavoritesSnackbar(data = data, compactLayout = compact) },
    ) { padding ->
        FavoritesScreen(
            state = FavoritesState(isLoading = false, movies = listOf(PreviewMovie)),
            compactLayout = compact,
            selectedMovieId = PreviewMovie.imdbId,
            navigationBarHeight = padding.calculateBottomPadding(),
            onAction = {},
        )
    }
}

private val PreviewMovie = Movie("tt18689424", "Batman v Superman: Dawn of Justice (Ultimate Edition)", null, MovieType.MOVIE)
