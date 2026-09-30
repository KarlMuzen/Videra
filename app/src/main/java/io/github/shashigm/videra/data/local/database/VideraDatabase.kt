package io.github.shashigm.videra.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.shashigm.videra.data.local.dao.AddonDao
import io.github.shashigm.videra.data.local.dao.LibraryDao
import io.github.shashigm.videra.data.local.entity.InstalledAddonEntity
import io.github.shashigm.videra.data.local.entity.LibraryEntity

@Database(
    entities = [
        InstalledAddonEntity::class,
        LibraryEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class VideraDatabase : RoomDatabase() {

    abstract fun addonDao(): AddonDao

    abstract fun libraryDao(): LibraryDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE installed_addons ADD COLUMN mediaItemsUrl TEXT"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS library_items (
                        libraryKey TEXT NOT NULL,
                        addonId TEXT NOT NULL,
                        addonName TEXT NOT NULL,
                        mediaId TEXT NOT NULL,
                        title TEXT NOT NULL,
                        posterUrl TEXT,
                        bannerUrl TEXT,
                        mediaType TEXT NOT NULL,
                        streamUrl TEXT,
                        streamQuality TEXT,
                        savedAt INTEGER NOT NULL,
                        PRIMARY KEY(libraryKey)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE installed_addons ADD COLUMN manifestUrl TEXT NOT NULL DEFAULT ''"
                )
                database.execSQL(
                    "UPDATE installed_addons SET manifestUrl = baseUrl WHERE manifestUrl = ''"
                )
                database.execSQL(
                    "ALTER TABLE installed_addons ADD COLUMN customName TEXT"
                )
                database.execSQL(
                    "ALTER TABLE installed_addons ADD COLUMN enabled INTEGER NOT NULL DEFAULT 1"
                )
                database.execSQL(
                    "ALTER TABLE installed_addons ADD COLUMN cachedMetadataJson TEXT"
                )
            }
        }

        fun create(context: Context): VideraDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                VideraDatabase::class.java,
                "videra.db"
            )
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4
                )
                .build()
        }
    }
}