package com.jacks.moviet.search.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.jacks.moviet.R
import com.jacks.moviet.core.designsystem.theme.Spacing
import com.jacks.moviet.search.presentation.SearchAction

@Composable
internal fun SearchHeader(query: String, compact: Boolean, onAction: (SearchAction) -> Unit) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        verticalArrangement = Arrangement.spacedBy(if (compact) Spacing.small else Spacing.medium),
    ) {
        Text(text = stringResource(R.string.app_name), style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineLarge)
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().testTag("search_query"),
            value = query,
            onValueChange = { onAction(SearchAction.QueryChanged(it)) },
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = { Icon(painterResource(R.drawable.ic_search), contentDescription = null) },
            trailingIcon = if (query.isNotEmpty()) {
                { IconButton(onClick = { onAction(SearchAction.ClearQuery) }) {
                    Icon(painterResource(R.drawable.ic_close), contentDescription = stringResource(R.string.search_clear))
                } }
            } else null,
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        )
    }
}
