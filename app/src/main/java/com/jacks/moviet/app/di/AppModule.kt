package com.jacks.moviet.app.di

import com.jacks.moviet.core.database.di.databaseModule
import com.jacks.moviet.core.network.di.networkModule
import com.jacks.moviet.details.di.detailsModule
import com.jacks.moviet.favorites.di.favoritesModule
import com.jacks.moviet.search.di.searchModule
import com.jacks.moviet.sdk.AdMobAdsService
import com.jacks.moviet.sdk.AdsService
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val appModule = module {
    includes(databaseModule, networkModule, searchModule, detailsModule, favoritesModule)
    single<AdMobAdsService>() bind AdsService::class
}
