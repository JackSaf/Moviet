package com.jacks.moviet.favorites.di

import com.jacks.moviet.favorites.data.RoomFavoritesRepository
import com.jacks.moviet.favorites.domain.FavoritesRepository
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val favoritesModule = module {
    single<RoomFavoritesRepository>() bind FavoritesRepository::class
}
