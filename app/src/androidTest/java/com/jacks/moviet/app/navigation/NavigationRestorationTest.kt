package com.jacks.moviet.app.navigation

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class NavigationRestorationTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var navigation: MovieNavigationState
    private val models = mutableMapOf<String, EntryLifetime>()

    @Test
    fun tabSwitchRetainsEntryStateAndPoppingClearsOnlyItsViewModel() {
        compose.setContent { TestNavigation() }
        compose.onNodeWithTag("search").performTextReplacement("batman")
        val searchModel = compose.runOnIdle { models.getValue("search") }
        compose.runOnIdle { navigation.openDetails("tt123") }
        compose.onNodeWithTag("search-tt123").performTextReplacement("expanded plot")
        val searchDetails = compose.runOnIdle { models.getValue("search-tt123") }
        compose.runOnIdle {
            navigation.selectTab(NavigationRoute.Favorites)
            navigation.openDetails("tt123")
        }
        compose.onNodeWithTag("favorites-tt123").performTextReplacement("favorite plot")
        val favoriteDetails = compose.runOnIdle { models.getValue("favorites-tt123") }
        compose.runOnIdle { navigation.selectTab(NavigationRoute.Search) }
        compose.onNodeWithTag("search-tt123").assertTextEquals("expanded plot")
        compose.runOnIdle {
            assertSame(searchDetails, models.getValue("search-tt123"))
            assertFalse(searchDetails.cleared)
            assertFalse(favoriteDetails.cleared)
            navigation.goBack()
        }
        compose.onNodeWithTag("search").assertTextEquals("batman")
        compose.runOnIdle {
            assertSame(searchModel, models.getValue("search"))
            assertTrue(searchDetails.cleared)
            assertFalse(favoriteDetails.cleared)
            navigation.selectTab(NavigationRoute.Favorites)
        }
        compose.onNodeWithTag("favorites-tt123").assertTextEquals("favorite plot")
    }

    @Test
    fun savedStateRestoresBothSelectionsActiveTabAndEntryContent() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { TestNavigation() }
        compose.onNodeWithTag("search").performTextReplacement("batman")
        compose.runOnIdle { navigation.openDetails("tt123") }
        compose.onNodeWithTag("search-tt123").performTextReplacement("search detail")
        compose.runOnIdle {
            navigation.selectTab(NavigationRoute.Favorites)
            navigation.openDetails("tt456")
        }
        compose.onNodeWithTag("favorites-tt456").performTextReplacement("favorite detail")
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithTag("favorites-tt456").assertTextEquals("favorite detail")
        compose.runOnIdle {
            assertTrue(navigation.favoritesSelected)
            assertEquals("tt123", navigation.selectedMovieId(NavigationRoute.Search))
            assertEquals("tt456", navigation.selectedMovieId(NavigationRoute.Favorites))
            navigation.selectTab(NavigationRoute.Search)
        }
        compose.onNodeWithTag("search-tt123").assertTextEquals("search detail")
        compose.runOnIdle { navigation.goBack() }
        compose.onNodeWithTag("search").assertTextEquals("batman")
    }

    @Composable
    private fun TestNavigation() {
        navigation = rememberMovieNavigationState()
        NavDisplay(
            entries = navigation.decoratedEntries(entryProvider {
                entry<NavigationRoute.Search> { EntryContent("search") }
                entry<NavigationRoute.Favorites> { EntryContent("favorites") }
                entry<NavigationRoute.Details> { route ->
                    val source = if (route.source == NavigationRoute.Search) "search" else "favorites"
                    EntryContent("$source-${route.imdbId}")
                }
            }),
            onBack = navigation::goBack,
        )
    }

    @Composable
    private fun EntryContent(tag: String) {
        var text by rememberSaveable { mutableStateOf("") }
        models[tag] = viewModel<EntryLifetime>()
        BasicTextField(modifier = Modifier.testTag(tag), value = text, onValueChange = { text = it })
    }
}

class EntryLifetime : ViewModel() {
    var cleared = false
        private set

    override fun onCleared() { cleared = true }
}
