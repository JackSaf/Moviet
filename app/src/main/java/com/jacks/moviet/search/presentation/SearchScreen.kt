package com.jacks.moviet.search.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jacks.moviet.R
import com.jacks.moviet.core.designsystem.theme.MovietTheme
import com.jacks.moviet.core.designsystem.theme.Spacing
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchScreenRoot(
    modifier: Modifier = Modifier,
    onFavoritesClick: () -> Unit,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SearchScreen(modifier = modifier, state = state, onFavoritesClick = onFavoritesClick)
}

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    state: SearchState,
    onFavoritesClick: () -> Unit,
) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(Spacing.large),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge)
            Text(text = state.welcome.asString())
            Button(onClick = onFavoritesClick) {
                Text(text = stringResource(R.string.favorites_title))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview() {
    MovietTheme {
        SearchScreen(state = SearchState(), onFavoritesClick = {})
    }
}
