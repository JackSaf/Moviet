package com.jacks.moviet.core.designsystem.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.jacks.moviet.R

@Composable
fun MovieNavigation(
    modifier: Modifier = Modifier,
    favoritesSelected: Boolean,
    onSearchClick: () -> Unit,
    onFavoritesClick: () -> Unit,
) {
    val colors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    )
    NavigationBar(modifier = modifier, containerColor = MaterialTheme.colorScheme.surface) {
        NavigationBarItem(
            selected = !favoritesSelected,
            onClick = { if (favoritesSelected) onSearchClick() },
            icon = { Icon(painterResource(R.drawable.ic_search), contentDescription = null) },
            label = { Text(stringResource(R.string.search_title)) },
            colors = colors,
        )
        NavigationBarItem(
            selected = favoritesSelected,
            onClick = { if (!favoritesSelected) onFavoritesClick() },
            icon = { Icon(painterResource(if (favoritesSelected) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark), contentDescription = null) },
            label = { Text(stringResource(R.string.favorites_title)) },
            colors = colors,
        )
    }
}
