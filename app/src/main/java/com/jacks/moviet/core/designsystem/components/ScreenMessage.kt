package com.jacks.moviet.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.jacks.moviet.R
import com.jacks.moviet.core.designsystem.theme.Spacing

@Composable
fun ScreenMessage(
    modifier: Modifier = Modifier,
    title: String,
    message: String? = null,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(Spacing.large),
        verticalArrangement = Arrangement.spacedBy(Spacing.small, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        message?.let {
            Text(text = it, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
        onRetry?.let { retry ->
            TextButton(onClick = retry) { Text(stringResource(R.string.retry)) }
        }
    }
}
