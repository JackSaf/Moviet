package com.jacks.moviet.app.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jacks.moviet.core.designsystem.components.MovieNavigation

/** Keeps navigation outside destination transforms while each screen owns its system insets. */
@Composable
internal fun NavigationScaffold(
    modifier: Modifier = Modifier,
    useTwoPanes: Boolean,
    showNavigation: Boolean,
    favoritesSelected: Boolean,
    onSearchClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    content: @Composable (navigationBarHeight: Dp) -> Unit,
) {
    val density = LocalDensity.current
    val keyboardVisible = WindowInsets.ime.getBottom(density) > 0
    var navigationBarHeightPx by remember { mutableIntStateOf(0) }
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showNavigation && !keyboardVisible) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    MovieNavigation(
                        modifier = Modifier
                            .weight(if (useTwoPanes) ListPaneWeight else 1f)
                            .onSizeChanged { navigationBarHeightPx = it.height },
                        favoritesSelected = favoritesSelected,
                        onSearchClick = onSearchClick,
                        onFavoritesClick = onFavoritesClick,
                    )
                    if (useTwoPanes) {
                        Spacer(modifier = Modifier.width(ListDetailDividerWidth))
                        Spacer(modifier = Modifier.weight(DetailPaneWeight))
                    }
                }
            }
        },
    ) { padding ->
        // Retain the list's space while it exits to phone details, avoiding a layout jump.
        // Do not pad NavDisplay: the wide details pane must keep its full viewport height.
        val navigationBarHeight = when {
            keyboardVisible -> 0.dp
            showNavigation -> padding.calculateBottomPadding()
            else -> with(density) { navigationBarHeightPx.toDp() }
        }
        content(navigationBarHeight)
    }
}
