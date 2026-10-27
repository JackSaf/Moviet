package com.jacks.moviet.core.designsystem.theme

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jacks.moviet.R

private val PreviewBorderWidth = 1.dp

@Preview(name = "Dark system", widthDp = 390, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Light system", widthDp = 390, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Narrow window", widthDp = 320)
@Preview(name = "Large text", widthDp = 320, fontScale = 1.5f)
@Composable
private fun MovietThemePreview() {
    MovietTheme {
        Surface {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.large),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium),
            ) {
                Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge)
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = "",
                    onValueChange = {},
                    placeholder = { Text(text = stringResource(R.string.theme_preview_search)) },
                    shape = MaterialTheme.shapes.large,
                    singleLine = true,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    FilterChip(
                        selected = true,
                        onClick = {},
                        label = { Text(text = stringResource(R.string.theme_preview_all)) },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                    FilterChip(
                        selected = false,
                        onClick = {},
                        label = { Text(text = stringResource(R.string.theme_preview_genre)) },
                        shape = CircleShape,
                    )
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(PreviewBorderWidth, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(Spacing.small),
                    ) {
                        Text(
                            text = stringResource(R.string.theme_preview_movie),
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Text(
                            text = stringResource(R.string.theme_preview_metadata),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            text = stringResource(R.string.theme_preview_rating),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(R.string.theme_preview_synopsis),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                Button(modifier = Modifier.fillMaxWidth(), onClick = {}) {
                    Text(text = stringResource(R.string.theme_preview_save))
                }
                TextButton(onClick = {}) {
                    Text(text = stringResource(R.string.favorites_title))
                }
                Text(
                    text = stringResource(R.string.theme_preview_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
