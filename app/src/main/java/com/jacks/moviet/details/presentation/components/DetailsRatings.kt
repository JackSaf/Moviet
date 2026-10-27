package com.jacks.moviet.details.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.jacks.moviet.R
import com.jacks.moviet.core.designsystem.theme.Spacing

@Composable
internal fun DetailsRatings(
    modifier: Modifier = Modifier,
    imdbRating: String?,
    contentRating: String?,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.compact),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        imdbRating?.let { rating ->
            Text(
                text = stringResource(R.string.movie_imdb_rating, rating),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        contentRating?.let { rating ->
            val description = stringResource(R.string.movie_content_rating, rating)
            Surface(
                modifier = Modifier.clearAndSetSemantics { contentDescription = description },
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                border = BorderStroke(ContentRatingBorderWidth, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = Spacing.small, vertical = Spacing.extraSmall),
                    text = rating,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

private val ContentRatingBorderWidth = 1.dp
