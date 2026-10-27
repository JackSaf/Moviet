package com.jacks.moviet.app.navigation

import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.SceneStrategyScope
import org.junit.Assert.*
import org.junit.Test

class ListDetailNavigationTest {
    private val list = NavEntry<NavKey>(NavigationRoute.Search, metadata = ListDetailScene.listPane()) {}
    private val detail = NavEntry<NavKey>(NavigationRoute.Details("tt123"), metadata = ListDetailScene.detailPane()) {}

    @Test
    fun `window width switches panes at 600dp`() {
        assertFalse(useListDetail(599.dp))
        assertTrue(useListDetail(600.dp))
        assertTrue(useListDetail(840.dp))
    }

    @Test
    fun `narrow window falls back to a single pane with the same back stack`() {
        val scene = with(ListDetailScene.Strategy<NavKey>(false)) {
            SceneStrategyScope<NavKey>().calculateScene(listOf(list, detail))
        }
        assertNull(scene)
    }

    @Test
    fun `wide scene pairs the list and details and predicts back to the list`() {
        val scene = with(ListDetailScene.Strategy<NavKey>(true)) {
            SceneStrategyScope<NavKey>().calculateScene(listOf(list, detail))
        } as ListDetailScene<NavKey>
        assertEquals(listOf(list, detail), scene.entries)
        assertEquals(listOf(list), scene.previousEntries)
        assertEquals(list.contentKey, scene.key)
    }

    @Test
    fun `wide search has a details placeholder before selection`() {
        val scene = with(ListDetailScene.Strategy<NavKey>(true)) {
            SceneStrategyScope<NavKey>().calculateScene(listOf(list))
        } as ListDetailScene<NavKey>
        assertEquals(listOf(list), scene.entries)
        assertNull(scene.detailEntry)
        assertTrue(scene.previousEntries.isEmpty())
    }

    @Test
    fun `unrelated destinations do not form a list detail scene`() {
        val favorites = NavEntry<NavKey>(NavigationRoute.Favorites) {}
        val scene = with(ListDetailScene.Strategy<NavKey>(true)) {
            SceneStrategyScope<NavKey>().calculateScene(listOf(list, detail, favorites))
        }
        assertNull(scene)
    }

    @Test
    fun `changing selection replaces details so back returns directly to search`() {
        val stack = mutableListOf<NavKey>(NavigationRoute.Search)
        stack.openDetails("tt123")
        stack.openDetails("tt456")
        stack.openDetails("tt456")
        assertEquals(listOf(NavigationRoute.Search, NavigationRoute.Details("tt456")), stack)
        stack.removeAt(stack.lastIndex)
        assertEquals(listOf(NavigationRoute.Search), stack)
    }
}
