package com.jacks.moviet.search.di

import com.jacks.moviet.search.presentation.SearchViewModel
import org.koin.dsl.module
import org.koin.plugin.module.dsl.viewModel

val searchModule = module {
    viewModel<SearchViewModel>()
}
