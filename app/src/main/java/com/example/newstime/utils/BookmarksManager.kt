package com.example.newstime.utils

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.annotation.NonNull

object BookmarksManager {
    private const val DB_NAME =  "bookmarks_db"

    @Volatile
    private var INSTANCE: BookmarksDB? = null

    fun initDb(ctx: Context): BookmarksDB {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                ctx.applicationContext,
                BookmarksDB::class.java,
                DB_NAME
            ).build().also { INSTANCE = it }
        }
    }
}

@Entity
data class BookmarkMeta(
    @PrimaryKey val articleId: String,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "url") val link: String?,
    @ColumnInfo(name = "date_published") val dateSaved: String?,
    @ColumnInfo(name = "imageUrl") val imageUrl: String?,
    @ColumnInfo(name = "desc") val desc: String?
)

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM BookmarkMeta")
    suspend fun getAll(): List<BookmarkMeta>

    @Query("SELECT url FROM BookmarkMeta WHERE title LIKE '%' || :title || '%'")
    suspend fun getLinkByTitle(title: String): List<String>

    @Query("DELETE FROM BookmarkMeta WHERE articleId LIKE '%' || :id_ || '%'")
    suspend fun deleteById(id_: String)

    @Query("SELECT * FROM BookmarkMeta WHERE articleId = :articleId LIMIT 1")
    suspend fun getById(articleId: String): BookmarkMeta?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkMeta)
}

@Database(entities = [BookmarkMeta::class], version = 1)
abstract class BookmarksDB : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
}