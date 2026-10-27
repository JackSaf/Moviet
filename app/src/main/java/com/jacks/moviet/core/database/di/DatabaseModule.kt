package com.jacks.moviet.core.database.di

import androidx.room.Room
import com.jacks.moviet.core.database.MovietDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(androidContext(), MovietDatabase::class.java, MovietDatabase.FILE_NAME)
            .build()
    }
    single { get<MovietDatabase>().favoriteMovieDao() }
}
