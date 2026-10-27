package com.jacks.moviet.details.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.jacks.moviet.core.designsystem.theme.MovietTheme
import com.jacks.moviet.core.domain.model.Movie
import com.jacks.moviet.core.domain.model.MovieType
import com.jacks.moviet.details.domain.MovieDetails

@Preview(name = "Details centered header", widthDp = 360, heightDp = 800)
@Composable
private fun DetailsScreenPreview() = PreviewDetails(title = "Interstellar")

@Preview(name = "Details long title large text", widthDp = 320, heightDp = 800, fontScale = 1.5f)
@Composable
private fun DetailsLongTitlePreview() = PreviewDetails(title = "The Lord of the Rings: The Fellowship of the Ring")

@Preview(name = "Details compact header", widthDp = 480, heightDp = 600)
@Composable
private fun DetailsCompactHeaderPreview() = PreviewDetails(title = "Interstellar", compactHeader = true)

@Composable
private fun PreviewDetails(title: String, compactHeader: Boolean = false) {
    MovietTheme {
        DetailsScreen(
            state = DetailsState(
                isLoading = false,
                details = MovieDetails(
                    movie = Movie(
                        imdbId = "preview",
                        title = title,
                        posterUrl = null,
                        type = MovieType.MOVIE,
                        year = "2014",
                    ),
                    contentRating = "PG-13",
                    imdbRating = "8.7",
                    genres = "Adventure, Drama, Sci-Fi",
                    plot = "A team of explorers travel beyond this galaxy to discover whether mankind has a future among the stars.",
                    runtime = "169 min",
                ),
            ),
            compactHeader = compactHeader,
            onBackClick = {},
            onAction = {},
        )
    }
}
