package com.jacks.moviet.app.di

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.SavedStateHandle
import com.jacks.moviet.core.database.dao.FavoriteMovieDao
import com.jacks.moviet.core.network.data.OmdbApi
import com.jacks.moviet.core.network.utils.OmdbApiKeyInterceptor
import com.jacks.moviet.details.data.OmdbDetailsRepository
import com.jacks.moviet.details.domain.DetailsRepository
import com.jacks.moviet.details.presentation.DetailsViewModel
import com.jacks.moviet.favorites.data.RoomFavoritesRepository
import com.jacks.moviet.favorites.domain.FavoritesRepository
import com.jacks.moviet.favorites.presentation.FavoritesViewModel
import com.jacks.moviet.search.data.OmdbSearchRepository
import com.jacks.moviet.search.domain.SearchRepository
import com.jacks.moviet.search.presentation.SearchViewModel
import com.jacks.moviet.sdk.AdMobAdsService
import com.jacks.moviet.sdk.AdsService
import com.jacks.moviet.testutil.FakeFavoriteMovieDao
import okhttp3.OkHttpClient
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.core.parameter.parametersOf

class RepositoryModulesTest {
    @Test
    fun `app graph resolves repository interfaces and the Retrofit service as singletons`() {
        val application = koinApplication {
            // Substitute only the Android DAO factory; use the real production network graph.
            modules(appModule, module { single<FavoriteMovieDao> { FakeFavoriteMovieDao() } })
        }
        val viewModelStore = ViewModelStore()
        try {
            val koin = application.koin
            assertTrue(koin.get<SearchRepository>() is OmdbSearchRepository)
            assertTrue(koin.get<DetailsRepository>() is OmdbDetailsRepository)
            assertTrue(koin.get<FavoritesRepository>() is RoomFavoritesRepository)
            assertSame(koin.get<SearchRepository>(), koin.get<SearchRepository>())
            assertSame(koin.get<DetailsRepository>(), koin.get<DetailsRepository>())
            assertSame(koin.get<FavoritesRepository>(), koin.get<FavoritesRepository>())
            assertSame(koin.get<OmdbApi>(), koin.get<OmdbApi>())
            assertTrue(koin.get<AdsService>() is AdMobAdsService)
            assertSame(koin.get<AdsService>(), koin.get<AdsService>())
            val searchViewModel = koin.get<SearchViewModel> { parametersOf(SavedStateHandle()) }
            viewModelStore.put("search", searchViewModel)
            assertNotNull(searchViewModel)
            val detailsViewModel = koin.get<DetailsViewModel>()
            viewModelStore.put("details", detailsViewModel)
            assertNotNull(detailsViewModel)
            val favoritesViewModel = koin.get<FavoritesViewModel>()
            viewModelStore.put("favorites", favoritesViewModel)
            assertNotNull(favoritesViewModel)
            assertSame(
                koin.get<OmdbApiKeyInterceptor>(),
                koin.get<OkHttpClient>().interceptors.single(),
            )
        } finally {
            viewModelStore.clear()
            application.close()
        }
    }
}
