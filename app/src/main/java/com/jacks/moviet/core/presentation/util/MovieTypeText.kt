package com.jacks.moviet.core.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.jacks.moviet.R
import com.jacks.moviet.core.domain.model.MovieType

@Composable
fun MovieType.displayName(): String? = when (this) {
    MovieType.MOVIE -> stringResource(R.string.movie_type_movie)
    MovieType.SERIES -> stringResource(R.string.movie_type_series)
    MovieType.EPISODE -> stringResource(R.string.movie_type_episode)
    MovieType.UNKNOWN -> null
}
