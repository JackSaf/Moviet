/*
 * Adapted from Android's Navigation 3 list-detail recipe (Apache License 2.0):
 * https://github.com/android/nav3-recipes/tree/main/app/src/main/java/com/example/nav3recipes/scenes/listdetail
 * Copyright 2025 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy at https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.jacks.moviet.app.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.recalculateWindowInsets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.contains
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

internal val LocalTwoPane = compositionLocalOf { false }

internal data class ListDetailScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val listEntry: NavEntry<T>,
    val detailEntry: NavEntry<T>?,
) : Scene<T> {
    override val entries: List<NavEntry<T>> = listOfNotNull(listEntry, detailEntry)
    override val content: @Composable () -> Unit = {
        CompositionLocalProvider(LocalTwoPane provides true) {
            Row(modifier = Modifier.fillMaxSize().testTag("list_detail_scene")) {
                Box(modifier = Modifier.weight(ListPaneWeight).fillMaxHeight().recalculateWindowInsets()) { listEntry.Content() }
                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Box(modifier = Modifier.weight(DetailPaneWeight).fillMaxHeight().recalculateWindowInsets()) {
                    AnimatedContent(
                        targetState = detailEntry,
                        contentKey = { it?.contentKey ?: EmptyDetailKey },
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "Selected movie",
                    ) { entry ->
                        if (entry == null) DetailsPlaceholder() else entry.Content()
                    }
                }
            }
        }
    }

    companion object {
        fun listPane() = metadata { put(ListKey, true) }
        fun detailPane() = metadata { put(DetailKey, true) }
    }
    private object ListKey : NavMetadataKey<Boolean>
    private object DetailKey : NavMetadataKey<Boolean>

    internal class Strategy<T : Any>(private val useTwoPanes: Boolean) : SceneStrategy<T> {
        override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
            if (!useTwoPanes) return null
            val lastEntry = entries.lastOrNull() ?: return null
            val isList = lastEntry.metadata.contains(ListKey)
            val isDetail = lastEntry.metadata.contains(DetailKey)
            if (!isList && !isDetail) return null
            // Only pair with the immediately preceding list; unrelated routes keep their own scene.
            val list = if (isList) lastEntry else entries.getOrNull(entries.lastIndex - 1)
                ?.takeIf { it.metadata.contains(ListKey) } ?: return null
            return ListDetailScene(
                key = list.contentKey,
                previousEntries = entries.dropLast(1),
                listEntry = list,
                detailEntry = lastEntry.takeIf { isDetail },
            )
        }
    }
}

private const val ListPaneWeight = 0.4f
private const val DetailPaneWeight = 0.6f
private const val EmptyDetailKey = "empty-detail"
