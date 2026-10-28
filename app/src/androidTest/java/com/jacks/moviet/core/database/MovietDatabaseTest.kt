package com.jacks.moviet.core.database

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.jacks.moviet.core.domain.model.Movie
import com.jacks.moviet.core.domain.model.MovieType
import com.jacks.moviet.favorites.data.RoomFavoritesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MovietDatabaseTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @get:Rule
    val migration = MigrationTestHelper(instrumentation, MovietDatabase::class.java)

    @Before
    @After
    fun cleanUp() {
        context.deleteDatabase(TEST_DATABASE)
    }

    @Test
    fun migrationPreservesOldFavoritesAndAllowsYearUpdates() = runBlocking {
        migration.createDatabase(TEST_DATABASE, 1).apply {
            execSQL("INSERT INTO favorite_movies (imdbId, title, posterUrl, type) VALUES ('tt123', 'Original', NULL, 'movie')")
            close()
        }
        // Discover the generated auto-migration and validate the exported v2 schema.
        migration.runMigrationsAndValidate(TEST_DATABASE, 2, true).use { migrated ->
            migrated.query("SELECT title, year FROM favorite_movies WHERE imdbId = 'tt123'").use { row ->
                assertTrue(row.moveToFirst())
                assertEquals("Original", row.getString(0))
                assertTrue(row.isNull(1))
            }
        }
        withDatabase { database ->
            val repository = RoomFavoritesRepository(database.favoriteMovieDao())
            val original = withTimeout(READ_TIMEOUT) { repository.observeFavorites().first().single() }
            repository.addFavorite(original.copy(year = "2001–2003"))
            val movies = withTimeout(READ_TIMEOUT) { repository.observeFavorites().first() }
            assertEquals(1, movies.size)
            assertEquals("2001–2003", movies.single().year)
        }
    }

    @Test
    fun favoritesSurviveReopeningAndWritesAreVisibleToReads() = runBlocking {
        val movie = Movie("tt456", "Saved film", null, MovieType.MOVIE, "2024")
        withDatabase { database ->
            RoomFavoritesRepository(database.favoriteMovieDao()).addFavorite(movie)
        }
        withDatabase { database ->
            val repository = RoomFavoritesRepository(database.favoriteMovieDao())
            assertEquals(listOf(movie), withTimeout(READ_TIMEOUT) { repository.observeFavorites().first() })
            assertTrue(withTimeout(READ_TIMEOUT) { repository.observeIsFavorite(movie.imdbId).first() })
            repository.addFavorite(movie.copy(title = "Updated film"))
            assertEquals("Updated film", withTimeout(READ_TIMEOUT) { repository.observeFavorites().first().single().title })
            repository.removeFavorite(movie.imdbId)
            assertFalse(withTimeout(READ_TIMEOUT) { repository.observeIsFavorite(movie.imdbId).first() })
        }
        withDatabase { database ->
            assertTrue(withTimeout(READ_TIMEOUT) { database.favoriteMovieDao().observeFavorites().first() }.isEmpty())
        }
    }

    private fun openDatabase() = Room.databaseBuilder(context, MovietDatabase::class.java, TEST_DATABASE).build()

    private inline fun withDatabase(block: (MovietDatabase) -> Unit) {
        val database = openDatabase()
        try {
            block(database)
        } finally {
            database.close()
        }
    }
}

private const val TEST_DATABASE = "favorites-integration-test.db"
private const val READ_TIMEOUT = 5_000L
