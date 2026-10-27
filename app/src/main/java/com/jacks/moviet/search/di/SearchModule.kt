package com.jacks.moviet.search.di

import com.jacks.moviet.search.data.OmdbSearchRepository
import com.jacks.moviet.search.domain.SearchRepository
import com.jacks.moviet.search.presentation.SearchViewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single
import org.koin.plugin.module.dsl.viewModel

val searchModule = module {
    single<OmdbSearchRepository>() bind SearchRepository::class
    viewModel<SearchViewModel>()
}
