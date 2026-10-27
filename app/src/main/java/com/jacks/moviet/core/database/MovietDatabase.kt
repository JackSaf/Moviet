package com.jacks.moviet.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jacks.moviet.core.database.dao.FavoriteMovieDao
import com.jacks.moviet.core.database.entity.FavoriteMovieEntity

@Database(entities = [FavoriteMovieEntity::class], version = 1, exportSchema = true)
abstract class MovietDatabase : RoomDatabase() {
    abstract fun favoriteMovieDao(): FavoriteMovieDao

    companion object {
        const val FILE_NAME = "moviet.db"
    }
}
