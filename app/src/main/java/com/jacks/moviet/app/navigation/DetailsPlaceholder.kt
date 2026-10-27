package com.jacks.moviet.app.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jacks.moviet.R
import com.jacks.moviet.core.designsystem.components.ScreenMessage

@Composable
internal fun DetailsPlaceholder() {
    Surface(modifier = Modifier.fillMaxSize()) {
        ScreenMessage(
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
            title = stringResource(R.string.details_choose_title),
            message = stringResource(R.string.details_choose_message),
        )
    }
}
