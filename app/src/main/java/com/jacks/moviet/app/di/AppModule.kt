package com.jacks.moviet.app.di

import com.jacks.moviet.core.database.di.databaseModule
import com.jacks.moviet.core.network.di.networkModule
import com.jacks.moviet.search.di.searchModule
import org.koin.dsl.module

val appModule = module {
    includes(databaseModule, networkModule, searchModule)
}
