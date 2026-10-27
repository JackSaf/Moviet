package com.jacks.moviet.favorites.di

import com.jacks.moviet.favorites.data.RoomFavoritesRepository
import com.jacks.moviet.favorites.domain.FavoritesRepository
import com.jacks.moviet.favorites.presentation.FavoritesViewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single
import org.koin.plugin.module.dsl.viewModel

val favoritesModule = module {
    single<RoomFavoritesRepository>() bind FavoritesRepository::class
    viewModel<FavoritesViewModel>()
}
