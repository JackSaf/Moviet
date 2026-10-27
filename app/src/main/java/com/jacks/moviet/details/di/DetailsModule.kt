package com.jacks.moviet.details.di

import com.jacks.moviet.details.data.OmdbDetailsRepository
import com.jacks.moviet.details.domain.DetailsRepository
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single
import org.koin.plugin.module.dsl.viewModel
import com.jacks.moviet.details.presentation.DetailsViewModel

val detailsModule = module {
    single<OmdbDetailsRepository>() bind DetailsRepository::class
    viewModel<DetailsViewModel>()
}
