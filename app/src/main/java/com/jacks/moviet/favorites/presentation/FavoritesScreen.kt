package com.jacks.moviet.favorites.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jacks.moviet.R
import com.jacks.moviet.core.designsystem.theme.Spacing

@Composable
fun FavoritesScreen(modifier: Modifier = Modifier, onBackClick: () -> Unit) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(Spacing.large),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = stringResource(R.string.favorites_title), style = MaterialTheme.typography.headlineLarge)
            Text(text = stringResource(R.string.favorites_empty))
            TextButton(onClick = onBackClick) {
                Text(text = stringResource(R.string.navigate_back))
            }
        }
    }
}
