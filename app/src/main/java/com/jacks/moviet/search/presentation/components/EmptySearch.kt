package com.jacks.moviet.search.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jacks.moviet.R
import com.jacks.moviet.core.designsystem.theme.Spacing

@Composable
internal fun EmptySearch(modifier: Modifier = Modifier, compact: Boolean) {
    Column(
        modifier = modifier.fillMaxWidth().padding(
            horizontal = if (compact) 0.dp else Spacing.small,
            vertical = if (compact) 0.dp else Spacing.large,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier.width(if (compact) CompactEmptyFilmWidth else EmptyFilmWidth)
                .aspectRatio(EmptyFilmAspectRatio),
            painter = painterResource(R.drawable.illustration_empty_search),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.height(if (compact) Spacing.small else Spacing.large))
        Text(
            text = stringResource(R.string.search_start_title),
            style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(if (compact) Spacing.extraSmall else Spacing.compact))
        Text(
            text = stringResource(R.string.search_start_message),
            style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private val EmptyFilmWidth = 176.dp
private val CompactEmptyFilmWidth = 72.dp
private const val EmptyFilmAspectRatio = 180f / 120f
