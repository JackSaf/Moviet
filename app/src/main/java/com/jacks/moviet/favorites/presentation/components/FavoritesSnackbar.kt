package com.jacks.moviet.favorites.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import com.jacks.moviet.R
import com.jacks.moviet.favorites.presentation.FavoritesAction
import com.jacks.moviet.favorites.presentation.UndoRemoval
import com.jacks.moviet.favorites.presentation.UndoStatus

@Composable
internal fun FavoritesSnackbarHost(
    modifier: Modifier = Modifier,
    undoRemoval: UndoRemoval?,
    compactLayout: Boolean,
    onAction: (FavoritesAction) -> Unit,
) {
    val hostState = remember { SnackbarHostState() }
    val currentOnAction = rememberUpdatedState(onAction)
    val message = stringResource(
        if (undoRemoval?.status == UndoStatus.FAILED) R.string.favorites_restore_failed else R.string.favorites_removed,
    )
    val actionLabel = stringResource(if (undoRemoval?.status == UndoStatus.FAILED) R.string.retry else R.string.favorites_undo)
    // Replacing the state cancels the previous snackbar instead of queuing stale Undo actions.
    LaunchedEffect(undoRemoval, message, actionLabel) {
        if (undoRemoval == null || undoRemoval.status == UndoStatus.RESTORING) return@LaunchedEffect
        val result = hostState.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            withDismissAction = true,
            duration = if (undoRemoval.status == UndoStatus.FAILED) SnackbarDuration.Indefinite else SnackbarDuration.Long,
        )
        currentOnAction.value(
            if (result == SnackbarResult.ActionPerformed) FavoritesAction.UndoRemove(undoRemoval.id)
            else FavoritesAction.DismissUndo(undoRemoval.id),
        )
    }
    SnackbarHost(modifier = modifier, hostState = hostState) { data ->
        FavoritesSnackbar(data = data, compactLayout = compactLayout)
    }
}

@Composable
internal fun FavoritesSnackbar(
    modifier: Modifier = Modifier,
    data: SnackbarData,
    compactLayout: Boolean,
) {
    Snackbar(
        modifier = modifier,
        snackbarData = data,
        actionOnNewLine = compactLayout || LocalDensity.current.fontScale >= LargeTextFontScale,
        shape = MaterialTheme.shapes.medium,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        actionColor = MaterialTheme.colorScheme.primary,
        dismissActionContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private const val LargeTextFontScale = 1.3f
