package com.jacks.moviet.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/** The supplied cinema design uses a dark palette, independently of the device theme. */
@Composable
fun MovietTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = MovietDarkColorScheme,
        typography = MovietTypography,
        shapes = MovietShapes,
        content = content,
    )
}
