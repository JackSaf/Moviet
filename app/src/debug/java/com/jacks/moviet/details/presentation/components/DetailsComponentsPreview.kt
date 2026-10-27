package com.jacks.moviet.details.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jacks.moviet.core.designsystem.theme.MovietTheme
import com.jacks.moviet.core.designsystem.theme.Spacing

@Preview(name = "Details interactions", widthDp = 360)
@Composable
private fun DetailsComponentsPreview() {
    var favorite by remember { mutableStateOf(false) }
    MovietTheme {
        Surface {
            Column(
                modifier = Modifier.padding(Spacing.large),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium),
            ) {
                FavoriteMovieButton(
                    isFavorite = favorite,
                    isSaving = false,
                    hasError = false,
                    onClick = { favorite = !favorite },
                )
                MoviePlot(
                    imdbId = "preview",
                    plot = "A cinema projectionist discovers a forgotten film in the archive. " +
                        "As she follows its clues through the city, she meets the people who made it " +
                        "and learns why its final scene was never shown. With the old cinema about " +
                        "to close, they come together for one last screening and a chance to finish " +
                        "the story they started many years ago.",
                )
                DetailSection(title = "Runtime", text = "120 min")
            }
        }
    }
}

@Preview(name = "Details large text", widthDp = 320, fontScale = 1.5f)
@Composable
private fun DetailsLargeTextPreview() = DetailsComponentsPreview()

@Preview(name = "Details wide", widthDp = 600)
@Composable
private fun DetailsWidePreview() = DetailsComponentsPreview()
