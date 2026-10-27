package com.jacks.moviet.core.designsystem.components

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.compositionLocalOf
import com.jacks.moviet.core.designsystem.theme.MovietMotion

// Lists and their details share a namespace; separate tabs must never match each other.
internal val LocalMovieSharedContentNamespace = compositionLocalOf<Any> { Unit }

// Keep the poster, title and metadata moving together between list and details layouts.
@OptIn(ExperimentalSharedTransitionApi::class)
internal val MovieBoundsTransform = BoundsTransform { _, _ -> MovietMotion.slowSpatial() }
