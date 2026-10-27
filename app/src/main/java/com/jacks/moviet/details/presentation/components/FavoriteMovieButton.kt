package com.jacks.moviet.details.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.jacks.moviet.R
import com.jacks.moviet.core.designsystem.theme.Spacing

@Composable
internal fun FavoriteMovieButton(
    modifier: Modifier = Modifier,
    isFavorite: Boolean,
    isSaving: Boolean,
    hasError: Boolean,
    onClick: () -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        Button(
            modifier = Modifier.fillMaxWidth().testTag("toggle_favorite"),
            onClick = onClick,
            enabled = !isSaving,
            shape = MaterialTheme.shapes.large,
            colors = if (isFavorite) ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) else ButtonDefaults.buttonColors(),
            contentPadding = PaddingValues(Spacing.medium),
        ) {
            Icon(
                painterResource(if (isFavorite) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark),
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(Spacing.small))
            Text(stringResource(if (isFavorite) R.string.remove_ticket else R.string.save_ticket))
        }
        if (hasError) {
            Text(stringResource(R.string.favorite_update_failed), color = MaterialTheme.colorScheme.error)
        }
    }
}
