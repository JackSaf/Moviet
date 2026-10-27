package com.jacks.moviet.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategyScope
import org.junit.Assert.*
import org.junit.Test
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ListDetailNavigationTest {
    private val list = NavEntry<NavKey>(NavigationRoute.Search, metadata = ListDetailScene.listPane() + topLevelNavigation(NavigationRoute.Search)) {}
    private val detail = NavEntry<NavKey>(NavigationRoute.Details("tt123"), metadata = ListDetailScene.detailPane() + topLevelNavigation(NavigationRoute.Search)) {}
    private val favorites = NavEntry<NavKey>(NavigationRoute.Favorites, metadata = ListDetailScene.listPane() + topLevelNavigation(NavigationRoute.Favorites)) {}

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

    @Test
    fun `wide favorites pairs its own tickets with details and back returns to tickets`() {
        val scene = with(ListDetailScene.Strategy<NavKey>(true)) {
            SceneStrategyScope<NavKey>().calculateScene(listOf(list, favorites, detail))
        } as ListDetailScene<NavKey>
        assertEquals(listOf(favorites, detail), scene.entries)
        assertEquals(listOf(list, favorites), scene.previousEntries)
        assertEquals(favorites.contentKey, scene.key)
    }

    @Test
    fun `wide favorites has its own placeholder and backs to search`() {
        val scene = with(ListDetailScene.Strategy<NavKey>(true)) {
            SceneStrategyScope<NavKey>().calculateScene(listOf(list, favorites))
        } as ListDetailScene<NavKey>
        assertEquals(listOf(favorites), scene.entries)
        assertNull(scene.detailEntry)
        assertEquals(listOf(list), scene.previousEntries)
    }

    @Test
    fun `narrow favorites uses the standard single destination scene`() {
        val scene = with(ListDetailScene.Strategy<NavKey>(false)) {
            SceneStrategyScope<NavKey>().calculateScene(listOf(list, favorites, detail))
        }
        assertNull(scene)
    }

    @Test
    fun `switching tabs restores each selection and keeps same movie keys distinct`() {
        val navigation = navigationState()
        navigation.openDetails("tt123")
        navigation.selectTab(NavigationRoute.Favorites)
        navigation.openDetails("tt123")
        assertEquals(listOf(
            NavigationRoute.Search, NavigationRoute.Details("tt123"),
            NavigationRoute.Favorites, NavigationRoute.Details("tt123", NavigationRoute.Favorites),
        ), navigation.backStack)
        assertEquals(navigation.backStack.size, navigation.backStack.distinct().size)

        navigation.openDetails("tt456")
        repeat(3) {
            navigation.selectTab(NavigationRoute.Search)
            assertFalse(navigation.favoritesSelected)
            assertEquals(listOf(NavigationRoute.Search, NavigationRoute.Details("tt123")), navigation.backStack)
            navigation.selectTab(NavigationRoute.Favorites)
            assertTrue(navigation.favoritesSelected)
            assertEquals(NavigationRoute.Details("tt456", NavigationRoute.Favorites), navigation.backStack.last())
        }
        assertEquals("tt123", navigation.selectedMovieId(NavigationRoute.Search))
        assertEquals("tt456", navigation.selectedMovieId(NavigationRoute.Favorites))
    }

    @Test
    fun `phone tab switches use fade through in both directions but movie navigation does not`() {
        val search = singlePane(list)
        val saved = singlePane(favorites)
        val movie = singlePane(detail)
        val savedMovie = singlePane(NavEntry<NavKey>(
            NavigationRoute.Details("tt123", NavigationRoute.Favorites),
            metadata = ListDetailScene.detailPane() + topLevelNavigation(NavigationRoute.Favorites),
        ) {})
        assertTrue(isTopLevelSwitch(search, saved))
        assertTrue(isTopLevelSwitch(saved, search))
        assertFalse(isTopLevelSwitch(search, movie))
        assertFalse(isTopLevelSwitch(movie, search))
        assertFalse(isTopLevelSwitch(saved, savedMovie))
        assertFalse(isTopLevelSwitch(savedMovie, saved))
        assertTrue(isTopLevelSwitch(movie, savedMovie))
        assertTrue(isTopLevelSwitch(savedMovie, movie))
        assertTrue(isTopLevelSwitch(saved, movie))
    }

    @Test
    fun `wide tab switches use the list identity even when a movie is selected`() {
        val search = ListDetailScene(list.contentKey, emptyList(), list, detail)
        val saved = ListDetailScene(favorites.contentKey, listOf(list), favorites, null)
        val selectedFavorite = saved.copy(detailEntry = detail)
        assertTrue(isTopLevelSwitch(search, saved))
        assertTrue(isTopLevelSwitch(selectedFavorite, search))
        assertFalse(isTopLevelSwitch(saved, selectedFavorite))
        assertFalse(isTopLevelSwitch(search, search.copy(detailEntry = null)))
    }

    @Test
    fun `phone navigation hides for details and returns with its originating list`() {
        val navigation = navigationState()
        assertTrue(showMovieNavigation(navigation.backStack, useTwoPanes = false))
        navigation.selectTab(NavigationRoute.Favorites)
        assertTrue(showMovieNavigation(navigation.backStack, useTwoPanes = false))
        navigation.openDetails("tt123")
        assertFalse(showMovieNavigation(navigation.backStack, useTwoPanes = false))
        navigation.goBack()
        assertTrue(showMovieNavigation(navigation.backStack, useTwoPanes = false))
        assertEquals(NavigationRoute.Favorites, navigation.backStack.last())
    }

    @Test
    fun `wide navigation stays available while selecting and switching lists`() {
        val navigation = navigationState()
        navigation.openDetails("tt123")
        assertTrue(showMovieNavigation(navigation.backStack, useTwoPanes = true))
        navigation.selectTab(NavigationRoute.Favorites)
        navigation.openDetails("tt456")
        assertTrue(showMovieNavigation(navigation.backStack, useTwoPanes = true))
        navigation.selectTab(NavigationRoute.Search)
        assertTrue(showMovieNavigation(navigation.backStack, useTwoPanes = true))
        assertEquals(listOf(NavigationRoute.Search, NavigationRoute.Details("tt123")), navigation.backStack)
    }

    @Test
    fun `same movie from separate tabs retains distinct origins after restoration`() {
        val navigation = navigationState()
        navigation.openDetails("tt123")
        val searchDetails = navigation.backStack.last() as NavigationRoute.Details
        navigation.selectTab(NavigationRoute.Favorites)
        navigation.openDetails("tt123")
        val favoriteDetails = navigation.backStack.last() as NavigationRoute.Details
        assertEquals(NavigationRoute.Search, searchDetails.source)
        assertEquals(NavigationRoute.Favorites, favoriteDetails.source)
        assertNotEquals(searchDetails.source, favoriteDetails.source)

        for (route in listOf(searchDetails, favoriteDetails)) {
            val restored = Json.decodeFromString<NavigationRoute.Details>(Json.encodeToString(route))
            assertEquals(route, restored)
        }

        navigation.openDetails("tt456")
        assertEquals(NavigationRoute.Favorites, (navigation.backStack.last() as NavigationRoute.Details).source)
        navigation.goBack()
        assertEquals(favoriteDetails.source, navigation.backStack.last())
    }

    @Test
    fun `reselecting the active tab does not discard its details`() {
        val navigation = navigationState()
        for (tab in listOf(NavigationRoute.Search, NavigationRoute.Favorites)) {
            navigation.selectTab(tab)
            navigation.openDetails("tt123")
            val before = navigation.backStack
            navigation.selectTab(tab)
            assertEquals(before, navigation.backStack)
        }
    }

    @Test
    fun `back clears only active selection then returns to retained search selection`() {
        val navigation = navigationState()
        navigation.openDetails("tt123")
        navigation.selectTab(NavigationRoute.Favorites)
        navigation.openDetails("tt456")
        navigation.goBack()
        assertTrue(navigation.favoritesSelected)
        assertNull(navigation.selectedMovieId(NavigationRoute.Favorites))
        assertEquals("tt123", navigation.selectedMovieId(NavigationRoute.Search))
        navigation.goBack()
        assertFalse(navigation.favoritesSelected)
        assertEquals(NavigationRoute.Details("tt123"), navigation.backStack.last())
        navigation.goBack()
        navigation.goBack()
        assertEquals(listOf(NavigationRoute.Search), navigation.backStack)
        navigation.selectTab(NavigationRoute.Favorites)
        assertEquals(NavigationRoute.Favorites, navigation.backStack.last())
    }

    @Test
    fun `restored tab stacks keep both movie origins and back behavior`() {
        val search = listOf<NavigationRoute>(NavigationRoute.Search, NavigationRoute.Details("tt123"))
        val favorites = listOf<NavigationRoute>(NavigationRoute.Favorites, NavigationRoute.Details("tt123", NavigationRoute.Favorites))
        val restored = MovieNavigationState(
            Json.decodeFromString<List<NavigationRoute>>(Json.encodeToString(search)).toMutableList<NavKey>(),
            Json.decodeFromString<List<NavigationRoute>>(Json.encodeToString(favorites)).toMutableList<NavKey>(),
            mutableStateOf(true),
        )
        assertTrue(restored.favoritesSelected)
        assertEquals("tt123", restored.selectedMovieId(NavigationRoute.Search))
        assertEquals("tt123", restored.selectedMovieId(NavigationRoute.Favorites))
        restored.selectTab(NavigationRoute.Search)
        assertEquals(search, restored.backStack)
        restored.selectTab(NavigationRoute.Favorites)
        restored.goBack()
        assertEquals(NavigationRoute.Favorites, restored.backStack.last())
    }

    @Test
    fun `wide scene back at favorites root returns to search with its selected movie`() {
        val searchMovie = detail
        val scene = with(ListDetailScene.Strategy<NavKey>(true)) {
            SceneStrategyScope<NavKey>().calculateScene(listOf(list, searchMovie, favorites))
        } as ListDetailScene<NavKey>
        assertNull(scene.detailEntry)
        assertEquals(listOf(list, searchMovie), scene.previousEntries)
        val previousScene = with(ListDetailScene.Strategy<NavKey>(true)) {
            SceneStrategyScope<NavKey>().calculateScene(scene.previousEntries)
        } as ListDetailScene<NavKey>
        assertEquals(searchMovie, previousScene.detailEntry)
    }

    private fun navigationState() = MovieNavigationState(
        mutableListOf(NavigationRoute.Search),
        mutableListOf(NavigationRoute.Favorites),
        mutableStateOf(false),
    )

    private fun singlePane(entry: NavEntry<NavKey>): Scene<NavKey> = object : Scene<NavKey> {
        override val key = entry.contentKey
        override val previousEntries = emptyList<NavEntry<NavKey>>()
        override val entries = listOf(entry)
        override val content: @Composable () -> Unit = { entry.Content() }
    }
}
