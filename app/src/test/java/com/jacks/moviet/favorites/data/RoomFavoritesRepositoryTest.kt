package com.jacks.moviet.favorites.data

import com.jacks.moviet.core.database.entity.FavoriteMovieEntity
import com.jacks.moviet.core.domain.model.Movie
import com.jacks.moviet.core.domain.model.MovieType
import com.jacks.moviet.testutil.FakeFavoriteMovieDao
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class RoomFavoritesRepositoryTest {
    @Test
    fun `observers receive add and remove changes as domain movies`() = runBlocking {
        withTimeout(5_000) {
            val repository = RoomFavoritesRepository(FakeFavoriteMovieDao())
            val movie = Movie("tt1234567", "Example", null, MovieType.MOVIE)
            val lists = mutableListOf<List<Movie>>()
            val statuses = mutableListOf<Boolean>()
            val listJob = launch(start = CoroutineStart.UNDISPATCHED) {
                repository.observeFavorites().take(3).toList(lists)
            }
            val statusJob = launch(start = CoroutineStart.UNDISPATCHED) {
                repository.observeIsFavorite(movie.imdbId).take(3).toList(statuses)
            }
            repository.addFavorite(movie)
            yield()
            repository.removeFavorite(movie.imdbId)
            listJob.join()
            statusJob.join()
            assertEquals(listOf(emptyList(), listOf(movie), emptyList()), lists)
            assertEquals(listOf(false, true, false), statuses)
        }
    }

    @Test
    fun `adding the same identity updates rather than duplicating a favorite`() = runBlocking {
        val repository = RoomFavoritesRepository(FakeFavoriteMovieDao())
        val movie = Movie("tt1234567", "Example", null, MovieType.EPISODE)
        repository.addFavorite(movie)
        repository.addFavorite(movie.copy(title = "Updated"))
        assertEquals(listOf(movie.copy(title = "Updated")), repository.observeFavorites().first())
        assertFalse(repository.observeIsFavorite("tt7654321").first())
    }

    @Test
    fun `every movie type round trips through the persisted representation`() = runBlocking {
        val repository = RoomFavoritesRepository(FakeFavoriteMovieDao())
        MovieType.entries.forEach { type ->
            val movie = Movie("tt1234567", "Example", null, type)
            repository.addFavorite(movie)
            assertEquals(listOf(movie), repository.observeFavorites().first())
        }
    }

    @Test
    fun `unrecognized persisted type remains readable`() = runBlocking {
        val dao = FakeFavoriteMovieDao()
        dao.upsert(FavoriteMovieEntity("tt1234567", "Example", null, "new-type"))
        assertEquals(
            MovieType.UNKNOWN,
            RoomFavoritesRepository(dao).observeFavorites().first().single().type,
        )
    }
}
