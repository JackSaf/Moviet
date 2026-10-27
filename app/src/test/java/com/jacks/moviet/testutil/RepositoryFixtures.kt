package com.jacks.moviet.testutil

import com.jacks.moviet.core.database.dao.FavoriteMovieDao
import com.jacks.moviet.core.database.entity.FavoriteMovieEntity
import com.jacks.moviet.core.network.data.OmdbApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private val fixtureJson = Json { ignoreUnknownKeys = true }

/** Exercises Retrofit encoding/serialization without credentials, sockets, or an OMDb account. */
internal fun omdbApi(
    body: String = "{}",
    statusCode: Int = 200,
    onRequest: (Request) -> Unit = {},
): OmdbApi {
    val jsonType = "application/json".toMediaType()
    val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()
            onRequest(request)
            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(statusCode)
                .message("Fixture")
                .body(body.toResponseBody(jsonType))
                .build()
        }
        .build()
    return Retrofit.Builder()
        .baseUrl("https://www.omdbapi.com/")
        .client(client)
        .addConverterFactory(fixtureJson.asConverterFactory(jsonType))
        .build()
        .create(OmdbApi::class.java)
}

/** A repository test double; real Room persistence is outside these JVM tests. */
internal class FakeFavoriteMovieDao : FavoriteMovieDao {
    private val movies = MutableStateFlow<List<FavoriteMovieEntity>>(emptyList())

    override fun observeFavorites(): Flow<List<FavoriteMovieEntity>> = movies

    override fun observeIsFavorite(imdbId: String): Flow<Boolean> = movies.map { list ->
        list.any { it.imdbId == imdbId }
    }

    override suspend fun upsert(movie: FavoriteMovieEntity) {
        movies.value = movies.value.filterNot { it.imdbId == movie.imdbId } + movie
    }

    override suspend fun delete(imdbId: String) {
        movies.value = movies.value.filterNot { it.imdbId == imdbId }
    }
}
