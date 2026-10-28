package com.jacks.moviet.favorites.data

import com.jacks.moviet.core.database.entity.FavoriteMovieEntity
import com.jacks.moviet.core.domain.model.Movie
import com.jacks.moviet.core.domain.model.MovieType
import com.jacks.moviet.testutil.FakeFavoriteMovieDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoomFavoritesRepositoryTest {
    @Test
    fun `observers receive add and remove changes as domain movies`() = runTest {
        val repository = RoomFavoritesRepository(FakeFavoriteMovieDao())
        val movie = Movie("tt1234567", "Example", null, MovieType.MOVIE)
        val lists = mutableListOf<List<Movie>>()
        val statuses = mutableListOf<Boolean>()
        backgroundScope.launch {
            repository.observeFavorites().take(3).toList(lists)
        }
        backgroundScope.launch {
            repository.observeIsFavorite(movie.imdbId).take(3).toList(statuses)
        }
        runCurrent()
        repository.addFavorite(movie)
        runCurrent()
        repository.removeFavorite(movie.imdbId)
        runCurrent()
        assertEquals(listOf(emptyList(), listOf(movie), emptyList()), lists)
        assertEquals(listOf(false, true, false), statuses)
    }

    @Test
    fun `adding the same identity updates rather than duplicating a favorite`() = runTest {
        val repository = RoomFavoritesRepository(FakeFavoriteMovieDao())
        val movie = Movie("tt1234567", "Example", null, MovieType.EPISODE)
        repository.addFavorite(movie)
        repository.addFavorite(movie.copy(title = "Updated"))
        assertEquals(listOf(movie.copy(title = "Updated")), repository.observeFavorites().first())
        assertFalse(repository.observeIsFavorite("tt7654321").first())
    }

    @Test
    fun `every movie type round trips through the persisted representation`() = runTest {
        val repository = RoomFavoritesRepository(FakeFavoriteMovieDao())
        MovieType.entries.forEach { type ->
            val movie = Movie("tt1234567", "Example", null, type)
            repository.addFavorite(movie)
            assertEquals(listOf(movie), repository.observeFavorites().first())
        }
    }

    @Test
    fun `saved favorites retain the year used by the shared subtitle`() = runTest {
        val dao = FakeFavoriteMovieDao()
        val repository = RoomFavoritesRepository(dao)
        val movie = Movie("tt0816692", "Interstellar", null, MovieType.MOVIE, year = "2014")
        repository.addFavorite(movie)

        // Recreate the repository so this cannot pass through in-memory movie retention.
        assertEquals(listOf(movie), RoomFavoritesRepository(dao).observeFavorites().first())
    }

    @Test
    fun `saving an older favorite fills its missing year without duplicating it`() = runTest {
        val dao = FakeFavoriteMovieDao()
        dao.upsert(FavoriteMovieEntity("tt0903747", "Breaking Bad", null, "series"))
        val repository = RoomFavoritesRepository(dao)
        val movie = Movie("tt0903747", "Breaking Bad", null, MovieType.SERIES, year = "2008–2013")

        assertEquals(null, repository.observeFavorites().first().single().year)
        repository.addFavorite(movie)
        assertEquals(listOf(movie), repository.observeFavorites().first())
    }

    @Test
    fun `unrecognized persisted type remains readable`() = runTest {
        val dao = FakeFavoriteMovieDao()
        dao.upsert(FavoriteMovieEntity("tt1234567", "Example", null, "new-type"))
        assertEquals(
            MovieType.UNKNOWN,
            RoomFavoritesRepository(dao).observeFavorites().first().single().type,
        )
    }
}
