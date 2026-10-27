package com.jacks.moviet.favorites.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jacks.moviet.R
import com.jacks.moviet.core.designsystem.theme.Spacing

@Composable
internal fun FavoritesHeader(compact: Boolean) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        text = stringResource(R.string.favorites_title),
        style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineLarge,
    )
}
