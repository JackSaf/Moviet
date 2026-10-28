package com.jacks.moviet.search.presentation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.paging.PagingData
import androidx.test.platform.app.InstrumentationRegistry
import com.jacks.moviet.R
import com.jacks.moviet.core.designsystem.theme.MovietTheme
import com.jacks.moviet.core.domain.NetworkResult
import com.jacks.moviet.core.domain.model.Movie
import com.jacks.moviet.core.domain.model.MovieType
import com.jacks.moviet.sdk.AdsService
import com.jacks.moviet.sdk.NativeAdSlot
import com.jacks.moviet.search.domain.MovieSearchPage
import com.jacks.moviet.search.domain.MovieSearchResult
import com.jacks.moviet.search.domain.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import org.junit.Rule

class SearchClearTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun clearLoadedAndScrolledResultsThenSearchAgain() {
        val store = ViewModelStore()
        val model = SearchViewModel(LoadedSearchRepository(), SavedStateHandle())
        store.put("search", model)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        try {
            compose.setContent {
                MovietTheme {
                    SearchScreenRoot(
                        onFavoritesClick = {},
                        onMovieClick = {},
                        viewModel = model,
                        ads = NoAds,
                    )
                }
            }
            repeat(3) { round ->
                compose.onNodeWithTag("search_query").performTextReplacement("batman")
                compose.waitUntil(timeoutMillis = 5_000) {
                    compose.onAllNodesWithText("Movie 0").fetchSemanticsNodes().isNotEmpty()
                }
                if (round > 0) {
                    compose.onNodeWithTag("search_results").performScrollToIndex(20)
                }
                compose.onNodeWithContentDescription(context.getString(R.string.search_clear)).performClick()
                compose.onNodeWithTag("search_query").assert(
                    SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("")),
                )
                compose.onNodeWithText(context.getString(R.string.search_start_title)).assertIsDisplayed()
                compose.onNodeWithText("Movie 0").assertDoesNotExist()
            }
        } finally {
            compose.runOnIdle { store.clear() }
        }
    }
}

private class LoadedSearchRepository : SearchRepository {
    override fun observeSearchMovies(query: String): Flow<PagingData<MovieSearchResult>> = flowOf(
        if (query.isBlank()) PagingData.empty() else PagingData.from(
            List(30) { index ->
                MovieSearchResult(Movie("tt$index", "Movie $index", null, MovieType.MOVIE), totalResults = 30)
            },
        ),
    )

    override suspend fun searchMovies(query: String, page: Int): NetworkResult<MovieSearchPage> =
        error("The UI test uses the paging stream")
}

private object NoAds : AdsService {
    override fun initialize(application: Application) = Unit

    @Composable
    override fun NativeAd(modifier: Modifier, slot: NativeAdSlot, compact: Boolean) = Unit
}
