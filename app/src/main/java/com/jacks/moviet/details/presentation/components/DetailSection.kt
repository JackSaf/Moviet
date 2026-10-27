package com.jacks.moviet.details.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jacks.moviet.core.designsystem.theme.Spacing

@Composable
internal fun DetailSection(modifier: Modifier = Modifier, title: String, text: String) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
