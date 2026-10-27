package com.jacks.moviet.details.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jacks.moviet.R
import com.jacks.moviet.core.designsystem.theme.Spacing

@Composable
internal fun MoviePlot(
    modifier: Modifier = Modifier,
    imdbId: String,
    plot: String,
) {
    var expanded by rememberSaveable(imdbId) { mutableStateOf(false) }
    var hasOverflow by remember(imdbId) { mutableStateOf(false) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        Text(stringResource(R.string.movie_plot), style = MaterialTheme.typography.titleMedium)
        Text(
            text = plot,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = if (expanded) Int.MAX_VALUE else CollapsedPlotLines,
            onTextLayout = { if (!expanded) hasOverflow = it.hasVisualOverflow },
        )
        if (hasOverflow || expanded) {
            TextButton(onClick = { expanded = !expanded }) {
                Text(stringResource(if (expanded) R.string.read_less else R.string.read_more))
            }
        }
    }
}

private const val CollapsedPlotLines = 4
