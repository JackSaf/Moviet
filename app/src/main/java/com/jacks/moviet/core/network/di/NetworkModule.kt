package com.jacks.moviet.core.network.di

import com.jacks.moviet.BuildConfig
import com.jacks.moviet.core.network.OmdbConfig
import com.jacks.moviet.core.network.data.OmdbApi
import com.jacks.moviet.core.network.utils.OmdbApiKeyInterceptor
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

val networkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
        }
    }
    single { OmdbApiKeyInterceptor(apiKey = BuildConfig.OMDB_API_KEY) }
    single {
        OkHttpClient.Builder()
            .callTimeout(OmdbConfig.REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(get<OmdbApiKeyInterceptor>())
            .build()
    }
    single {
        Retrofit.Builder()
            .baseUrl(BuildConfig.OMDB_BASE_URL)
            .client(get())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }
    single { get<Retrofit>().create(OmdbApi::class.java) }
}
